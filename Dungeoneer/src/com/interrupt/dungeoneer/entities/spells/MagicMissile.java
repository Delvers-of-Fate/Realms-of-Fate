package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Explosion;
import com.interrupt.dungeoneer.entities.Monster;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.gfx.Material;
import com.interrupt.managers.EntityManager;

public class MagicMissile extends Spell {

    /** Sound to play on hit. */
    public String hitSound = null;

    /** Spell sprite. */
    public Material appearance = null;

    /** Spell explosion. */
    public Explosion explosion = null;

    /** Spell projectile speed. */
    public float speed = 0.17f;

    /** Particle trail spawn interval. */
    public float trailInterval = 1f;

    /** Force of spell splash damage. */
    public float splashForce = 0.1f;

    /** Radius of spell splash. */
    public float splashRadius = 3f;

    /** Does spell cause splash damage? */
    public boolean splashDamage = false;

    /** Does spell projectile float? */
    public boolean floating = true;

    /** Spell aim accuracy. */
    public float shotAccuracy = 1.0f;

    /** Spell projectile prefab. */
    public MagicMissileProjectile magicMissileProjectile = null;


    // ------------------------------------------------------------------------
    // FIRE PATTERN
    // ------------------------------------------------------------------------

    /** Pattern used by a normal cast. */
    public FirePattern firePattern = FirePattern.SINGLE;

    /**
     * Pattern used by a fully charged cast.
     * null = use normal firePattern.
     */
    public FirePattern chargedFirePattern = null;

    /** Projectile count for a normal cast. */
    public int projectileCount = 1;

    /** Projectile count for a fully charged cast. */
    public int chargedProjectileCount = 1;

    /** Total horizontal spread in degrees. */
    public float projectileSpread = 0f;

    /** Total horizontal spread for charged casts. */
    public float chargedProjectileSpread = 0f;


    // ------------------------------------------------------------------------
    // TIMED PATTERNS
    // ------------------------------------------------------------------------

    /**
     * Delay in seconds between projectiles for:
     *
     * VOLLEY
     * BURST
     * ALTERNATING
     * SPIRAL
     */
    public float projectileDelay = 0.1f;

    /**
     * Charged version of projectileDelay.
     *
     * -1 = use normal projectileDelay.
     */
    public float chargedProjectileDelay = -1f;

    /**
     * Degrees rotated between each SPIRAL projectile.
     */
    public float patternAngleStep = 30f;

    /**
     * Charged version of patternAngleStep.
     *
     * -1 = use normal patternAngleStep.
     */
    public float chargedPatternAngleStep = -1f;


    public MagicMissile() {
    }


    // ------------------------------------------------------------------------
    // CURRENT CAST SETTINGS
    // ------------------------------------------------------------------------

    private FirePattern getCurrentFirePattern() {

        if(canCharge
            && isChargedCast
            && chargedFirePattern != null) {

            return chargedFirePattern;
        }

        if(firePattern == null) {
            return FirePattern.SINGLE;
        }

        return firePattern;
    }


    private int getCurrentProjectileCount() {

        int count = projectileCount;

        if(canCharge && isChargedCast) {
            count = chargedProjectileCount;
        }

        // Safety limit so bad mod data cannot accidentally
        // create thousands of entities in one cast.
        if(count < 1) {
            count = 1;
        }

        if(count > 64) {
            count = 64;
        }

        return count;
    }


    private float getCurrentProjectileSpread() {

        if(canCharge && isChargedCast) {
            return chargedProjectileSpread;
        }

        return projectileSpread;
    }


    private float getCurrentProjectileDelay() {

        if(canCharge
            && isChargedCast
            && chargedProjectileDelay >= 0f) {

            return chargedProjectileDelay;
        }

        return projectileDelay;
    }


    private float getCurrentPatternAngleStep() {

        if(canCharge
            && isChargedCast
            && chargedPatternAngleStep >= 0f) {

            return chargedPatternAngleStep;
        }

        return patternAngleStep;
    }


    // ------------------------------------------------------------------------
    // CAST
    // ------------------------------------------------------------------------

