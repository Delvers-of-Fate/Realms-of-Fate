package com.interrupt.dungeoneer.entities.spells;

import com.badlogic.gdx.math.Vector3;
import com.interrupt.dungeoneer.entities.Entity;
import com.interrupt.dungeoneer.game.Level;

/**
 * Runtime controller used by MagicMissile for delayed projectile patterns.
 *
 * This entity is invisible and exists only long enough to spawn
 * the remaining projectiles in a timed spell sequence.
 */
public class ProjectileSequence extends Entity {

    public MagicMissile spell = null;
    public Entity owner = null;

    public Vector3 castPosition = new Vector3();
    public Vector3 castDirection = new Vector3();

    public FirePattern pattern = FirePattern.VOLLEY;

    public int totalShots = 1;
    public int shotIndex = 0;

    /** Total spread used by VOLLEY / ALTERNATING. */
    public float spread = 0f;

    /** Rotation between successive SPIRAL shots. */
    public float angleStep = 30f;

    /**
     * Delay between shots in Delver tick units.
     * Delver runs at roughly 60 gameplay units per second.
     */
    public float delayTicks = 6f;

    /** Damage was rolled when the original spell was cast. */
    public int[] shotDamages = null;

    private float timer = 0f;

    public ProjectileSequence() {
        hidden = true;
        isSolid = false;
        isDynamic = true;

        collision.set(0f, 0f, 0f);
    }

    public ProjectileSequence(
        MagicMissile spell,
        Entity owner,
        Vector3 castPosition,
        Vector3 castDirection,
        FirePattern pattern,
        int totalShots,
        float spread,
        float angleStep,
        float delaySeconds,
        int[] shotDamages) {

        this();

        this.spell = spell;
        this.owner = owner;

        this.castPosition.set(castPosition);
        this.castDirection.set(castDirection);

        this.pattern = pattern;

        this.totalShots = Math.max(1, totalShots);

        this.spread = spread;
        this.angleStep = angleStep;

        this.delayTicks = Math.max(0.001f, delaySeconds * 60f);

        this.shotDamages = shotDamages;

        timer = 0f;
    }

    @Override
    public void tick(Level level, float delta) {

        if(spell == null || owner == null || totalShots <= 0) {
            isActive = false;
            return;
        }

        timer -= delta;

        // The while loop lets the sequence catch up if a frame takes
        // unusually long, without losing shots.
        while(timer <= 0f && shotIndex < totalShots) {

            Vector3 shotDirection = getShotDirection(shotIndex);

            int damage;

            if(shotDamages != null && shotIndex < shotDamages.length) {
                damage = shotDamages[shotIndex];
            }
            else {
                damage = spell.doAttackRoll();
            }

            spell.spawnProjectileWithDamage(
                owner,
                castPosition,
                shotDirection,
                damage
            );

            shotIndex++;

            if(shotIndex >= totalShots) {
                isActive = false;
                return;
            }

            timer += delayTicks;
        }
    }

    private Vector3 getShotDirection(int index) {

        Vector3 direction = castDirection.cpy();

        switch(pattern) {

            case VOLLEY:
                applyVolleyDirection(direction, index);
                break;

            case ALTERNATING:
                applyAlternatingDirection(direction, index);
                break;

            case SPIRAL:
                applySpiralDirection(direction, index);
                break;

            case BURST:
            default:
                // BURST intentionally keeps the original direction.
                break;
        }

        return direction;
    }

    private void applyVolleyDirection(Vector3 direction, int index) {

        if(totalShots <= 1 || spread == 0f) {
            return;
        }

        float t = (float)index / (float)(totalShots - 1);

        float angle =
            (-spread * 0.5f)
                + (spread * t);

        direction.rotate(
            angle,
            0f,
            1f,
            0f
        );
    }

    private void applyAlternatingDirection(Vector3 direction, int index) {

        if(index == 0 || spread == 0f) {
            return;
        }

        int sideIndex = (index + 1) / 2;

        int maxSideIndex =
            Math.max(1, (totalShots - 1 + 1) / 2);

        float step =
            (spread * 0.5f)
                / maxSideIndex;

        float angle = sideIndex * step;

        // 1 = left
        // 2 = right
        // 3 = farther left
        // 4 = farther right
        if(index % 2 == 1) {
            angle = -angle;
        }

        direction.rotate(
            angle,
            0f,
            1f,
            0f
        );
    }

    private void applySpiralDirection(Vector3 direction, int index) {

        direction.rotate(
            angleStep * index,
            0f,
            1f,
            0f
        );
    }
}
