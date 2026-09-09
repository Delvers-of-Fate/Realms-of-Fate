package com.interrupt.dungeoneer.statuseffects;

import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;

public class ElementalResistanceEffect extends StatusEffect {

    public DamageType resistanceType = DamageType.FIRE;

    /**
     * 0.25f = 25% resistance.
     */
    public float resistanceAmount = 0.25f;

    public ElementalResistanceEffect() {
    }

    public ElementalResistanceEffect(
        String name,
        DamageType resistanceType,
        float resistanceAmount,
        float duration) {

        this.name = name;
        this.resistanceType = resistanceType;
        this.resistanceAmount = resistanceAmount;
        this.timer = duration;
    }

    public float getResistance(DamageType damageType) {
        if(damageType == resistanceType) {
            return resistanceAmount;
        }

        return 0f;
    }
}