    @Override
    public void doCast(
        Entity owner,
        Vector3 direction,
        Vector3 position) {

        FirePattern pattern = getCurrentFirePattern();

        int count = getCurrentProjectileCount();

        float spread = getCurrentProjectileSpread();

        if(pattern == null) {
            pattern = FirePattern.SINGLE;
        }

        switch(pattern) {

            case SPREAD:
                castSpread(
                    owner,
                    position,
                    direction,
                    count,
                    spread);
                break;


            case RING:
                castRing(
                    owner,
                    position,
                    direction,
                    count);
                break;


            case CROSS:
                castCross(
                    owner,
                    position,
                    direction,
                    count);
                break;


            case RANDOM:
                castRandom(
                    owner,
                    position,
                    direction,
                    count,
                    spread);
                break;


            case VOLLEY:
            case BURST:
            case ALTERNATING:
            case SPIRAL:

                castTimedPattern(
                    owner,
                    position,
                    direction,
                    pattern,
                    count,
                    spread);
                break;


            case SINGLE:
            default:

                spawnProjectile(
                    owner,
                    position,
                    direction);

                break;
        }
    }


    // ------------------------------------------------------------------------
    // SINGLE PROJECTILE
    // ------------------------------------------------------------------------

    private void spawnProjectile(
        Entity owner,
        Vector3 position,
        Vector3 direction) {

        int damage = doAttackRoll();

        spawnProjectileWithDamage(
            owner,
            position,
            direction,
            damage);
    }


    /**
     * Package-private because ProjectileSequence uses this.
     *
     * Damage is supplied explicitly because timed patterns must roll
     * their damage while the charged-cast state is still active.
     */
    void spawnProjectileWithDamage(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int damage) {

        Vector3 projectileDirection = direction.cpy();

        applyAccuracy(projectileDirection);

        MagicMissileProjectile projectile =
            makeProjectile(
                position,
                projectileDirection,
                damage,
                owner);

        Game.GetLevel().entities.add(projectile);
    }


    // ------------------------------------------------------------------------
    // ACCURACY
    // ------------------------------------------------------------------------

    private void applyAccuracy(Vector3 direction) {

        if(Math.abs(shotAccuracy) >= 1.0f) {
            return;
        }

        Vector3 axis = direction.cpy();

        direction.rotate(
            Game.rand.nextFloat()
                * (1.0f - Math.abs(shotAccuracy))
                * 45f,
            0f,
            1f,
            0f);

        direction.rotate(
            axis,
            Game.rand.nextFloat() * 360f);
    }


    // ------------------------------------------------------------------------
    // SPREAD
    // ------------------------------------------------------------------------

    private void castSpread(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int count,
        float spread) {

        if(count <= 1) {

            spawnProjectile(
                owner,
                position,
                direction);

            return;
        }

        for(int i = 0; i < count; i++) {

            float t =
                (float)i
                    / (float)(count - 1);

            float angle =
                (-spread * 0.5f)
                    + (spread * t);

            Vector3 shotDirection =
                direction.cpy();

            shotDirection.rotate(
                angle,
                0f,
                1f,
                0f);

            spawnProjectile(
                owner,
                position,
                shotDirection);
        }
    }


    // ------------------------------------------------------------------------
    // RING
    // ------------------------------------------------------------------------

    private void castRing(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int count) {

        if(count <= 1) {

            spawnProjectile(
                owner,
                position,
                direction);

            return;
        }

        float angleStep =
            360f / (float)count;

        for(int i = 0; i < count; i++) {

            Vector3 shotDirection =
                direction.cpy();

            shotDirection.rotate(
                angleStep * i,
                0f,
                1f,
                0f);

            spawnProjectile(
                owner,
                position,
                shotDirection);
        }
    }


    // ------------------------------------------------------------------------
    // CROSS
    // ------------------------------------------------------------------------

