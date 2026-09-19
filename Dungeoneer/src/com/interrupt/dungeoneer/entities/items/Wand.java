package com.interrupt.dungeoneer.entities.items;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.entities.Particle;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.entities.spells.MagicMissile;
import com.interrupt.dungeoneer.entities.spells.Spell;
import com.interrupt.dungeoneer.game.CachePools;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.serializers.KryoSerializer;
import com.interrupt.managers.StringManager;

import java.text.MessageFormat;

public class Wand extends Weapon {
    /** Spell to cast when fired. */
	public Spell spell = new MagicMissile();

	/** Current number of charges. */
	@EditorProperty
	public int charges = 0;

    /** Require charges to fire? */
	@EditorProperty
	public boolean usesCharges = true;

    /** Use player mana instead of wand charges? */
    @EditorProperty
    public boolean usesMana = false;

	private transient int lastComputedChargeValue = 0;
	private transient String chargeText = "0";

    /** Fire automatically? */
	@EditorProperty
	public boolean autoFire = false;

    /** Time between automatic shots in milliseconds. */
	@EditorProperty
	public float autoFireTime = 0.1f;

    /** Magic stat scalar for contributing to charge count. */
	@EditorProperty
	protected float magicStatBoostMod = 1f;

	private float attackTimer = 0.0f;

    /** Runtime state for maintained / held spells such as Ward. */
    private transient boolean heldSpellActive = false;

	private Entity fireEffect = null;
	private transient Particle chargeEffect = null;
	private float chargeTime = 0f;

    public Wand() { attackAnimation = "wandAttack"; chargeAnimation = "wandCharge"; equipSound = "/ui/ui_equip_item.mp3"; }

	public Wand(float x, float y) {
		super(x, y, 16, ItemType.wand, StringManager.get("items.Wand.defaultNameText"));
		charges = 5;
	}

	public Color getColor()
	{
		return Weapon.getEnchantmentColor(this.damageType);
	}

	public String GetInfoText() {
		if(usesCharges)
			return MessageFormat.format(StringManager.get("items.Wand.infoText"), getChargeNumber(Game.instance.player)) + "\n" + super.GetInfoText();
		else
			return super.GetInfoText();
	}

    @Override
    public void doAttack(Player p, Level lvl, float attackPower) {

        boolean isChargedCast = spell != null
            && spell.canCharge
            && attackPower >= 1.0f;

        spell.isChargedCast = isChargedCast;

        int manaCost = spell.getCastManaCost();

        if(autoFire) {
            p.handAnimateTimer = autoFireTime * 3f;
        }
        else {
            p.handAnimateTimer =
                (p.handAnimation.length() / p.handAnimation.speed) * 0.75f;
        }

        attackTimer = 1.0f;

        // Check whether this wand has enough resources to fire.
        if(usesMana) {
            if(p.mp < manaCost) {
                Audio.playSound("ui/ui_noammo_wand.mp3", 0.3f);
                spell.isChargedCast = false;
                return;
            }
        }
        else if(usesCharges) {
            if(getChargeNumber(p) <= 0) {
                Audio.playSound("ui/ui_noammo_wand.mp3", 0.3f);
                return;
            }
        }

        // Consume the appropriate resource.
        if(usesMana) {
            p.mp -= manaCost;

            if(p.mp < 0) {
                p.mp = 0;
            }
        }
        else if(usesCharges) {
            charges--;
        }

        Vector3 direction = getCrosshairDirection(-0.3f);
        if(direction == null) direction = Game.camera.direction;

        spell.damageType = damageType;
        spell.baseDamage = getBaseDamage();
        spell.randDamage = getRandDamage();

        spell.zap(
            p,
            direction.cpy(),
            new Vector3(
                x + direction.x * 0.15f,
                y + direction.z * 0.15f,
                z + direction.y * 0.15f
            )
        );

        spell.isChargedCast = false;

        p.history.usedWand(this);

        if(autoFire)
            p.shake(0.2f);
        else
            p.shake(2f);

        makeFireEffect(lvl);
    }

