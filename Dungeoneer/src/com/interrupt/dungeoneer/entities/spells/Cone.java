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
 * Instant cone-shaped spell.
 *
 * Damage is applied immediately to valid targets inside the cone.
 * Particles are visual only and are generated from the same cone shape.
 */
public class Cone extends Spell {

    /** Maximum distance the cone can reach. */
    public float range = 5f;

    /** Full cone angle in degrees. */
    public float coneAngle = 60f;

    /** Horizontal knockback applied to hit targets. */
    public float knockback = 0.15f;

    /** Require clear line of sight before damaging a target. */
    public boolean requiresLineOfSight = true;

    /** Chance from 0.0 to 1.0 to apply applyStatusEffect. */
    public float statusEffectChance = 1f;

    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    /** Spawn cone particles when cast. */
    public boolean showConeParticles = true;

    /** Sprite atlas used by cone particles. */
    public String particleSpriteAtlas = "particle";

    /** First particle texture frame. */
    public int particleStartTex = 8;

    /** Last particle texture frame. Same as start for a static sprite. */
    public int particleEndTex = 12;

    /** Particle animation speed. */
    public float particleAnimSpeed = 35f;

    /** Number of particles spawned per cast. */
    public int particleCount = 32;

    /** Particle travel speed. */
    public float particleSpeed = 0.06f;

    /** Particle lifetime in Delver ticks. */
    public float particleLifetime = 28f;

    /** Initial particle scale. */
    public float particleScale = 0.55f;

    /** Final particle scale. */
    public float particleEndScale = 0.15f;

    /** Distance in front of the caster where particles begin. */
    public float particleSpawnDistance = 0.35f;

    /** Make particles full-bright. */
    public boolean particleFullbrite = true;

    public Cone() {
        // Cone creates its own particles.
        doCastVfx = false;
    }

    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {
        if(owner == null || direction == null || position == null) {
            return;
        }

        Level level = Game.GetLevel();
        if(level == null) {
            return;
        }

        Vector3 castDirection = direction.cpy();
        if(castDirection.isZero()) {
            return;
        }
        castDirection.nor();

        float originX = position.x;
        float originY = position.y;
        float originZ = position.z;

        // For the local player, always use the camera origin. This keeps the
        // cone VFX directly in front of the first-person view and also makes
        // targeting line up with the crosshair.
        if(owner == Game.instance.player && Game.camera != null) {
            originX = Game.camera.position.x;
            originY = Game.camera.position.z;
            originZ = Game.camera.position.y;
        }
        else if(owner instanceof Monster) {
            // Monster spell positions originate near their feet, so raise the
            // origin to the monster's configured projectile height.
            originZ += ((Monster)owner).projectileOffset;
        }

        damageTargets(level, owner, castDirection, originX, originY, originZ);

        if(showConeParticles) {
            spawnConeParticles(level, castDirection, originX, originY, originZ);
        }
    }

    // ---------------------------------------------------------------------
    // DAMAGE
    // ---------------------------------------------------------------------