    private void castCross(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int count) {

        /*
         * CROSS divides shots evenly around the caster.
         *
         * 4 shots:
         *
         *       ^
         *       |
         *   <---+--->
         *       |
         *       v
         *
         * Higher counts create additional evenly spaced directions.
         */

        if(count < 4) {
            count = 4;
        }

        float angleStep =
            360f / (float)count;

        for(int i = 0; i < count; i++) {

            Vector3 shotDirection =
                direction.cpy();

            shotDirection.rotate(
                angleStep * i,
                0f,
                1f,
                0f);

            spawnProjectile(
                owner,
                position,
                shotDirection);
        }
    }


    // ------------------------------------------------------------------------
    // RANDOM
    // ------------------------------------------------------------------------

    private void castRandom(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int count,
        float spread) {

        for(int i = 0; i < count; i++) {

            Vector3 shotDirection =
                direction.cpy();

            float angle =
                (Game.rand.nextFloat() - 0.5f)
                    * spread;

            shotDirection.rotate(
                angle,
                0f,
                1f,
                0f);

            spawnProjectile(
                owner,
                position,
                shotDirection);
        }
    }


    // ------------------------------------------------------------------------
    // TIMED PATTERNS
    // ------------------------------------------------------------------------

    private void castTimedPattern(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        FirePattern pattern,
        int count,
        float spread) {

        /*
         * IMPORTANT:
         *
         * Wand resets isChargedCast immediately after zap().
         *
         * Because delayed shots occur later, their damage must be
         * rolled NOW while the charged state is still active.
         */

        int[] damageRolls =
            new int[count];

        for(int i = 0; i < count; i++) {
            damageRolls[i] = doAttackRoll();
        }

        ProjectileSequence sequence =
            new ProjectileSequence(
                this,
                owner,
                position,
                direction,
                pattern,
                count,
                spread,
                getCurrentPatternAngleStep(),
                getCurrentProjectileDelay(),
                damageRolls);

        Game.GetLevel()
            .SpawnNonCollidingEntity(sequence);
    }


    // ------------------------------------------------------------------------
    // PROJECTILE CREATION
    // ------------------------------------------------------------------------

    protected MagicMissileProjectile makeProjectile(
        Vector3 position,
        Vector3 direction,
        int dmg,
        Entity owner) {

        Player p = Game.instance.player;

        float xOffset =
            (owner == p) ? 0f : 0f;

        float yOffset =
            (owner == p) ? 0f : 0f;

        float zOffset =
            (owner == p) ? 0f : 0.31f;

        MagicMissileProjectile projectile;

        if(magicMissileProjectile == null) {

            // Legacy Delver projectile setup
            projectile =
                new MagicMissileProjectile(
                    position.x + xOffset,
                    position.y + yOffset,
                    position.z + zOffset,
                    direction.x * speed,
                    direction.z * speed,
                    dmg,
                    damageType,
                    new Color(spellColor),
                    owner);

            if(explosion != null) {
                projectile.explosion = explosion;
            }

            projectile.trailInterval =
                trailInterval;

            projectile.splashForce =
                splashForce;

            projectile.splashRadius =
                splashRadius;

            projectile.splashDamage =
                splashDamage;

            projectile.floating =
                floating;

            if(appearance != null) {

                projectile.spriteAtlas =
                    appearance.texAtlas;

                projectile.tex =
                    appearance.tex;
            }
        }

        else {

            // Modern data-driven projectile setup
            projectile =
                (MagicMissileProjectile)
                    EntityManager.instance.Copy(
                        magicMissileProjectile);

            projectile.x =
                position.x + xOffset;

            projectile.y =
                position.y + yOffset;

            projectile.z =
                position.z
                    + zOffset
                    + 0.1f;

            projectile.xa =
                direction.x * speed;

            projectile.ya =
                direction.z * speed;

            projectile.owner =
                owner;

            projectile.damage =
                dmg;

            projectile.damageType =
                damageType;

            if(spellColor != null) {
                projectile.color = spellColor;
            }
        }

        // Offset monster projectiles vertically.
        if(owner instanceof Monster) {

            projectile.z +=
                ((Monster)owner)
                    .projectileOffset;
        }

        projectile.za =
            direction.y * speed;

        if(hitSound != null) {
            projectile.hitSound =
                hitSound;
        }

        return projectile;
    }
}
