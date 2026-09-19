package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

/**
 * Persistent cloud spell.
 *
 * Cloud is the spell definition. Casting it creates a CloudEffect runtime
 * entity that remains in the level, periodically damages targets, applies an
 * optional status effect, and continuously emits particles.
 */
public class Cloud extends Spell {

    /** Maximum distance from the caster where the cloud is placed. */
    public float castDistance = 5f;

    /** Distance between obstruction checks while finding the cast point. */
    public float placementStep = 0.1f;

    /** Keep the cloud this far back from the first blocked point. */
    public float placementPadding = 0.15f;

    /** Snap the cloud volume to the floor at its final X/Y position. */
    public boolean snapToFloor = true;

    /** Horizontal radius of the cloud. */
    public float radius = 2.5f;

    /** Total vertical height of the cloud volume. */
    public float cloudHeight = 1.5f;

    /**
     * Cloud lifetime in Delver gameplay ticks.
     * Approximately 60 gameplay ticks = 1 real-time second.
     */
    public float duration = 300f;

    /**
     * Time between damage pulses in Delver gameplay ticks.
     * 30 = approximately two damage pulses per second.
     */
    public float damageTickRate = 30f;

    /** Apply one damage pulse as soon as the CloudEffect starts. */
    public boolean damageImmediately = true;

    /** Horizontal knockback applied away from the cloud center. */
    public float knockback = 0f;

    /** Require clear line of sight from the cloud to a target. */
    public boolean requiresLineOfSight = true;

    /** Chance from 0.0 to 1.0 to apply applyStatusEffect on a damage pulse. */
    public float statusEffectChance = 1f;

    /**
     * Preserve normal spell allegiance automatically:
     * Player caster -> monsters, Monster caster -> player.
     * Neutral/environmental owners use the explicit damage flags below.
     */
    public boolean autoTargetByOwner = true;

    /** Allow the cloud to damage Player targets. */
    public boolean damagePlayers = true;

    /** Allow the cloud to damage Monster targets. */
    public boolean damageMonsters = true;

    /** Allow the cloud to damage other living Actor types. */
    public boolean damageOtherActors = false;


    // ---------------------------------------------------------------------
    // PARTICLES
    // ---------------------------------------------------------------------

    /** Continuously emit cloud particles while the effect is active. */
    public boolean showCloudParticles = true;

    /** Sprite atlas used by cloud particles. */
    public String particleSpriteAtlas = "particle";

    /** First particle texture frame. */
    public int particleStartTex = 8;

    /** Last particle texture frame. Same as start for a static sprite. */
    public int particleEndTex = 12;

    /** Particle animation speed. */
    public float particleAnimSpeed = 35f;

    /** Number of particles emitted on each particle pulse. */
    public int particlesPerPulse = 5;

    /** Time between particle pulses in Delver gameplay ticks. */
    public float particleInterval = 4f;

    /** Random horizontal particle drift speed. */
    public float particleSpeed = 0.01f;

    /** Particle lifetime in Delver gameplay ticks. */
    public float particleLifetime = 45f;

    /** Initial particle scale. */
    public float particleScale = 0.7f;

    /** Final particle scale. */
    public float particleEndScale = 0.25f;

    /** Make particles full-bright. */
    public boolean particleFullbrite = true;


