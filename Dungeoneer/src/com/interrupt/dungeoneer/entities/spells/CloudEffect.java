package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

/**
 * Runtime entity created by Cloud.
 *
 * This class is intentionally not a spell type. It is the persistent world
 * effect that keeps the Cloud alive after the initial cast.
 */
public class CloudEffect extends Entity {

    /** Horizontal radius of the damaging cloud. */
    public float radius = 2.5f;

    /** Total vertical height of the cloud volume. */
    public float cloudHeight = 1.5f;

    /** Lifetime in Delver gameplay ticks. */
    public float duration = 300f;

    /** Time between damage pulses in Delver gameplay ticks. */
    public float damageTickRate = 30f;

    /** Apply a damage pulse immediately when the cloud begins. */
    public boolean damageImmediately = true;

    /** Damage roll copied from the casting spell. */
    public int baseDamage = 1;
    public int randDamage = 1;
    public float damageMultiplier = 1f;
    public DamageType damageType = DamageType.MAGIC;

    /** Horizontal knockback away from the cloud center. */
    public float knockback = 0f;

    /** Require clear LOS from cloud center to each target. */
    public boolean requiresLineOfSight = true;

    /** Optional status effect applied on damage pulses. */
    public StatusEffect applyStatusEffect = null;

    /** Chance from 0.0 to 1.0 to apply applyStatusEffect. */
    public float statusEffectChance = 1f;

    /**
     * Preserve normal spell allegiance automatically:
     * Player owner -> monsters, Monster owner -> player.
     * Neutral/environmental owners use the explicit damage flags below.
     */
    public boolean autoTargetByOwner = true;

    /** Allow this cloud to damage Player targets. */
    public boolean damagePlayers = true;

    /** Allow this cloud to damage Monster targets. */
    public boolean damageMonsters = true;

    /** Allow this cloud to damage other living Actor types. */
    public boolean damageOtherActors = false;


    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    public boolean showCloudParticles = true;
    public String particleSpriteAtlas = "particle";
    public int particleStartTex = 8;
    public int particleEndTex = 12;
    public float particleAnimSpeed = 35f;
    public int particlesPerPulse = 5;
    public float particleInterval = 4f;
    public float particleSpeed = 0.01f;
    public float particleLifetime = 45f;
    public float particleScale = 0.7f;
    public float particleEndScale = 0.25f;
    public boolean particleFullbrite = true;
    public Color cloudColor = new Color(Color.WHITE);


    // ---------------------------------------------------------------------
    // RUNTIME STATE
    // ---------------------------------------------------------------------

    private transient boolean runtimeStarted = false;
    private transient float lifeTimer = 0f;
    private transient float damageTimer = 0f;
    private transient float particleTimer = 0f;


    public CloudEffect() {
        // This entity is only a runtime controller. Particles provide the
        // visuals, so the controller itself should never be rendered.
        artType = ArtType.hidden;
        hidden = true;
        isSolid = false;
        isDynamic = false;
        persists = false;
        floating = true;
        collision.set(0.05f, 0.05f, 0.05f);
    }


    @Override
    public void tick(Level level, float delta) {
        if(!isActive) {
            return;
        }

        if(!runtimeStarted) {
            runtimeStarted = true;

            if(showCloudParticles) {
                spawnCloudParticles(level);
            }

            if(damageImmediately) {
                damageTargets(level);
            }
        }

        lifeTimer += delta;

        if(duration <= 0f || lifeTimer >= duration) {
            isActive = false;
            return;
        }

        damageTimer += delta;
        particleTimer += delta;

        float safeDamageTickRate = Math.max(0.01f, damageTickRate);

        // Normally this runs once, but the loop keeps cloud timing stable if a
        // frame has an unusually large gameplay delta.
        int damagePulses = 0;
        while(damageTimer >= safeDamageTickRate && damagePulses < 4) {
            damageTimer -= safeDamageTickRate;
            damageTargets(level);
            damagePulses++;
        }

        if(showCloudParticles) {
            float safeParticleInterval = Math.max(0.1f, particleInterval);

            int particlePulses = 0;
            while(particleTimer >= safeParticleInterval && particlePulses < 4) {
                particleTimer -= safeParticleInterval;
                spawnCloudParticles(level);
                particlePulses++;
            }
        }
    }


