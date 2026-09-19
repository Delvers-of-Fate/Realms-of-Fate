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
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

/**
 * Instant radial area spell centered on the caster.
 *
 * Damage is applied immediately to valid targets inside the configured radius.
 * Particles are visual only and burst outward in 360 degrees around the caster.
 */
public class Nova extends Spell {

    /** Maximum 3D distance from the caster that the Nova can hit. */
    public float radius = 5f;

    /** Horizontal knockback applied away from the caster. */
    public float knockback = 0.25f;

    /** Require clear line of sight before damaging a target. */
    public boolean requiresLineOfSight = true;

    /** Chance from 0.0 to 1.0 to apply applyStatusEffect. */
    public float statusEffectChance = 1f;


    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    /** Spawn Nova particles when cast. */
    public boolean showNovaParticles = true;

    /** Sprite atlas used by Nova particles. */
    public String particleSpriteAtlas = "particle";

    /** First particle texture frame. */
    public int particleStartTex = 8;

    /** Last particle texture frame. Same as start for a static sprite. */
    public int particleEndTex = 12;

    /** Particle animation speed. */
    public float particleAnimSpeed = 35f;

    /** Number of particles spawned per cast. */
    public int particleCount = 48;

    /** Particle travel speed. */
    public float particleSpeed = 0.07f;

    /** Particle lifetime in Delver ticks. */
    public float particleLifetime = 28f;

    /** Initial particle scale. */
    public float particleScale = 0.6f;

    /** Final particle scale. */
    public float particleEndScale = 0.1f;

    /** Distance from the Nova origin where particles begin. */
    public float particleSpawnDistance = 0.2f;

    /**
     * Vertical spread of the particle burst.
     *
     * 0.0 = flat horizontal ring
     * 0.35 = broad magical burst
     * 1.0 = full spherical burst
     */
    public float particleVerticalSpread = 0.35f;

    /** Make particles full-bright. */
    public boolean particleFullbrite = true;


    public Nova() {
        // Nova creates its own radial VFX.
        doCastVfx = false;
    }


    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {
        if(owner == null || position == null) {
            return;
        }

        Level level = Game.GetLevel();
        if(level == null) {
            return;
        }

        float originX = position.x;
        float originY = position.y;
        float originZ = position.z;

        // For the local player, use the first-person camera origin so the burst
        // is visibly centered on the player's view and LOS checks match what the
        // player can actually see.
        if(Game.instance != null
            && owner == Game.instance.player
            && Game.camera != null) {

            originX = Game.camera.position.x;
            originY = Game.camera.position.z;
            originZ = Game.camera.position.y;
        }
        else if(owner instanceof Monster) {
            // Monster spell positions originate near their feet, so raise the
            // Nova to the monster's configured projectile height.
            originZ += ((Monster)owner).projectileOffset;
        }

        damageTargets(level, owner, originX, originY, originZ);

        if(showNovaParticles) {
            spawnNovaParticles(level, originX, originY, originZ);
        }
    }


    // ---------------------------------------------------------------------
    // DAMAGE
    // ---------------------------------------------------------------------

    private void damageTargets(Level level, Entity owner,
                               float originX, float originY, float originZ) {

        float safeRadius = Math.max(0.01f, radius);
        float safeRadiusSquared = safeRadius * safeRadius;

        Array<Entity> candidates = new Array<Entity>();
        candidates.addAll(
            level.spatialhash.getEntitiesAt(originX, originY, safeRadius)
        );

        Array<Entity> staticCandidates =
            level.staticSpatialhash.getEntitiesAt(originX, originY, safeRadius);

        for(Entity entity : staticCandidates) {
            if(!candidates.contains(entity, true)) {
                candidates.add(entity);
            }
        }

        // Player is not guaranteed to exist in the same spatial hash queried
        // for world actors, so explicitly include the player for monster casts.
        if(owner instanceof Monster
            && Game.instance != null
            && Game.instance.player != null) {

            Player player = Game.instance.player;
            if(!candidates.contains(player, true)) {
                candidates.add(player);
            }
        }

        Vector3 hitDirection = new Vector3();

        for(Entity target : candidates) {
            if(!isValidTarget(owner, target)) {
                continue;
            }

            float targetZ = target.z + (target.collision.z * 0.5f);

            float dx = target.x - originX;
            float dy = target.y - originY;
            float dz = targetZ - originZ;

            float distanceSquared =
                (dx * dx)
                + (dy * dy)
                + (dz * dz);

            if(distanceSquared <= 0.000001f
                || distanceSquared > safeRadiusSquared) {
                continue;
            }

            if(requiresLineOfSight
                && !hasLineOfSight(
                    level,
                    originX, originY, originZ,
                    target.x, target.y, targetZ,
                    safeRadius)) {

                continue;
            }

            int damage = doAttackRoll();

            // Nova knockback always pushes horizontally away from the caster.
            hitDirection.set(dx, dy, 0f);

            if(!hitDirection.isZero()) {
                hitDirection.nor();
            }

            target.hit(
                hitDirection.x,
                hitDirection.y,
                damage,
                knockback,
                damageType,
                owner
            );

            applyConfiguredStatusEffect(target);
        }
    }