    /** Returns true when this Wand contains a maintained spell. */
    public boolean hasHeldSpell() {
        return spell != null && spell.isHeldSpell();
    }

    public boolean isHeldSpellActive() {
        return heldSpellActive;
    }

    /**
     * Starts a maintained spell without routing through the normal one-shot
     * Wand.doAttack() mana / charge path.
     */
    public boolean beginHeldSpell(Player p) {
        if(p == null || !hasHeldSpell()) {
            return false;
        }

        if(heldSpellActive) {
            return true;
        }

        boolean consumeMana = usesMana;

        if(!spell.canBeginHeldCast(p, consumeMana)) {
            Audio.playSound("ui/ui_noammo_wand.mp3", 0.3f);
            return false;
        }

        // Preserve normal Wand charge semantics for non-mana held spells:
        // one charge raises the maintained spell, then the spell remains up
        // until released or cancelled. Mana-powered Wards use their own upkeep.
        if(!usesMana && usesCharges && getChargeNumber(p) <= 0) {
            Audio.playSound("ui/ui_noammo_wand.mp3", 0.3f);
            return false;
        }

        Vector3 direction = getHeldSpellDirection();
        Vector3 position = getHeldSpellPosition(direction);

        spell.damageType = damageType;
        spell.baseDamage = getBaseDamage();
        spell.randDamage = getRandDamage();

        if(!spell.beginHeldCast(p, direction, position, consumeMana)) {
            Audio.playSound("ui/ui_noammo_wand.mp3", 0.3f);
            return false;
        }

        if(!usesMana && usesCharges) {
            charges--;
        }

        heldSpellActive = true;
        p.history.usedWand(this);
        return true;
    }

    /** Updates a maintained spell while the attack button remains held. */
    public boolean tickHeldSpell(Player p, float delta) {
        if(p == null || !hasHeldSpell() || !heldSpellActive) {
            return false;
        }

        Vector3 direction = getHeldSpellDirection();
        Vector3 position = getHeldSpellPosition(direction);

        boolean keepActive = spell.tickHeldCast(
            p,
            direction,
            position,
            delta,
            usesMana
        );

        if(!keepActive) {
            endHeldSpell(p);
            return false;
        }

        return true;
    }

    /** Stops a maintained spell and guarantees spell-specific cleanup. */
    public void endHeldSpell(Player p) {
        if(!hasHeldSpell()) {
            heldSpellActive = false;
            return;
        }

        if(heldSpellActive) {
            spell.endHeldCast(p);
        }

        heldSpellActive = false;
    }

    private Vector3 getHeldSpellDirection() {
        Vector3 direction = getCrosshairDirection(-0.3f);
        if(direction == null) {
            direction = Game.camera.direction;
        }

        return direction.cpy();
    }

    private Vector3 getHeldSpellPosition(Vector3 direction) {
        return new Vector3(
            x + direction.x * 0.15f,
            y + direction.z * 0.15f,
            z + direction.y * 0.15f
        );
    }

	public int getRandDamage() {
		int boost = 0;
		if(Game.instance != null && Game.instance.player != null) {
			boost = Math.max(0, Game.instance.player.getMagicStatBoost());
		}
		return super.getRandDamage() + boost;
	}

    @Override
    public float getChargeSpeed() {
        if(spell != null && spell.canCharge && spell.maxChargeTime > 0f) {
            // Player.attackChargeTime is 40 and gameplay ticks at a
            // 60-units-per-second scale.
            //
            // This converts maxChargeTime (seconds) into the charge speed
            // expected by Delver's existing attack charging system.
            return 40f / (spell.maxChargeTime * 60f);
        }

        return super.getChargeSpeed();
    }