    public Cloud() {
        // CloudEffect owns the sustained visual effect.
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

        // Player spells should originate from the first-person camera so the
        // cloud placement matches the crosshair.
        if(Game.instance != null
            && owner == Game.instance.player
            && Game.camera != null) {

            originX = Game.camera.position.x;
            originY = Game.camera.position.z;
            originZ = Game.camera.position.y;
        }
        else if(owner instanceof Monster) {
            // Monster spell positions originate near their feet.
            originZ += ((Monster)owner).projectileOffset;
        }

        // Player casts use castDistance directly. Monster casts treat
        // castDistance as a maximum and stop at the player's actual distance
        // when the player is closer, preventing the cloud from overshooting.
        float effectiveCastDistance = Math.max(0.1f, castDistance);

        if(owner instanceof Monster
            && Game.instance != null
            && Game.instance.player != null) {

            Player player = Game.instance.player;

            float targetX = player.x;
            float targetY = player.y;
            float targetZ = player.z + (player.collision.z * 0.5f);

            float dx = targetX - originX;
            float dy = targetY - originY;
            float dz = targetZ - originZ;

            float targetDistance = (float)Math.sqrt(
                (dx * dx) + (dy * dy) + (dz * dz)
            );

            if(targetDistance > 0.01f) {
                effectiveCastDistance = Math.min(
                    effectiveCastDistance,
                    targetDistance
                );
            }
        }

        Vector3 cloudPosition = findCloudPosition(
            level,
            castDirection,
            originX,
            originY,
            originZ,
            effectiveCastDistance
        );

        CloudEffect effect = new CloudEffect();

        effect.x = cloudPosition.x;
        effect.y = cloudPosition.y;
        effect.z = cloudPosition.z;
        effect.owner = owner;

        effect.radius = Math.max(0.05f, radius);
        effect.cloudHeight = Math.max(0.05f, cloudHeight);
        effect.duration = Math.max(0f, duration);
        effect.damageTickRate = Math.max(0.01f, damageTickRate);
        effect.damageImmediately = damageImmediately;

        effect.baseDamage = baseDamage;
        effect.randDamage = randDamage;
        effect.damageMultiplier = getCastDamageMultiplier();
        effect.damageType = damageType;
        effect.knockback = knockback;
        effect.requiresLineOfSight = requiresLineOfSight;
        effect.statusEffectChance = statusEffectChance;
        effect.autoTargetByOwner = autoTargetByOwner;
        effect.damagePlayers = damagePlayers;
        effect.damageMonsters = damageMonsters;
        effect.damageOtherActors = damageOtherActors;

        if(applyStatusEffect != null) {
            effect.applyStatusEffect =
                (StatusEffect)KryoSerializer.copyObject(applyStatusEffect);
        }

        effect.showCloudParticles = showCloudParticles;
        effect.particleSpriteAtlas = particleSpriteAtlas;
        effect.particleStartTex = particleStartTex;
        effect.particleEndTex = particleEndTex;
        effect.particleAnimSpeed = particleAnimSpeed;
        effect.particlesPerPulse = particlesPerPulse;
        effect.particleInterval = particleInterval;
        effect.particleSpeed = particleSpeed;
        effect.particleLifetime = particleLifetime;
        effect.particleScale = particleScale;
        effect.particleEndScale = particleEndScale;
        effect.particleFullbrite = particleFullbrite;
        effect.cloudColor = spellColor == null
            ? new Color(Color.WHITE)
            : new Color(spellColor);

        // CloudEffect has no physical collision and manages its own lifecycle.
        level.SpawnNonCollidingEntity(effect);
    }


    /**
     * Walk along the aim ray until the desired distance or an obstruction is
     * reached. The final cloud is placed at the last visible point.
     */
    private Vector3 findCloudPosition(Level level,
                                      Vector3 direction,
                                      float originX,
                                      float originY,
                                      float originZ,
                                      float maxPlacementDistance) {

        float safeDistance = Math.max(0.1f, maxPlacementDistance);
        float safeStep = MathUtils.clamp(placementStep, 0.05f, 0.5f);
        float padding = Math.max(0f, placementPadding);

        float lastDistance = 0f;
        boolean hitObstruction = false;

        for(float travelled = safeStep;
            travelled <= safeDistance + 0.0001f;
            travelled += safeStep) {

            float checkDistance = Math.min(travelled, safeDistance);

            float testX = originX + (direction.x * checkDistance);
            float testY = originY + (direction.z * checkDistance);
            float testZ = originZ + (direction.y * checkDistance);

            boolean visible =
                level.canSeeIncludingDoors(
                    originX,
                    originY,
                    testX,
                    testY,
                    safeDistance + 1f
                )
                && level.canSee3D(
                    originX,
                    originY,
                    originZ,
                    testX,
                    testY,
                    testZ
                );

            if(!visible) {
                hitObstruction = true;
                break;
            }

            lastDistance = checkDistance;

            if(checkDistance >= safeDistance) {
                break;
            }
        }

        if(hitObstruction) {
            lastDistance = Math.max(0f, lastDistance - padding);
        }

        float cloudX = originX + (direction.x * lastDistance);
        float cloudY = originY + (direction.z * lastDistance);
        float cloudZ = originZ + (direction.y * lastDistance);

        if(snapToFloor) {
            float halfHeight = Math.max(0.05f, cloudHeight) * 0.5f;
            float floorHeight = level.maxFloorHeight(
                cloudX,
                cloudY,
                cloudZ,
                0.05f
            );

            // Delver actors stand at floorHeight + 0.5. Treat that as the
            // bottom of the cloud volume and center the cloud above it.
            cloudZ = floorHeight + 0.5f + halfHeight;

            // Avoid placing the center above a very low ceiling.
            float ceilingHeight = level.minCeilHeight(
                cloudX,
                cloudY,
                cloudZ,
                0.05f
            );

            float maxCenter = ceilingHeight - halfHeight;
            if(cloudZ > maxCenter) {
                cloudZ = maxCenter;
            }
        }

        return new Vector3(cloudX, cloudY, cloudZ);
    }
}