    // ---------------------------------------------------------------------
    // VALID TARGETS
    // ---------------------------------------------------------------------

    private boolean isValidTarget(Entity owner, Entity target) {
        if(target == null || target == owner || !target.isActive) {
            return false;
        }

        // Player Nova damages living monsters.
        if(owner instanceof Player) {
            return target instanceof Monster
                && ((Monster)target).isAlive();
        }

        // Monster Nova damages the player.
        if(owner instanceof Monster) {
            return target instanceof Player
                && ((Player)target).isAlive();
        }

        // Fallback for future caster types.
        return target instanceof Actor
            && ((Actor)target).isAlive();
    }


    // ---------------------------------------------------------------------
    // LINE OF SIGHT
    // ---------------------------------------------------------------------

    private boolean hasLineOfSight(Level level,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float maxDistance) {

        return level.canSeeIncludingDoors(
            x1, y1,
            x2, y2,
            maxDistance + 1f
        )
        && level.canSee3D(
            x1, y1, z1,
            x2, y2, z2
        );
    }


    // ---------------------------------------------------------------------
    // STATUS EFFECT
    // ---------------------------------------------------------------------

    private void applyConfiguredStatusEffect(Entity target) {
        if(applyStatusEffect == null || !(target instanceof Actor)) {
            return;
        }

        float chance = MathUtils.clamp(statusEffectChance, 0f, 1f);

        if(chance <= 0f || Game.rand.nextFloat() > chance) {
            return;
        }

        // Copy the configured effect so every target receives its own runtime
        // StatusEffect instance.
        StatusEffect copiedEffect =
            (StatusEffect)KryoSerializer.copyObject(applyStatusEffect);

        if(copiedEffect != null) {
            ((Actor)target).addStatusEffect(copiedEffect);
        }
    }


    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    private void spawnNovaParticles(Level level,
                                    float originX, float originY, float originZ) {

        // Prevent accidental JSON values from spawning an unreasonable number
        // of particles in a single frame.
        int count = MathUtils.clamp(particleCount, 0, 192);

        if(count <= 0) {
            return;
        }

        float spawnDistance = Math.max(0.05f, particleSpawnDistance);
        float verticalSpread = MathUtils.clamp(particleVerticalSpread, 0f, 1f);

        Vector3 particleDirection = new Vector3();

        for(int i = 0; i < count; i++) {

            float angle = Game.rand.nextFloat() * MathUtils.PI2;

            // A controlled amount of vertical movement keeps the default Nova
            // looking like a radial blast instead of a random particle sphere.
            float vertical =
                ((Game.rand.nextFloat() * 2f) - 1f)
                * verticalSpread;

            float horizontal =
                (float)Math.sqrt(
                    Math.max(0f, 1f - (vertical * vertical))
                );

            particleDirection.set(
                MathUtils.cos(angle) * horizontal,
                MathUtils.sin(angle) * horizontal,
                vertical
            ).nor();

            float px = originX + (particleDirection.x * spawnDistance);
            float py = originY + (particleDirection.y * spawnDistance);
            float pz = originZ + (particleDirection.z * spawnDistance);

            float speedVariation = 0.8f + (Game.rand.nextFloat() * 0.4f);
            float speed = particleSpeed * speedVariation;

            Particle particle = new Particle(
                px,
                py,
                pz,
                particleDirection.x * speed,
                particleDirection.y * speed,
                particleDirection.z * speed,
                particleStartTex,
                spellColor == null ? Color.WHITE : spellColor,
                particleFullbrite
            );

            particle.setSpriteAtlas(particleSpriteAtlas);
            particle.tex = particleStartTex;
            particle.floating = true;
            particle.checkCollision = false;

            particle.lifetime = particleLifetime
                * (0.85f + Game.rand.nextFloat() * 0.3f);

            particle.scale = particleScale;
            particle.startScale = particleScale;
            particle.endScale = particleEndScale;

            // Particle.tick() initializes interpolation state only when this is
            // false. This is the same fix used by the working Cone VFX.
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