    private void damageTargets(Level level, Entity owner, Vector3 castDirection,
                               float originX, float originY, float originZ) {

        float safeRange = Math.max(0.01f, range);
        float halfAngle = MathUtils.clamp(coneAngle * 0.5f, 0f, 180f);
        float minimumDot = MathUtils.cosDeg(halfAngle);

        Array<Entity> candidates = new Array<Entity>();
        candidates.addAll(level.spatialhash.getEntitiesAt(originX, originY, safeRange));

        Array<Entity> staticCandidates =
            level.staticSpatialhash.getEntitiesAt(originX, originY, safeRange);

        for(Entity entity : staticCandidates) {
            if(!candidates.contains(entity, true)) {
                candidates.add(entity);
            }
        }

        if(owner instanceof Monster
            && Game.instance != null
            && Game.instance.player != null) {

            Player player = Game.instance.player;
            if(!candidates.contains(player, true)) {
                candidates.add(player);
            }
        }

        Vector3 toTarget = new Vector3();
        Vector3 hitDirection = new Vector3();

        for(Entity target : candidates) {
            if(!isValidTarget(owner, target)) {
                continue;
            }

            float targetZ = target.z + (target.collision.z * 0.5f);

            // Spell direction layout is X = world X, Y = vertical Z,
            // Z = world Y.
            toTarget.set(
                target.x - originX,
                targetZ - originZ,
                target.y - originY
            );

            float distance = toTarget.len();
            if(distance <= 0.001f || distance > safeRange) {
                continue;
            }

            toTarget.scl(1f / distance);

            if(castDirection.dot(toTarget) < minimumDot) {
                continue;
            }

            if(requiresLineOfSight
                && !hasLineOfSight(level,
                    originX, originY, originZ,
                    target.x, target.y, targetZ,
                    safeRange)) {
                continue;
            }

            int damage = doAttackRoll();

            hitDirection.set(
                target.x - originX,
                target.y - originY,
                0f
            );

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

    private boolean isValidTarget(Entity owner, Entity target) {
        if(target == null || target == owner || !target.isActive) {
            return false;
        }

        if(owner instanceof Player) {
            return target instanceof Monster && ((Monster)target).isAlive();
        }

        if(owner instanceof Monster) {
            return target instanceof Player && ((Player)target).isAlive();
        }

        return target instanceof Actor && ((Actor)target).isAlive();
    }

    private boolean hasLineOfSight(Level level,
                                   float x1, float y1, float z1,
                                   float x2, float y2, float z2,
                                   float maxDistance) {
        return level.canSeeIncludingDoors(x1, y1, x2, y2, maxDistance + 1f)
            && level.canSee3D(x1, y1, z1, x2, y2, z2);
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

    private void spawnConeParticles(Level level, Vector3 castDirection,
                                    float originX, float originY, float originZ) {

        int count = MathUtils.clamp(particleCount, 0, 128);
        if(count <= 0) {
            return;
        }

        float halfAngle = MathUtils.clamp(coneAngle * 0.5f, 0f, 179f);
        float cosMaxAngle = MathUtils.cosDeg(halfAngle);

        Vector3 forward = castDirection.cpy().nor();
        Vector3 right = forward.cpy().crs(Vector3.Y);

        if(right.len2() < 0.0001f) {
            right.set(Vector3.X);
        }
        else {
            right.nor();
        }

        Vector3 up = right.cpy().crs(forward).nor();
        Vector3 particleDirection = new Vector3();

        for(int i = 0; i < count; i++) {
            float cosTheta = MathUtils.lerp(
                cosMaxAngle,
                1f,
                Game.rand.nextFloat()
            );

            float sinTheta = (float)Math.sqrt(
                Math.max(0f, 1f - (cosTheta * cosTheta))
            );

            float phi = Game.rand.nextFloat() * MathUtils.PI2;
            float cosPhi = MathUtils.cos(phi);
            float sinPhi = MathUtils.sin(phi);

            particleDirection
                .set(forward)
                .scl(cosTheta)
                .mulAdd(right, cosPhi * sinTheta)
                .mulAdd(up, sinPhi * sinTheta)
                .nor();

            float spawnDistance = Math.max(0.05f, particleSpawnDistance);

            float px = originX + (particleDirection.x * spawnDistance);
            float py = originY + (particleDirection.z * spawnDistance);
            float pz = originZ + (particleDirection.y * spawnDistance);

            float speedVariation = 0.8f + (Game.rand.nextFloat() * 0.4f);
            float speed = particleSpeed * speedVariation;

            Particle particle = new Particle(
                px,
                py,
                pz,
                particleDirection.x * speed,
                particleDirection.z * speed,
                particleDirection.y * speed,
                particleStartTex,
                spellColor == null ? Color.WHITE : spellColor,
                particleFullbrite
            );

            particle.setSpriteAtlas(particleSpriteAtlas);
            particle.tex = particleStartTex;
            particle.floating = true;
            particle.checkCollision = false;
            particle.lifetime = particleLifetime *
                (0.85f + Game.rand.nextFloat() * 0.3f);

            particle.scale = particleScale;
            particle.startScale = particleScale;
            particle.endScale = particleEndScale;

            // IMPORTANT: Particle.tick() only initializes starttime/startScale
            // when initialized is false. Without this, a changing scale can
            // divide by zero on the first tick and the particle disappears.
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