    public boolean canFire(Player p) {
        if(p == null || spell == null) {
            return false;
        }

        if(hasHeldSpell()) {
            if(heldSpellActive) {
                return true;
            }

            if(!spell.canBeginHeldCast(p, usesMana)) {
                return false;
            }

            if(!usesMana && usesCharges) {
                return getChargeNumber(p) > 0;
            }

            return true;
        }

        if(usesMana) {
            return p.mp >= spell.getCastManaCost();
        }

        if(usesCharges) {
            return getChargeNumber(p) > 0;
        }

        return true;
    }

	public int getChargeNumber(Player p) {
		if(!usesCharges)
			return 1;

		return charges + (int)(p.getMagicStatBoost() * magicStatBoostMod);
	}

	public String getChargeText() {

		if(!usesCharges)
			return "";

		if(getChargeNumber(Game.instance.player) == lastComputedChargeValue) return chargeText;
		else {
			int value = getChargeNumber(Game.instance.player);
			lastComputedChargeValue = value;
			chargeText = value + "";
			return chargeText;
		}
	}

	public void makeFireEffect(Level lvl) {
	    if(fireEffect == null) {
            Particle p = CachePools.getParticle(x, y, z - 0.35f, 0, 0, 0, 18, spell.spellColor, true);

            p.checkCollision = false;
            p.floating = true;
            p.lifetime = (int) (3 * Game.rand.nextFloat()) + 10;
            p.playAnimation(18, 23, p.lifetime);
            p.startScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
            p.endScale = 1f + (0.5f * Game.rand.nextFloat() - 0.25f);
            p.scale = 0.3f;

            p.xa = (0.00125f * Game.rand.nextFloat());
            p.ya = (0.00125f * Game.rand.nextFloat());
            p.za = (0.00125f * Game.rand.nextFloat()) + 0.0025f;

            lvl.SpawnNonCollidingEntity(p);
        }
        else {
            Entity p = (Entity)KryoSerializer.copyObject(fireEffect);
            p.x = x;
            p.y = y;
            p.z = z - 0.3f;
            lvl.SpawnNonCollidingEntity(p);
        }
    }

    Color t_chargeColor = new Color();
	public void tickEquipped(Player player, Level level, float delta, String equipLoc) {
		super.tickEquipped(player, level, delta, equipLoc);

		float effectScale = 0.0f;

        if(player.attackCharge > 0 && canFire(player)) {
			effectScale = Interpolation.circleOut.apply(0f, 1.0f, Math.min(chargeTime * 2f, 1f)) * 0.2f;
		}
		if(attackTimer > 0) {
			effectScale = Interpolation.circleOut.apply(0f, 0.35f, attackTimer);
		}

		if(effectScale > 0) {
			if(chargeEffect == null) {
				t_chargeColor.set(spell.spellColor);
				t_chargeColor.a = effectScale * 2f;

				chargeEffect = CachePools.getParticle(x, y, z - 0.3f, 0, 0, 0, 52, t_chargeColor, true);
				chargeEffect.persists = false;
				chargeEffect.fullbrite = true;
				chargeEffect.blendMode = BlendMode.ADD;
				chargeEffect.haloMode = HaloMode.STENCIL_ONLY;
				chargeEffect.lifetime = 10;

				chargeEffect.setSpriteAtlas("fog_sprites");
				chargeEffect.tex = 0;

				chargeTime = 0f;
				level.addEntity(chargeEffect);
			}

			chargeEffect.x = x;
			chargeEffect.y = y;
			chargeEffect.z = z - 0.275f;
			chargeEffect.lifetime = 5;

			Vector3 cameraDir = GameManager.renderer.camera.direction;
			chargeEffect.x += cameraDir.x * 0.035f;
			chargeEffect.y += cameraDir.z * 0.035f;
			chargeEffect.z += cameraDir.y * 0.035f;

			chargeEffect.scale = effectScale;

			chargeEffect.z -= chargeEffect.scale * 0.15f;

			chargeTime += delta * 0.003f;
		}
		else if(chargeEffect != null) {
			chargeEffect.isActive = false;
			chargeEffect = null;
		}

		if(attackTimer > 0) {
			attackTimer -= delta * 0.04f;
		}
	}
}
