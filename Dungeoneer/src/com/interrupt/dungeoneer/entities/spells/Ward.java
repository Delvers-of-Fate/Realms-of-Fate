package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;

/**
 * A maintained defensive spell.
 *
 * The Ward is active only while its caster holds the attack button. It can
 * charge an initial mana cost, consume mana at a configurable interval, and
 * reduce incoming damage while active.
 *
 * Visual shield geometry and physical projectile interception can be layered
 * on top of this class without changing its held-cast or protection contract.
 */
public class Ward extends Spell {

    /**
     * Mana spent once when the Ward is first raised.
     *
     * 0 = no initial cost.
     */
    public int initialManaCost = 0;

    /**
     * Mana consumed each upkeep tick while the Ward is maintained.
     *
     * Example: 2 = consume 2 MP each time manaTickRate elapses.
     */
    public int manaCostPerTick = 2;

    /**
     * Number of real-time seconds between mana upkeep ticks.
     *
     * Example: 1.0 = every second, 2.0 = every two seconds.
     */
    public float manaTickRate = 1.0f;

    /**
     * Fraction of incoming damage prevented while the Ward is active.
     *
     * 0.00 = no reduction
     * 0.50 = 50% reduction
     * 1.00 = full damage block
     */
    public float damageReduction = 0.75f;

    /** Show a sustained particle shield while the Ward is active. */
    public boolean showWardParticles = true;

    /** Real-time seconds between sustained Ward particle pulses. */
    public float wardParticleInterval = 0.08f;

    /** Number of particles emitted per sustained pulse. */
    public int wardParticlesPerPulse = 2;

    /** Distance of the visible Ward curtain in front of the caster. */
    public float wardParticleDistance = 0.75f;

    /** Width of the Ward particle curtain. */
    public float wardParticleWidth = 0.9f;

    /** Height of the Ward particle curtain. */
    public float wardParticleHeight = 0.75f;

    /** Runtime-only state. */
    private transient boolean active = false;
    private transient float manaTickTimer = 0f;
    private transient float wardParticleTimer = 0f;

    public Ward() {
        // Held spells do not use the normal one-shot mpCost.
        mpCost = 0;

        // Ward is maintained rather than charged and released.
        canCharge = false;

        // Custom sustained Ward visuals will be added separately.
        doCastVfx = false;
    }

    @Override
    public boolean isHeldSpell() {
        return true;
    }

    @Override
    public boolean canBeginHeldCast(Player owner, boolean consumeMana) {
        if(owner == null) {
            return false;
        }

        if(!consumeMana) {
            return true;
        }

        int minimumMana = Math.max(0, initialManaCost);

        // Require enough MP for the first upkeep tick as well. This prevents
        // raising a mana-powered Ward at 0 MP and immediately collapsing it.
        if(manaCostPerTick > 0 && manaTickRate > 0f) {
            minimumMana += manaCostPerTick;
        }

        return owner.hasMana(minimumMana);
    }

    @Override
    public boolean beginHeldCast(
        Player owner,
        Vector3 direction,
        Vector3 position,
        boolean consumeMana) {

        if(owner == null) {
            return false;
        }

        if(active) {
            owner.setActiveWard(this);
            return true;
        }

        if(!canBeginHeldCast(owner, consumeMana)) {
            return false;
        }

        if(consumeMana && initialManaCost > 0) {
            if(!owner.useMana(initialManaCost)) {
                return false;
            }
        }

        manaTickTimer = 0f;
        wardParticleTimer = 0f;
        active = true;
        owner.setActiveWard(this);

        if(showWardParticles) {
            spawnWardParticles(owner, direction, true);
        }

        playCastSound(owner);
        return true;
    }

    @Override
    public boolean tickHeldCast(
        Player owner,
        Vector3 direction,
        Vector3 position,
        float delta,
        boolean consumeMana) {

        if(!active || owner == null || owner.isDead) {
            endHeldCast(owner);
            return false;
        }

        tickWardParticles(owner, direction, delta);

        // A Ward on a wand that does not use mana has no upkeep cost.
        if(!consumeMana || manaCostPerTick <= 0 || manaTickRate <= 0f) {
            return true;
        }

        // Delver gameplay delta uses roughly 60 gameplay units per real-time
        // second. Convert it so manaTickRate remains human-readable seconds.
        manaTickTimer += delta / 60f;

        while(manaTickTimer >= manaTickRate) {
            if(!owner.useMana(manaCostPerTick)) {
                endHeldCast(owner);
                return false;
            }

            manaTickTimer -= manaTickRate;

            // The payment above covered the time that just elapsed. If it
            // exhausted the caster's MP, collapse at this upkeep boundary.
            if(owner.mp <= 0) {
                endHeldCast(owner);
                return false;
            }
        }

        return true;
    }

