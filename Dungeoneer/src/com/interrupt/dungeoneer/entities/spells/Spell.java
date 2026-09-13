package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Actor;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;
import com.interrupt.dungeoneer.game.Colors;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.dungeoneer.statuseffects.StatusEffect;

import java.util.Random;

public class Spell {
    /** Mana point cost. */
	public int mpCost = 1;

	/** Base amount of damage to deal. */
	public int baseDamage = 1;

	//* Random amount of damage to deal. */
	public int randDamage = 1;

	/** Damage type. */
	public DamageType damageType = DamageType.MAGIC;

	/** Spell color. */
	public Color spellColor = Colors.MAGIC;

	/** Sound to play when cast. */
	public String castSound = "spell-missile-2.mp3,spell-missile-2_02.mp3,spell-missile-2_03.mp3,spell-missile-2_04.mp3";

	/** Spell cast volume. */
	public float castSoundVolume = 0.5f;

	/** Minimum spell range. */
	public float minDistanceToTarget = 0f;

	/** Maximum spell range. */
	public float maxDistanceToTarget = 30f;

	/** Status effect to apply to target. */
	public StatusEffect applyStatusEffect = null;

	/** Create vfx entity when cast? */
	public boolean doCastVfx = true;

	/** Entity to spawn when spell is cast. */
	public Entity castVfx = null;

    /** Can this spell become stronger by charging before release? */
    public boolean canCharge = false;

    /** Time in seconds required to reach full charge. */
    public float maxChargeTime = 1.5f;

    /**
     * Mana cost at full charge.
     * Set to -1 to always use the normal mpCost.
     */
    public int chargedMpCost = -1;

    /**
     * Damage multiplier at full charge.
     * 1.0 = no additional damage.
     * 2.0 = double damage at full charge.
     */
    public float chargedDamageMultiplier = 1f;

    /**
     * True only while performing a fully charged cast.
     *
     * Runtime only; this is not saved into spell data.
     */
    public transient boolean isChargedCast = false;

    /**
     * Runtime charge strength for the current cast.
     *
     * 0.0 = normal / uncharged
     * 1.0 = fully charged
     *
     * Transient so this value is not saved into spell data.
     */

	public Spell() { }

	// casting directly costs spell points
	public void cast(Actor owner, Vector3 direction) {
		if(owner.mp < mpCost) return;

		owner.mp -= mpCost;
		if(owner.mp < 0) owner.mp = 0;
		if(owner.mp > owner.maxMp) owner.mp = owner.maxMp;

		doCast(owner, direction, new Vector3(owner.x, owner.y, owner.z));
		playCastSound(owner);
	}

	// zapping from a wand or scroll costs no spell points
	public void zap(Actor owner, Vector3 direction) {
		zap(owner, direction, new Vector3(owner.x, owner.y, owner.z));
	}

	// zap with a position AND direction
    public void zap(Actor owner, Vector3 direction, Vector3 position) {
        doCast(owner, direction, position);

		if(doCastVfx) {
			if(castVfx == null) {
				doCastEffect(position, Game.GetLevel(), owner);
			}
			else {
				Entity vfx = (Entity)KryoSerializer.copyObject(castVfx);

				vfx.x += position.x;
				vfx.y += position.y;
				vfx.z += position.z;

				Game.GetLevel().SpawnEntity(vfx);
			}
		}

        playCastSound(owner);
    }

	// Override this for specific spell effects
	public void doCast(Entity owner, Vector3 direction, Vector3 position) { }

	private void doCastEffect(Level level, Entity owner) {
		doCastEffect(new Vector3(owner.x, owner.y, owner.z), level, owner);
	}

	// Override this for different spell casting effects
	protected void doCastEffect(Vector3 pos, Level level, Entity owner) { }

	// Override this for different casting sounds
	public void playCastSound(Actor owner) {
		if(owner == Game.instance.player) {
			Audio.playSound(castSound, castSoundVolume);
		}
		else {
			Audio.playPositionedSound(castSound, new Vector3(owner.x, owner.y, owner.z), castSoundVolume, 12);
		}
	}


    /**
     * Gets the mana cost for a given charge amount.
     */
    public int getManaCostForCharge(float charge) {
        charge = Math.max(0f, Math.min(charge, 1f));

        if(!canCharge || chargedMpCost < 0) {
            return mpCost;
        }

        return Math.round(mpCost + ((chargedMpCost - mpCost) * charge));
    }

    /**
     * Gets the damage multiplier for a given charge amount.
     */
    public float getDamageMultiplierForCharge(float charge) {
        charge = Math.max(0f, Math.min(charge, 1f));

        if(!canCharge) {
            return 1f;
        }

        return 1f + ((chargedDamageMultiplier - 1f) * charge);
    }

    /**
     * Gets the mana cost for the current cast.
     */
    public int getCastManaCost() {
        if(canCharge && isChargedCast && chargedMpCost >= 0) {
            return chargedMpCost;
        }

        return mpCost;
    }

    /**
     * Gets the damage multiplier for the current cast.
     */
    public float getCastDamageMultiplier() {
        if(canCharge && isChargedCast) {
            return chargedDamageMultiplier;
        }

        return 1f;
    }

    public int doAttackRoll() {
        Random r = new Random();

        int dmg = baseDamage;
        dmg += r.nextInt(randDamage + 1);

        dmg = Math.round(dmg * getCastDamageMultiplier());

        if(dmg < 1) {
            dmg = 1;
        }

        return dmg;
    }
}
