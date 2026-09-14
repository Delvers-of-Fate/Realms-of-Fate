package com.interrupt.dungeoneer.entities.projectiles;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Explosion;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Options;
import com.interrupt.dungeoneer.gfx.GlRenderer;
import com.interrupt.dungeoneer.gfx.animation.SpriteAnimation;
import com.interrupt.dungeoneer.gfx.drawables.DrawableSprite;

public class MagicMissileProjectile extends Projectile {

    private float trailTimer = 2 + (int)(Math.random() * 4);

    /** Leave particle trail? */
    public boolean leaveTrail = true;

    /** Splash force. */
    public float splashForce = 0.1f;

    /** Splash radius. */
    public float splashRadius = 3f;

    /** Splash damage amount. */
    public boolean splashDamage = false;

    /** Explosion particle scalar. */
    public float particleAmoundMod = 1f;

    /** Particle trail spawn interval. */
    public float trailInterval = 0.1f;

    /** Explosion entity. */
    public Explosion explosion = new Explosion();

    /** Light scalar. */
    public float lightMod = 1f;

    /** Trail particle sprite index. */
    public int trailParticleTex = 0;

    /** Trail particle lifetime. */
    public float trailParticleLifetime = 60f;

    /** Trail particle random lifetime. */
    public float trailParticleRandomLifetime = 60f;

    /** Trail particle start scale. */
    public float trailParticleStartScale = 1f;

    /** Trail particle end scale. */
    public float trailParticleEndScale = 0f;

    /** Sprite animation. */
    protected SpriteAnimation animation = null;

    /** End sprite index. */
    @EditorProperty
    public Integer endAnimTex = null;

    /** Projectile sprite animation speed. */
    @EditorProperty
    public Float animSpeed = 30f;

    /** Projectile halo type. */
    public HaloMode haloMode = HaloMode.BOTH;

    /** Movement behavior used by this projectile. */
    public ProjectileMovement movement = ProjectileMovement.STRAIGHT;

    /** Maximum range at which HOMING can acquire a target. */
    public float homingRange = 8f;

    /** Strength of HOMING turning. */
    public float homingStrength = 0.05f;

    /** Allow HOMING projectile to acquire another target if target is lost. */
    public boolean homingCanRetarget = true;

    /** Side-to-side amount used by WAVE. */
    public float waveStrength = 0.04f;

    /** Speed of WAVE oscillation. */
    public float waveSpeed = 0.15f;

    /** Speed change per tick for ACCELERATE / DECELERATE. */
    public float movementAcceleration = 0.002f;

    /** Minimum speed allowed by DECELERATE. */
    public float minimumMovementSpeed = 0.02f;

    /** Maximum speed allowed by ACCELERATE. */
    public float maximumMovementSpeed = 1.0f;

    /* Runtime movement state. */
    private transient Entity homingTarget = null;
    private transient float movementAge = 0f;

    private transient Vector3 waveForward = null;
    private transient Vector3 waveRight = null;


    public MagicMissileProjectile() {
        artType = ArtType.sprite;
        collision.set(0.05f, 0.05f, 0.1f);
        color = new Color(Colors.MAGIC);
        canStepUpOn = false;
        dropSound = "";
    }


    public MagicMissileProjectile(
        float x,
        float y,
        float z,
        float xa,
        float za,
        int damage,
        DamageType damageType,
        Color color,
        Entity owner) {

        super(x, y, z, 8, xa, za, damage, damageType, owner);

        this.z = z + 0.1f;

        isSolid = true;
        floating = true;
        fullbrite = true;

        this.color = color;
        this.damageType = damageType;

        collision.set(0.05f, 0.05f, 0.1f);

        canStepUpOn = false;
    }


