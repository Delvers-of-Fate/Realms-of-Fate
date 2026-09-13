package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.*;
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
    float speed = 0.17f;

	/** Particle trail spawn interval. */
	float trailInterval = 1f;

	/** Force of spell splash damage. */
	float splashForce = 0.1f;

	/** Radius of spell splash. */
	float splashRadius = 3f;

	/** Does spell cause splash damage? */
	public boolean splashDamage = false;

	/** Does spell projectile float? */
	boolean floating = true;

	/** Spell aim accuracy. */
	float shotAccuracy = 1.0f;

    /** Firing pattern used by a normal cast. */
    public FirePattern firePattern = FirePattern.SINGLE;

    /**
     * Firing pattern used when fully charged.
     * Null means use firePattern.
     */
    public FirePattern chargedFirePattern = null;

    /** Number of projectiles fired by a normal cast. */
    public int projectileCount = 1;

    /** Number of projectiles fired by a fully charged cast. */
    public int chargedProjectileCount = 1;

    /** Horizontal spread in degrees for a normal cast. */
    public float projectileSpread = 0f;

    /** Horizontal spread in degrees for a fully charged cast. */
    public float chargedProjectileSpread = 0f;

    /**
     * Delay in seconds between shots for timed fire patterns.
     */
    public float projectileDelay = 0.1f;

    /**
     * Charged delay between shots.
     * Set below 0 to use projectileDelay.
     */
    public float chargedProjectileDelay = -1f;

    /**
     * Rotation in degrees between shots when using SPIRAL.
     */
    public float patternAngleStep = 30f;

    /**
     * Charged rotation between SPIRAL shots.
     * Set below 0 to use patternAngleStep.
     */
    public float chargedPatternAngleStep = -1f;

	/** Spell projectile. */
    public MagicMissileProjectile magicMissileProjectile = null;

    public MagicMissile() { }

    private FirePattern getCurrentFirePattern() {
        if(canCharge && isChargedCast && chargedFirePattern != null) {
            return chargedFirePattern;
        }

        return firePattern;
    }

    private int getCurrentProjectileCount() {

        int count;

        if(canCharge && isChargedCast) {
            count = chargedProjectileCount;
        }
        else {
            count = projectileCount;
        }

        return Math.max(1, Math.min(count, 64));
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

    private void castTimedPattern(
        Entity owner,
        Vector3 direction,
        Vector3 position,
        FirePattern pattern,
        int count,
        float spread) {

        float delay = getCurrentProjectileDelay();
        float angleStep = getCurrentPatternAngleStep();

        int[] damageRolls = new int[count];

        // Roll damage now so charged damage is preserved even after
        // Wand resets isChargedCast.
        for(int i = 0; i < count; i++) {
            damageRolls[i] = doAttackRoll();
        }

        ProjectileSequence sequence =
            new ProjectileSequence(
                this,
                owner,
                position.cpy(),
                direction.cpy(),
                pattern,
                count,
                spread,
                angleStep,
                delay,
                damageRolls
            );

        Game.GetLevel().SpawnNonCollidingEntity(sequence);
    }

    private void spawnProjectile(
        Entity owner,
        Vector3 position,
        Vector3 direction) {

        spawnProjectileWithDamage(
            owner,
            position,
            direction,
            doAttackRoll()
        );
    }

    /**
     * Spawns a projectile using damage that has already been calculated.
     *
     * Package-private so ProjectileSequence can use it.
     */
    void spawnProjectileWithDamage(
        Entity owner,
        Vector3 position,
        Vector3 direction,
        int damage) {

        Vector3 projectileDirection = direction.cpy();

        // Keep Delver's existing accuracy behavior.
        if(Math.abs(shotAccuracy) < 1.0f) {

            Vector3 axis = projectileDirection.cpy();

            projectileDirection.rotate(
                Game.rand.nextFloat()
                    * (1.0f - Math.abs(shotAccuracy))
                    * 45f,
                0f,
                1f,
                0f
            );

            projectileDirection.rotate(
                axis,
                Game.rand.nextFloat() * 360f
            );
        }

        MagicMissileProjectile projectile =
            makeProjectile(
                position,
                projectileDirection,
                damage,
                owner
            );

        Game.GetLevel().entities.add(projectile);
    }

    @Override
    public void doCast(Entity owner, Vector3 direction, Vector3 position) {

        FirePattern pattern = getCurrentFirePattern();
        int count = getCurrentProjectileCount();
        float spread = getCurrentProjectileSpread();

        switch(pattern) {

            case SPREAD:
                castSpread(
                    owner,
                    direction,
                    position,
                    count,
                    spread
                );
                break;

            case RING:
                castRing(
                    owner,
                    direction,
                    position,
                    count
                );
                break;

            case CROSS:
                castCross(
                    owner,
                    direction,
                    position
                );
                break;

            case RANDOM:
                castRandom(
                    owner,
                    direction,
                    position,
                    count,
                    spread
                );
                break;

            case VOLLEY:
            case BURST:
            case ALTERNATING:
            case SPIRAL:
                castTimedPattern(
                    owner,
                    direction,
                    position,
                    pattern,
                    count,
                    spread
                );
                break;

            case SINGLE:
            default:
                spawnProjectile(
                    owner,
                    position,
                    direction
                );
                break;
        }
    }

    private void castRing(
        Entity owner,
        Vector3 direction,
        Vector3 position,
        int count) {

        float angleStep = 360f / count;

        for(int i = 0; i < count; i++) {
            Vector3 shotDirection = direction.cpy();

            shotDirection.y = 0f;

            if(shotDirection.len2() <= 0.0001f) {
                shotDirection.set(1f, 0f, 0f);
            }

            shotDirection.nor();
            shotDirection.rotate(i * angleStep, 0f, 1f, 0f);

            spawnProjectile(owner, position, shotDirection);
        }
    }

    private void castCross(
        Entity owner,
        Vector3 direction,
        Vector3 position) {

        for(int i = 0; i < 4; i++) {
            Vector3 shotDirection = direction.cpy();

            shotDirection.y = 0f;

            if(shotDirection.len2() <= 0.0001f) {
                shotDirection.set(1f, 0f, 0f);
            }

            shotDirection.nor();
            shotDirection.rotate(i * 90f, 0f, 1f, 0f);

            spawnProjectile(owner, position, shotDirection);
        }
    }

    private void castRandom(
        Entity owner,
        Vector3 direction,
        Vector3 position,
        int count,
        float spread) {

        for(int i = 0; i < count; i++) {
            Vector3 shotDirection = direction.cpy();

            float angle =
                (Game.rand.nextFloat() * spread)
                    - (spread * 0.5f);

            shotDirection.rotate(angle, 0f, 1f, 0f);

            spawnProjectile(owner, position, shotDirection);
        }
    }

    private void castSpread(
        Entity owner,
        Vector3 direction,
        Vector3 position,
        int count,
        float spread) {

        if(count <= 1) {
            spawnProjectile(owner, position, direction);
            return;
        }

        for(int i = 0; i < count; i++) {
            Vector3 shotDirection = direction.cpy();

            float t = (float)i / (float)(count - 1);
            float angle = (-spread * 0.5f) + (spread * t);

            shotDirection.rotate(angle, 0f, 1f, 0f);

            spawnProjectile(owner, position, shotDirection);
        }
    }

	// Make the projectile to fire
	protected MagicMissileProjectile makeProjectile(Vector3 position, Vector3 direction, int dmg, Entity owner) {

		Player p = Game.instance.player;
		float xOffset = (owner == p) ? 0f : 0f;
		float yOffset = (owner == p) ? 0f : 0f;
		float zOffset = (owner == p) ? 0f : 0.31f;

		MagicMissileProjectile projectile;

		if(magicMissileProjectile == null) {

			// Old and busted way
			projectile = new MagicMissileProjectile(position.x + xOffset, position.y + yOffset, position.z + zOffset, direction.x * speed, direction.z * speed, dmg, damageType, new Color(spellColor), owner);

			if (explosion != null) projectile.explosion = explosion;
			projectile.trailInterval = trailInterval;
			projectile.splashForce = splashForce;
			projectile.splashRadius = this.splashRadius;
			projectile.splashDamage = this.splashDamage;
			projectile.floating = this.floating;

			if(appearance != null) {
				projectile.spriteAtlas = appearance.texAtlas;
				projectile.tex = appearance.tex;
			}
		}
		else {

			// New, data driven way
			projectile = (MagicMissileProjectile)EntityManager.instance.Copy(magicMissileProjectile);

			projectile.x = position.x + xOffset;
			projectile.y = position.y + yOffset;
			projectile.z = position.z + zOffset + 0.1f;

			projectile.xa = direction.x * speed;
			projectile.ya = direction.z * speed;

			projectile.owner = owner;
			projectile.damage = dmg;
			projectile.damageType = damageType;

			if(spellColor != null) projectile.color = spellColor;
		}

		// Offset projectiles for monsters
		if(owner instanceof Monster) {
			projectile.z += ((Monster)owner).projectileOffset;
		}

		projectile.za = direction.y * speed;
		if(hitSound != null) projectile.hitSound = hitSound;

		return projectile;
	}
}
