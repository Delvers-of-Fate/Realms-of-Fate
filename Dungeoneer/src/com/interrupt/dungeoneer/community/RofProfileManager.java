package com.interrupt.dungeoneer.community;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.interrupt.dungeoneer.game.Game;

import java.util.HashMap;
import java.util.UUID;

public class RofProfileManager {

    private static final String PROFILE_DIRECTORY =
        "community/";

    private static final String PROFILE_FILE =
        "player.rofprofile";

    private static RofProfile profile;

    private static Json json;


    // =========================================================
    // INITIALIZATION
    // =========================================================

    public static void initialize() {

        Gdx.app.log(
            "RealmsOfFate",
            "Initializing community profile..."
        );

        ensureJson();

        loadOrCreateProfile();
    }


    // =========================================================
    // GET PROFILE
    // =========================================================

    public static RofProfile getProfile() {

        if(profile == null) {
            initialize();
        }

        return profile;
    }


    // =========================================================
    // LOAD OR CREATE
    // =========================================================

    public static void loadOrCreateProfile() {

        ensureJson();

        FileHandle file =
            getProfileFile();

        Gdx.app.log(
            "RealmsOfFate",
            "Looking for profile at: "
                + file.file().getAbsolutePath()
        );

        if(file.exists()) {

            try {

                String contents =
                    file.readString("UTF-8");

                RofProfile loaded =
                    json.fromJson(
                        RofProfile.class,
                        contents
                    );

                if(loaded != null) {

                    profile = loaded;

                    repairProfile();

                    Gdx.app.log(
                        "RealmsOfFate",
                        "Community profile loaded."
                    );

                    Gdx.app.log(
                        "RealmsOfFate",
                        "Community ID: "
                            + profile.profile.communityId
                    );

                    return;
                }
            }
            catch(Exception exception) {

                Gdx.app.error(
                    "RealmsOfFate",
                    "Could not load community profile.",
                    exception
                );
            }
        }

        Gdx.app.log(
            "RealmsOfFate",
            "No existing profile found. Creating one."
        );

        createNewProfile();
    }


    // =========================================================
    // CREATE PROFILE
    // =========================================================

    private static void createNewProfile() {

        profile =
            new RofProfile();

        long now =
            System.currentTimeMillis();

        profile.profile.communityId =
            UUID.randomUUID().toString();

        profile.profile.createdAt =
            now;

        profile.profile.lastUpdatedAt =
            now;

        save();

        Gdx.app.log(
            "RealmsOfFate",
            "Created new community profile."
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Community ID: "
                + profile.profile.communityId
        );
    }


    // =========================================================
    // SAVE
    // =========================================================

    public static void save() {

        if(profile == null) {
            return;
        }

        ensureJson();

        repairProfile();

        profile.profile.lastUpdatedAt =
            System.currentTimeMillis();

        FileHandle directory =
            getProfileDirectory();

        if(!directory.exists()) {

            Gdx.app.log(
                "RealmsOfFate",
                "Creating community directory: "
                    + directory.file().getAbsolutePath()
            );

            directory.mkdirs();
        }

        FileHandle file =
            getProfileFile();

        try {

            String contents =
                json.prettyPrint(profile);

            file.writeString(
                contents,
                false,
                "UTF-8"
            );

            Gdx.app.log(
                "RealmsOfFate",
                "Community profile saved to:"
            );

            Gdx.app.log(
                "RealmsOfFate",
                file.file().getAbsolutePath()
            );
        }
        catch(Exception exception) {

            Gdx.app.error(
                "RealmsOfFate",
                "Failed to save community profile.",
                exception
            );
        }
    }


    // =========================================================
    // REPAIR PROFILE
    // =========================================================

    private static void repairProfile() {

        if(profile == null) {
            profile = new RofProfile();
        }

        if(profile.profile == null) {
            profile.profile =
                new RofProfile.ProfileInfo();
        }

        if(profile.profile.communityId == null ||
            profile.profile.communityId.trim().isEmpty()) {

            profile.profile.communityId =
                UUID.randomUUID().toString();
        }

        if(profile.profile.createdAt <= 0L) {

            profile.profile.createdAt =
                System.currentTimeMillis();
        }


        // =====================================================
        // EVENTS
        // =====================================================

        if(profile.events == null) {

            profile.events =
                new RofProfile.EventData();
        }

        if(profile.events.active == null) {

            profile.events.active =
                new HashMap<
                    String,
                    RofProfile.ActiveEvent
                    >();
        }

        if(profile.events.completed == null) {

            profile.events.completed =
                new HashMap<
                    String,
                    RofProfile.CompletedEvent
                    >();
        }


        // =====================================================
        // QUESTS
        // =====================================================

        if(profile.quests == null) {

            profile.quests =
                new RofProfile.QuestData();
        }

        if(profile.quests.active == null) {

            profile.quests.active =
                new HashMap<
                    String,
                    RofProfile.QuestProgress
                    >();
        }

        if(profile.quests.completed == null) {

            profile.quests.completed =
                new HashMap<
                    String,
                    RofProfile.CompletedQuest
                    >();
        }


        // =====================================================
        // ACHIEVEMENTS
        // =====================================================

        if(profile.achievements == null) {

            profile.achievements =
                new RofProfile.AchievementData();
        }

        if(profile.achievements.unlocked == null) {

            profile.achievements.unlocked =
                new HashMap<
                    String,
                    RofProfile.AchievementProgress
                    >();
        }


        // =====================================================
        // REWARDS
        // =====================================================

        if(profile.rewards == null) {

            profile.rewards =
                new RofProfile.RewardData();
        }

        if(profile.rewards.claimed == null) {

            profile.rewards.claimed =
                new HashMap<
                    String,
                    RofProfile.ClaimedReward
                    >();
        }
    }


    // =========================================================
    // FILE LOCATIONS
    // =========================================================

    public static FileHandle getProfileDirectory() {

        return Game.getFile(
            PROFILE_DIRECTORY
        );
    }


    public static FileHandle getProfileFile() {

        return Game.getFile(
            PROFILE_DIRECTORY
                + PROFILE_FILE
        );
    }


    // =========================================================
    // COMMUNITY ID
    // =========================================================

    public static String getCommunityId() {

        return getProfile()
            .profile
            .communityId;
    }


    // =========================================================
    // JSON
    // =========================================================

    private static void ensureJson() {

        if(json != null) {
            return;
        }

        json =
            new Json();

        json.setOutputType(
            JsonWriter.OutputType.json
        );
    }


    // =========================================================
    // DEBUG
    // =========================================================

    public static void printProfileLocation() {

        FileHandle file =
            getProfileFile();

        Gdx.app.log(
            "RealmsOfFate",
            "PROFILE LOCATION: "
                + file.file().getAbsolutePath()
        );
    }
}
