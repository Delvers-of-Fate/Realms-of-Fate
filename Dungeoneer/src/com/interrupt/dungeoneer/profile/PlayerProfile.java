package com.interrupt.dungeoneer.profile;

import com.badlogic.gdx.utils.Array;

public class PlayerProfile {

    public int version = 1;

    public String profileId = "";
    public String username = "Adventurer";

    public long createdAt = 0L;

    public Array<ProfileStashEntry> stash =
        new Array<ProfileStashEntry>();

    public Array<ProfileStashEntry> campChest =
        new Array<ProfileStashEntry>();

    public PlayerProfile() {
    }
}
