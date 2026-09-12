package com.interrupt.dungeoneer.profile;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import java.util.UUID;

public class ProfileManager {

    private static final String PROFILE_DIRECTORY = "save/profile";
    private static final String PROFILE_FILE = "profile.json";

    private static PlayerProfile currentProfile;

    private static final Json json = new Json();


    public static PlayerProfile getProfile() {

        if(currentProfile == null) {
            load();
        }

        return currentProfile;
    }


    public static void load() {

        FileHandle file = getProfileFile();

        if(file.exists()) {

            try {

                currentProfile = json.fromJson(
                    PlayerProfile.class,
                    file
                );

                if(currentProfile != null) {

                    if(currentProfile.stash == null) {
                        currentProfile.stash =
                            new com.badlogic.gdx.utils.Array<ProfileStashEntry>();
                    }

                    if(currentProfile.campChest == null) {
                        currentProfile.campChest =
                            new com.badlogic.gdx.utils.Array<ProfileStashEntry>();
                    }

                    Gdx.app.log(
                        "ProfileManager",
                        "Loaded profile: "
                            + currentProfile.username
                    );

                    return;
                }
            }
            catch(Exception e) {

                Gdx.app.error(
                    "ProfileManager",
                    "Failed to load profile.",
                    e
                );
            }
        }

        createNewProfile();
    }




    private static void createNewProfile() {

        currentProfile = new PlayerProfile();

        currentProfile.profileId =
            UUID.randomUUID().toString();

        currentProfile.username =
            "Adventurer";

        currentProfile.createdAt =
            System.currentTimeMillis();

        save();

        Gdx.app.log(
            "ProfileManager",
            "Created new profile: "
                + currentProfile.profileId
        );
    }


    public static void save() {

        if(currentProfile == null) {
            return;
        }

        try {

            FileHandle file = getProfileFile();

            file.parent().mkdirs();

            String profileJson =
                json.prettyPrint(currentProfile);

            file.writeString(
                profileJson,
                false,
                "UTF-8"
            );

            Gdx.app.log(
                "ProfileManager",
                "Profile saved."
            );
        }
        catch(Exception e) {

            Gdx.app.error(
                "ProfileManager",
                "Failed to save profile.",
                e
            );
        }
    }


    private static FileHandle getProfileFile() {

        return Gdx.files.local(
            PROFILE_DIRECTORY
                + "/"
                + PROFILE_FILE
        );
    }

    public static void addToStash(String itemName, int amount) {

        if(itemName == null || itemName.isEmpty() || amount <= 0) {
            return;
        }

        PlayerProfile profile = getProfile();

        for(ProfileStashEntry entry : profile.stash) {

            if(entry.itemName.equals(itemName)) {
                entry.amount += amount;
                save();
                return;
            }
        }

        profile.stash.add(
            new ProfileStashEntry(
                itemName,
                amount
            )
        );

        save();
    }

    public static boolean removeFromStash(String itemName, int amount) {

        if(itemName == null || itemName.isEmpty() || amount <= 0) {
            return false;
        }

        PlayerProfile profile = getProfile();

        for(int i = 0; i < profile.stash.size; i++) {

            ProfileStashEntry entry =
                profile.stash.get(i);

            if(!entry.itemName.equals(itemName)) {
                continue;
            }

            if(entry.amount < amount) {
                return false;
            }

            entry.amount -= amount;

            if(entry.amount <= 0) {
                profile.stash.removeIndex(i);
            }

            save();
            return true;
        }

        return false;
    }

    public static int getStashAmount(String itemName) {

        if(itemName == null || itemName.isEmpty()) {
            return 0;
        }

        PlayerProfile profile = getProfile();

        for(ProfileStashEntry entry : profile.stash) {

            if(entry.itemName.equals(itemName)) {
                return entry.amount;
            }
        }

        return 0;
    }

    public static int getCampChestAmount(String itemName) {

        if(itemName == null || itemName.isEmpty()) {
            return 0;
        }

        PlayerProfile profile = getProfile();

        if(profile.campChest == null) {
            return 0;
        }

        for(ProfileStashEntry entry : profile.campChest) {

            if(entry.itemName.equals(itemName)) {
                return entry.amount;
            }
        }

        return 0;
    }

    public static boolean sendToCamp(
        String itemName,
        int amount) {

        if(itemName == null
            || itemName.isEmpty()
            || amount <= 0) {

            return false;
        }

        PlayerProfile profile = getProfile();

        if(profile.stash == null) {
            return false;
        }

        if(profile.campChest == null) {
            profile.campChest =
                new com.badlogic.gdx.utils.Array<ProfileStashEntry>();
        }


        // Find the item in the permanent stash.
        ProfileStashEntry stashEntry = null;

        for(ProfileStashEntry entry : profile.stash) {

            if(entry.itemName.equals(itemName)) {
                stashEntry = entry;
                break;
            }
        }

        if(stashEntry == null) {
            return false;
        }

        if(stashEntry.amount < amount) {
            return false;
        }


        // Find an existing stack in the camp chest.
        ProfileStashEntry campEntry = null;

        for(ProfileStashEntry entry : profile.campChest) {

            if(entry.itemName.equals(itemName)) {
                campEntry = entry;
                break;
            }
        }


        // Remove from permanent stash.
        stashEntry.amount -= amount;

        if(stashEntry.amount <= 0) {
            profile.stash.removeValue(
                stashEntry,
                true
            );
        }


        // Add to camp chest.
        if(campEntry != null) {

            campEntry.amount += amount;
        }
        else {

            profile.campChest.add(
                new ProfileStashEntry(
                    itemName,
                    amount
                )
            );
        }


        save();

        return true;
    }

    public static boolean returnFromCamp(
        String itemName,
        int amount) {

        if(itemName == null
            || itemName.isEmpty()
            || amount <= 0) {
            return false;
        }

        PlayerProfile profile = getProfile();

        if(profile.campChest == null) {
            return false;
        }

        if(profile.stash == null) {
            profile.stash =
                new com.badlogic.gdx.utils.Array<ProfileStashEntry>();
        }

        ProfileStashEntry campEntry = null;

        for(ProfileStashEntry entry : profile.campChest) {
            if(entry.itemName.equals(itemName)) {
                campEntry = entry;
                break;
            }
        }

        if(campEntry == null || campEntry.amount < amount) {
            return false;
        }

        ProfileStashEntry stashEntry = null;

        for(ProfileStashEntry entry : profile.stash) {
            if(entry.itemName.equals(itemName)) {
                stashEntry = entry;
                break;
            }
        }

        campEntry.amount -= amount;

        if(campEntry.amount <= 0) {
            profile.campChest.removeValue(
                campEntry,
                true
            );
        }

        if(stashEntry != null) {
            stashEntry.amount += amount;
        }
        else {
            profile.stash.add(
                new ProfileStashEntry(
                    itemName,
                    amount
                )
            );
        }

        save();

        return true;
    }
}