    // ---------------------------------------------------------------------
    // DAMAGE
    // ---------------------------------------------------------------------

    private void damageTargets(Level level) {
        if(owner == null || level == null) {
            return;
        }

        float safeRadius = Math.max(0.05f, radius);
        float radiusSquared = safeRadius * safeRadius;

        /*
         * Match Delver's proven Explosion-style area lookup: damage is based
         * primarily on horizontal distance in the 2.5D world. cloudHeight is
         * visual shaping only; it must not prevent an actor standing in the
         * visible cloud from taking damage.
         */
        Array<Entity> candidates = new Array<Entity>();

        // Copy spatial-hash results because later LOS queries can reuse the
        // spatial hash's internal temporary arrays.
        candidates.addAll(
            level.spatialhash.getEntitiesAt(x, y, safeRadius)
        );

        // Also include living monsters directly in case a custom monster is
        // non-solid and therefore absent from the spatial hash.
        if(level.entities != null) {
            for(Entity entity : level.entities) {
                if(entity instanceof Monster
                    && !candidates.contains(entity, true)) {

                    candidates.add(entity);
                }
            }
        }

        // Player is managed separately by the level and may not be present in
        // every query path, so always include it explicitly.
        if(Game.instance != null && Game.instance.player != null) {
            Player player = Game.instance.player;
            if(!candidates.contains(player, true)) {
                candidates.add(player);
            }
        }

        Vector3 hitDirection = new Vector3();

        for(Entity target : candidates) {
            if(!isValidTarget(target)) {
                continue;
            }

            float dx = target.x - x;
            float dy = target.y - y;
            float distanceSquared = (dx * dx) + (dy * dy);

            if(distanceSquared > radiusSquared) {
                continue;
            }

            /*
             * Delver's canSee3D truncates Z values to integers while walking
             * the ray. That works for several engine uses but is too strict
             * for a floor-hugging persistent cloud, especially on raised or
             * sloped tiles. Use the engine's normal wall/door LOS check only.
             */
            if(requiresLineOfSight
                && !level.canSeeIncludingDoors(
                    x,
                    y,
                    target.x,
                    target.y,
                    safeRadius + 1f)) {

                continue;
            }

            hitDirection.set(dx, dy, 0f);
            if(!hitDirection.isZero()) {
                hitDirection.nor();
            }

            target.hit(
                hitDirection.x,
                hitDirection.y,
                rollDamage(),
                knockback,
                damageType,
                owner
            );

            applyConfiguredStatusEffect(target);
        }
    }




    private int rollDamage() {
        int randomDamage = Math.max(0, randDamage);
        int damage = baseDamage;

        if(randomDamage > 0) {
            damage += Game.rand.nextInt(randomDamage + 1);
        }

        damage = Math.round(damage * Math.max(0f, damageMultiplier));

        if(damage < 1) {
            damage = 1;
        }

        return damage;
    }


    private boolean isValidTarget(Entity target) {
        if(target == null
            || target == owner
            || !target.isActive) {

            return false;
        }

        boolean isPlayerTarget = target instanceof Player;
        boolean isMonsterTarget = target instanceof Monster;

        if(isPlayerTarget && !((Player)target).isAlive()) {
            return false;
        }

        if(isMonsterTarget && !((Monster)target).isAlive()) {
            return false;
        }

        /*
         * Default spell allegiance. This preserves the behavior already used
         * by Cone/Nova/Cloud while still allowing environmental owners.
         */
        if(autoTargetByOwner) {
            if(owner instanceof Player) {
                return damageMonsters && isMonsterTarget;
            }

            if(owner instanceof Monster) {
                return damagePlayers && isPlayerTarget;
            }
        }

        /*
         * Neutral/environmental owners (mushrooms, vents, traps, etc.) use
         * the explicit target flags directly. Setting autoTargetByOwner=false
         * also forces any caster to use these explicit rules.
         */
        if(isPlayerTarget) {
            return damagePlayers;
        }

        if(isMonsterTarget) {
            return damageMonsters;
        }

        return damageOtherActors
            && target instanceof Actor
            && ((Actor)target).isAlive();
    }


