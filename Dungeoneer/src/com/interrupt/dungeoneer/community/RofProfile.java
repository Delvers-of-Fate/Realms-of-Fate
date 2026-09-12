package com.interrupt.dungeoneer.community;

import java.util.HashMap;
import java.util.Map;

/**
 * Persistent Realms of Fate community profile.
 *
 * This file intentionally contains only community-facing data.
 * It is separate from the normal Delver save game.
 */
public class RofProfile {

    public int formatVersion = 1;

    public ProfileInfo profile =
        new ProfileInfo();

    public EventData events =
        new EventData();

    public QuestData quests =
        new QuestData();

    public AchievementData achievements =
        new AchievementData();

    public RewardData rewards =
        new RewardData();


    // =========================================================
    // PROFILE INFORMATION
    // =========================================================

    public static class ProfileInfo {

        /**
         * Permanent ID used to identify this Realms of Fate
         * community profile.
         *
         * This should NEVER be regenerated after the profile
         * has been created.
         */
        public String communityId;

        /**
         * Unix time in milliseconds when the profile
         * was first created.
         */
        public long createdAt = 0L;

        /**
         * Unix time in milliseconds when the profile
         * was most recently saved.
         */
        public long lastUpdatedAt = 0L;
    }


    // =========================================================
    // EVENTS
    // =========================================================

    public static class EventData {

        /**
         * Events currently being tracked by the player.
         *
         * Key:
         * eventId
         *
         * Example:
         * blackstone_fortress
         */
        public Map<String, ActiveEvent> active =
            new HashMap<String, ActiveEvent>();

        /**
         * Events completed by this profile.
         */
        public Map<String, CompletedEvent> completed =
            new HashMap<String, CompletedEvent>();
    }


    public static class ActiveEvent {

        /**
         * When the player started participating
         * in this event.
         */
        public long startedAt = 0L;

        /**
         * Player progress for this event.
         */
        public EventProgress progress =
            new EventProgress();
    }


    public static class CompletedEvent {

        /**
         * When the event was completed.
         */
        public long completedAt = 0L;

        /**
         * Final recorded progress.
         */
        public EventProgress progress =
            new EventProgress();
    }


    public static class EventProgress {

        public int kills = 0;

        public int floorsCleared = 0;
    }


    // =========================================================
    // QUESTS
    // =========================================================

    public static class QuestData {

        /**
         * Intentionally empty for now.
         *
         * We are reserving this section so adding quests
         * later will not require changing the root profile
         * format.
         */
        public Map<String, QuestProgress> active =
            new HashMap<String, QuestProgress>();

        public Map<String, CompletedQuest> completed =
            new HashMap<String, CompletedQuest>();
    }


    public static class QuestProgress {

        public long startedAt = 0L;

        public int progress = 0;
    }


    public static class CompletedQuest {

        public long completedAt = 0L;
    }


    // =========================================================
    // ACHIEVEMENTS
    // =========================================================

    public static class AchievementData {

        /**
         * Key:
         * achievementId
         */
        public Map<String, AchievementProgress> unlocked =
            new HashMap<String, AchievementProgress>();
    }


    public static class AchievementProgress {

        public long unlockedAt = 0L;
    }


    // =========================================================
    // REWARDS
    // =========================================================

    public static class RewardData {

        /**
         * Records rewards that the game has successfully
         * accepted / claimed.
         *
         * We will expand this later when the actual
         * reward import system exists.
         */
        public Map<String, ClaimedReward> claimed =
            new HashMap<String, ClaimedReward>();
    }


    public static class ClaimedReward {

        public long claimedAt = 0L;
    }
}
