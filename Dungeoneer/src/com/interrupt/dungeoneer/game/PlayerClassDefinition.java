package com.interrupt.dungeoneer.game;

import java.util.HashMap;

public class PlayerClassDefinition {

    public String id = "";
    public String displayName = "";

    public int hp = 8;
    public int mp = 20;

    public int atk = 4;
    public int def = 4;
    public int dex = 4;
    public int spd = 4;
    public int mag = 4;
    public int end = 4;

    public float jumpHeight = 0.05f;

    public float sprintSpeedMultiplier = 1.65f;
    public float crouchSpeedMultiplier = 0.45f;

    public boolean manaRegenerationEnabled = false;
    public float manaRegenRate = 0.05f;

    public String[] startingItems = new String[0];

    public PlayerClassDefinition() { }

    /** Elemental resistances, using DamageType names as keys. */
    public HashMap<String, Float> resistances =
        new HashMap<String, Float>();
}