    @Override
    public void onTick(float delta) {

        color.a = 1f;

        movementAge += delta;
        updateMovement(delta);

        if(lightMod > 0) {

            com.interrupt.dungeoneer.gfx.DynamicLight light =
                GlRenderer.getLight();

            if(light != null) {
                light.color.set(color.r, color.g, color.b);
                light.position.set(x, z, y);
                light.range *= lightMod;
            }
        }

        int detailLevel =
            Options.instance.graphicsDetailLevel;

        if(leaveTrail) {

            if(trailTimer > 0) {

                trailTimer -= delta;
            }
            else {

                trailTimer =
                    trailInterval
                        + (Game.rand.nextFloat()
                        * trailInterval
                        * 0.5f);

                trailTimer +=
                    (1f - Options.instance.gfxQuality);

                float driftSpeed = 0.001f;
                float driftSpeedHalf = driftSpeed / 2f;

                Particle p = CachePools.getParticle(
                    x - xa
                        + ((Game.rand.nextFloat() - 0.5f) * 0.1f),

                    y - ya
                        + ((Game.rand.nextFloat() - 0.5f) * 0.1f),

                    z + 0.5f
                        - za
                        + yOffset
                        + ((Game.rand.nextFloat() - 0.5f) * 0.1f),

                    (Game.rand.nextFloat() * driftSpeed)
                        - driftSpeedHalf,

                    (Game.rand.nextFloat() * driftSpeed)
                        - driftSpeedHalf,

                    (Game.rand.nextFloat() * driftSpeed)
                        - driftSpeedHalf,

                    this.trailParticleTex,
                    color,
                    fullbrite
                );

                p.floating = true;

                p.lifetime =
                    (int)(
                        this.trailParticleRandomLifetime
                            * Game.rand.nextFloat()
                    )
                        + this.trailParticleLifetime;

                p.scale =
                    this.trailParticleStartScale;

                p.endScale =
                    this.trailParticleEndScale;

                p.roll =
                    Game.rand.nextFloat() * 360f;

                if(detailLevel > 3) {
                    p.haloMode = HaloMode.CORONA_ONLY;
                }

                p.rotateAmount =
                    Game.rand.nextFloat() * 0.5f;

                Game.GetLevel().SpawnNonCollidingEntity(p);
            }
        }

        if(endAnimTex != null && animation == null) {
            playAnimation();
        }

        if(animation != null && animation.playing) {
            animation.animate(delta, this);
        }
    }


    private void updateMovement(float delta) {

        if(movement == null) {
            movement = ProjectileMovement.STRAIGHT;
        }

        switch(movement) {

            case HOMING:
                updateHoming(delta);
                break;

            case WAVE:
                updateWave(delta);
                break;

            case ACCELERATE:
                updateAcceleration(
                    delta,
                    movementAcceleration
                );
                break;

            case DECELERATE:
                updateAcceleration(
                    delta,
                    -movementAcceleration
                );
                break;

            case STRAIGHT:
            default:
                break;
        }
    }


    private void updateAcceleration(
        float delta,
        float acceleration) {

        Vector3 velocity =
            new Vector3(
                xa,
                ya,
                za
            );

        float speed = velocity.len();

        if(speed <= 0.0001f) {
            return;
        }

        float newSpeed =
            speed
                + acceleration * delta;

        if(acceleration > 0f) {

            newSpeed =
                Math.min(
                    newSpeed,
                    maximumMovementSpeed
                );
        }
        else {

            newSpeed =
                Math.max(
                    newSpeed,
                    minimumMovementSpeed
                );
        }

        velocity
            .nor()
            .scl(newSpeed);

        xa = velocity.x;
        ya = velocity.y;
        za = velocity.z;
    }


    private void updateWave(float delta) {

        Vector3 velocity =
            new Vector3(
                xa,
                ya,
                za
            );

        float speed = velocity.len();

        if(speed <= 0.0001f) {
            return;
        }

        if(waveForward == null) {

            waveForward =
                velocity.cpy().nor();

            waveRight =
                new Vector3(
                    -waveForward.y,
                    waveForward.x,
                    0f
                );

            if(waveRight.len2() <= 0.0001f) {
                waveRight.set(
                    1f,
                    0f,
                    0f
                );
            }

            waveRight.nor();
        }

        float wave =
            (float)Math.sin(
                movementAge * waveSpeed
            );

        Vector3 newVelocity =
            waveForward
                .cpy()
                .scl(speed);

        newVelocity.add(
            waveRight
                .cpy()
                .scl(
                    wave * waveStrength
                )
        );

        newVelocity
            .nor()
            .scl(speed);

        xa = newVelocity.x;
        ya = newVelocity.y;
        za = newVelocity.z;
    }


    private void updateHoming(float delta) {

        if(!isValidHomingTarget(homingTarget)) {

            if(homingTarget == null || homingCanRetarget) {
                homingTarget = findHomingTarget();
            }
            else {
                homingTarget = null;
            }
        }

        if(homingTarget == null) {
            return;
        }

        Vector3 currentVelocity =
            new Vector3(
                xa,
                ya,
                za
            );

        float speed =
            currentVelocity.len();

        if(speed <= 0.0001f) {
            return;
        }

        Vector3 desiredDirection =
            new Vector3(
                homingTarget.x - x,
                homingTarget.y - y,
                homingTarget.z - z
            );

        if(desiredDirection.len2() <= 0.0001f) {
            return;
        }

        desiredDirection.nor();

        Vector3 currentDirection =
            currentVelocity
                .cpy()
                .nor();

        float turnAmount =
            Math.max(
                0f,
                Math.min(
                    1f,
                    homingStrength * delta
                )
            );

        currentDirection.lerp(
            desiredDirection,
            turnAmount
        );

        if(currentDirection.len2() <= 0.0001f) {
            return;
        }

        currentDirection
            .nor()
            .scl(speed);

        xa = currentDirection.x;
        ya = currentDirection.y;
        za = currentDirection.z;
    }


