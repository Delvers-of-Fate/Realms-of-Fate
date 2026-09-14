package com.realmsoffate.toolkit.content.weapon;

import com.interrupt.dungeoneer.entities.items.Bow;
import com.interrupt.dungeoneer.entities.items.Gun;
import com.interrupt.dungeoneer.entities.items.Sword;
import com.interrupt.dungeoneer.entities.items.Wand;
import com.interrupt.dungeoneer.entities.items.Weapon;

public enum WeaponKind {

    SWORD(
        "Sword",
        Sword.class,
        "sword"
    ),

    BOW(
        "Bow",
        Bow.class,
        "bow"
    ),

    GUN(
        "Gun",
        Gun.class,
        "wand"
    ),

    WAND(
        "Wand",
        Wand.class,
        "wand"
    );

    private final String displayName;

    private final Class<? extends Weapon> engineClass;

    private final String defaultItemType;

    WeaponKind(
        String displayName,
        Class<? extends Weapon> engineClass,
        String defaultItemType
    ) {

        this.displayName =
            displayName;

        this.engineClass =
            engineClass;

        this.defaultItemType =
            defaultItemType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Class<? extends Weapon> getEngineClass() {
        return engineClass;
    }

    public String getEngineClassName() {
        return engineClass.getName();
    }

    public String getDefaultItemType() {
        return defaultItemType;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