    private boolean hasLineOfSight(Level level,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float maxDistance) {

        return level.canSeeIncludingDoors(
            x1,
            y1,
            x2,
            y2,
            maxDistance + 1f
        )
        && level.canSee3D(
            x1,
            y1,
            z1,
            x2,
            y2,
            z2
        );
    }


    private void applyConfiguredStatusEffect(Entity target) {
        if(applyStatusEffect == null || !(target instanceof Actor)) {
            return;
        }

        float chance = MathUtils.clamp(statusEffectChance, 0f, 1f);

        if(chance <= 0f || Game.rand.nextFloat() > chance) {
            return;
        }

        StatusEffect copiedEffect =
            (StatusEffect)KryoSerializer.copyObject(applyStatusEffect);

        if(copiedEffect != null) {
            ((Actor)target).addStatusEffect(copiedEffect);
        }
    }


    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    private void spawnCloudParticles(Level level) {
        int count = MathUtils.clamp(particlesPerPulse, 0, 64);

        if(count <= 0) {
            return;
        }

        float safeRadius = Math.max(0.05f, radius);
        float safeHeight = Math.max(0.05f, cloudHeight);
        float halfHeight = safeHeight * 0.5f;

        for(int i = 0; i < count; i++) {
            // sqrt(random) distributes particles evenly across the area instead
            // of clustering them at the cloud center.
            float radialDistance =
                (float)Math.sqrt(Game.rand.nextFloat()) * safeRadius;

            float angle = Game.rand.nextFloat() * MathUtils.PI2;

            float offsetX = MathUtils.cos(angle) * radialDistance;
            float offsetY = MathUtils.sin(angle) * radialDistance;
            float offsetZ = (Game.rand.nextFloat() - 0.5f) * safeHeight;

            float horizontalAngle = Game.rand.nextFloat() * MathUtils.PI2;
            float horizontalSpeed =
                particleSpeed * (0.4f + (Game.rand.nextFloat() * 0.6f));

            float velocityX = MathUtils.cos(horizontalAngle) * horizontalSpeed;
            float velocityY = MathUtils.sin(horizontalAngle) * horizontalSpeed;
            float velocityZ =
                particleSpeed * (0.25f + (Game.rand.nextFloat() * 0.75f));

            Particle particle = new Particle(
                x + offsetX,
                y + offsetY,
                z + MathUtils.clamp(offsetZ, -halfHeight, halfHeight),
                velocityX,
                velocityY,
                velocityZ,
                particleStartTex,
                cloudColor == null ? Color.WHITE : cloudColor,
                particleFullbrite
            );

            particle.setSpriteAtlas(particleSpriteAtlas);
            particle.tex = particleStartTex;
            particle.floating = true;
            particle.checkCollision = false;

            particle.lifetime =
                particleLifetime
                * (0.85f + (Game.rand.nextFloat() * 0.3f));

            particle.scale = particleScale;
            particle.startScale = particleScale;
            particle.endScale = particleEndScale;

            // Same initialization requirement as the working Cone/Nova VFX.
            // Without this, scale interpolation can fail on the first tick.
            particle.initialized = false;

            particle.blendMode = Entity.BlendMode.ADD;

            if(particleEndTex > particleStartTex) {
                particle.playAnimation(
                    particleStartTex,
                    particleEndTex,
                    particleAnimSpeed,
                    true
                );
            }

            level.SpawnNonCollidingEntity(particle);
        }
    }
}
