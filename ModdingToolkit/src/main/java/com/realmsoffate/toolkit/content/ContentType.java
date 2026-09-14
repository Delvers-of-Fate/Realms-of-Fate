package com.realmsoffate.toolkit.content;

public enum ContentType {

    WEAPON(
        "Weapon",
        "Create melee weapons, ranged weapons, wands, and staves."
    ),

    ARMOR(
        "Armor",
        "Create wearable armor and equipment."
    ),

    CONSUMABLE(
        "Consumable",
        "Create potions, food, and other usable items."
    ),

    MISC_ITEM(
        "Miscellaneous Item",
        "Create general items that do not fit another category."
    ),

    SPELL(
        "Spell",
        "Create spells used by magical weapons, abilities, and other systems."
    ),

    SPELL_EFFECT(
        "Spell Effect",
        "Create reusable magical effects."
    ),

    STATUS_EFFECT(
        "Status Effect",
        "Create buffs, debuffs, damage-over-time effects, and other statuses."
    ),

    MONSTER(
        "Monster",
        "Create hostile creatures and enemies."
    ),

    NPC(
        "NPC",
        "Create non-player characters."
    );

    private final String displayName;
    private final String description;

    ContentType(
        String displayName,
        String description
    ) {

        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