    @Override
    public void endHeldCast(Player owner) {
        active = false;
        manaTickTimer = 0f;
        wardParticleTimer = 0f;

        if(owner != null) {
            owner.clearActiveWard(this);
        }
    }

    /**
     * Emits a restrained first-person-friendly Ward shimmer. Held spells do not
     * pass through Spell.zap(), so sustained visuals belong to the Ward itself.
     */
    private void tickWardParticles(Player owner, Vector3 direction, float delta) {
        if(!showWardParticles || wardParticleInterval <= 0f || wardParticlesPerPulse <= 0) {
            return;
        }

        wardParticleTimer += delta / 60f;

        while(wardParticleTimer >= wardParticleInterval) {
            spawnWardParticles(owner, direction, false);
            wardParticleTimer -= wardParticleInterval;
        }
    }

    private void spawnWardParticles(Player owner, Vector3 direction, boolean openingBurst) {
        if(owner == null || Game.GetLevel() == null) {
            return;
        }

        Vector3 castDirection = direction;
        if(castDirection == null || castDirection.len2() < 0.0001f) {
            castDirection = Game.camera.direction;
        }

        // Delver entity space is X/Y horizontal + Z vertical, while the camera
        // direction is X/Z horizontal + Y vertical. Convert explicitly here.
        float forwardX = castDirection.x;
        float forwardY = castDirection.z;
        float forwardZ = castDirection.y;

        float directionLength = (float)Math.sqrt(
            forwardX * forwardX +
            forwardY * forwardY +
            forwardZ * forwardZ
        );

        if(directionLength > 0.0001f) {
            forwardX /= directionLength;
            forwardY /= directionLength;
            forwardZ /= directionLength;
        }

        float horizontalLength = (float)Math.sqrt(
            forwardX * forwardX + forwardY * forwardY
        );

        float sideX = 1f;
        float sideY = 0f;

        if(horizontalLength > 0.0001f) {
            sideX = -forwardY / horizontalLength;
            sideY = forwardX / horizontalLength;
        }

        float originX = owner.x;
        float originY = owner.y;
        float originZ = owner.z + owner.collision.z * 0.35f;

        // For the local player, use the actual camera origin so the Ward sits
        // visibly in front of the first-person view rather than near the feet.
        if(owner == Game.instance.player) {
            originX = Game.camera.position.x;
            originY = Game.camera.position.z;
            originZ = Game.camera.position.y;
        }

        float centerX = originX + forwardX * wardParticleDistance;
        float centerY = originY + forwardY * wardParticleDistance;
        float centerZ = originZ + forwardZ * wardParticleDistance;

        int count = openingBurst ? Math.max(8, wardParticlesPerPulse * 4) : wardParticlesPerPulse;

        for(int i = 0; i < count; i++) {
            float lateral = (Game.rand.nextFloat() - 0.5f) * wardParticleWidth;
            float vertical = (Game.rand.nextFloat() - 0.5f) * wardParticleHeight;

            float px = centerX + sideX * lateral;
            float py = centerY + sideY * lateral;
            float pz = centerZ + vertical;

            Particle particle = new Particle(
                px, py, pz,
                0f, 0f, 0f,
                0, spellColor, true
            );

            particle.floating = true;
            particle.checkCollision = false;
            particle.lifetime = openingBurst ? 32f : 20f;
            particle.scale = openingBurst ? 0.45f : 0.32f;
            particle.startScale = particle.scale;
            particle.endScale = particle.scale;
            particle.blendMode = Entity.BlendMode.ADD;
            particle.playAnimation(8, 12, Game.rand.nextInt(25) + 25);

            Game.GetLevel().SpawnNonCollidingEntity(particle);
        }
    }

    /**
     * Applies this Ward's defensive reduction to raw incoming damage.
     * Healing and non-positive values are never modified.
     */
    public int modifyIncomingDamage(int damage, DamageType damageType, Entity instigator) {
        if(!active || damage <= 0 || damageType == DamageType.HEALING) {
            return damage;
        }

        float reduction = Math.max(0f, Math.min(damageReduction, 1f));
        return (int)Math.ceil(damage * (1f - reduction));
    }

    public boolean isActive() {
        return active;
    }
}