    private boolean isValidHomingTarget(Entity target) {

        if(target == null) {
            return false;
        }

        if(!target.isActive) {
            return false;
        }

        if(target == owner) {
            return false;
        }

        float dx =
            target.x - x;

        float dy =
            target.y - y;

        float dz =
            target.z - z;

        float distanceSquared =
            dx * dx
                + dy * dy
                + dz * dz;

        return distanceSquared
            <= homingRange * homingRange;
    }


    private Entity findHomingTarget() {

        Entity bestTarget = null;

        float bestDistanceSquared =
            homingRange * homingRange;

        /*
         * Player-owned projectile:
         * find the nearest Monster.
         */
        if(owner instanceof Player) {

            for(Entity entity : Game.GetLevel().entities) {

                if(!(entity instanceof Monster)) {
                    continue;
                }

                if(!entity.isActive) {
                    continue;
                }

                float dx =
                    entity.x - x;

                float dy =
                    entity.y - y;

                float dz =
                    entity.z - z;

                float distanceSquared =
                    dx * dx
                        + dy * dy
                        + dz * dz;

                if(distanceSquared < bestDistanceSquared) {

                    bestDistanceSquared =
                        distanceSquared;

                    bestTarget =
                        entity;
                }
            }
        }

        /*
         * Monster-owned projectile:
         * home toward the player.
         */
        else if(owner instanceof Monster) {

            Player player =
                Game.instance.player;

            if(player != null && player.isActive) {

                float dx =
                    player.x - x;

                float dy =
                    player.y - y;

                float dz =
                    player.z - z;

                float distanceSquared =
                    dx * dx
                        + dy * dy
                        + dz * dz;

                if(distanceSquared <= bestDistanceSquared) {

                    bestTarget =
                        player;
                }
            }
        }

        return bestTarget;
    }


    @Override
    public void hitEffect() {

        if(!isActive) {
            return;
        }

        explosion.initExplosion(
            x,
            y,
            z + yOffset,
            splashForce,
            splashRadius
        );

        if(explosion.color == null) {
            explosion.color = color;
        }

        explosion.color.a = 1f;

        if(splashDamage) {
            explosion.damage = damage;
        }

        explosion.explodeSound =
            hitSound;

        explosion.explode(
            Game.GetLevel(),
            makeHitParticles
                ? particleAmoundMod
                : 0
        );

        isActive = false;

        makeHitDecal();
    }


    @Override
    public void hit(
        float xa,
        float ya,
        int damage,
        float force,
        DamageType damageType,
        Entity instigator) {

        Game.instance.player.shake(
            4f,
            4f,
            new Vector3(x, y, z)
        );

        if(damageType != DamageType.PHYSICAL) {

            Particle ring =
                new Particle(
                    x,
                    y,
                    z,
                    0,
                    0,
                    0,
                    0,
                    color,
                    true
                );

            ((DrawableSprite)ring.drawable)
                .billboard = false;

            ((DrawableSprite)ring.drawable)
                .dir
                .set(
                    xa,
                    za,
                    ya
                )
                .nor();

            ring.xa = 0;
            ring.ya = 0;
            ring.za = 0;

            ring.artType =
                ArtType.particle;

            ring.tex = 16;

            ring.x = x;
            ring.y = y;
            ring.z = z;

            ring.lifetime = 20;

            ring.startScale = 0.1f;

            ring.fullbrite = true;

            ring.endScale = 8f;

            ring.scale = 0f;

            ring.floating = true;

            ring.yOffset = yOffset;

            ring.checkCollision = false;

            ring.color.set(color);

            ring.isActive = true;

            ring.initialized = false;

            ring.isDynamic = true;

            ring.haloMode =
                HaloMode.CORONA_ONLY;

            Game.instance.level
                .non_collidable_entities
                .add(ring);

            if(this.explosion.damage == 0) {

                super.hit(
                    xa,
                    ya,
                    damage,
                    force,
                    damageType,
                    instigator
                );
            }
        }
        else {

            Vector2 calcVec =
                new Vector2(
                    this.xa,
                    this.ya
                );

            float speed =
                calcVec.len();

            calcVec
                .set(xa, ya)
                .nor();

            this.xa =
                calcVec.x * speed;

            this.ya =
                calcVec.y * speed;

            if(xa == 0 && ya == 0) {

                super.hit(
                    xa,
                    ya,
                    damage,
                    force,
                    damageType,
                    instigator
                );
            }
        }
    }


    public void playAnimation() {

        animation =
            new SpriteAnimation(
                tex,
                endAnimTex,
                animSpeed,
                null
            );

        animation.loop();
        animation.randomizeTime();
    }


    @Override
    public void onDestroy() {
    }


    @Override
    public HaloMode getHaloMode() {

        if(haloMode != null) {
            return haloMode;
        }

        return HaloMode.BOTH;
    }
}
