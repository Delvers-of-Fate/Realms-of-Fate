package com.realmsoffate.discord;

import java.util.HashMap;

public class RofProfileSubmission {

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


    public static class ProfileInfo {

        public String communityId;

        public long createdAt = 0L;

        public long lastUpdatedAt = 0L;
    }


    public static class EventData {

        public HashMap<String, ActiveEvent> active =
            new HashMap<String, ActiveEvent>();

        public HashMap<String, CompletedEvent> completed =
            new HashMap<String, CompletedEvent>();
    }


    public static class ActiveEvent {

        public long startedAt = 0L;

        public EventProgress progress =
            new EventProgress();
    }


    public static class CompletedEvent {

        public long completedAt = 0L;

        public EventProgress progress =
            new EventProgress();
    }


    public static class EventProgress {

        public int kills = 0;

        // Temporary compatibility with the
        // current game-side profile format.
        public int floorsCleared = 0;
    }


    public static class QuestData {

        public HashMap<String, QuestProgress> active =
            new HashMap<String, QuestProgress>();

        public HashMap<String, CompletedQuest> completed =
            new HashMap<String, CompletedQuest>();
    }


    public static class QuestProgress {

        public long startedAt = 0L;

        public int progress = 0;
    }


    public static class CompletedQuest {

        public long completedAt = 0L;
    }


    public static class AchievementData {

        public HashMap<String, AchievementProgress> unlocked =
            new HashMap<String, AchievementProgress>();
    }


    public static class AchievementProgress {

        public long unlockedAt = 0L;
    }


    public static class RewardData {

        public HashMap<String, ClaimedReward> claimed =
            new HashMap<String, ClaimedReward>();
    }


    public static class ClaimedReward {

        public long claimedAt = 0L;
    }


    /**
     * Returns the player's recorded kill total for an event.
     *
     * The event might still be active in the profile or it might
     * already have been moved into the completed map.
     */
    public int getEventKills(String eventId) {

        if(eventId == null ||
            eventId.trim().isEmpty() ||
            events == null) {

            return 0;
        }

        if(events.active != null) {

            ActiveEvent activeEvent =
                events.active.get(eventId);

            if(activeEvent != null &&
                activeEvent.progress != null) {

                return Math.max(
                    0,
                    activeEvent.progress.kills
                );
            }
        }

        if(events.completed != null) {

            CompletedEvent completedEvent =
                events.completed.get(eventId);

            if(completedEvent != null &&
                completedEvent.progress != null) {

                return Math.max(
                    0,
                    completedEvent.progress.kills
                );
            }
        }

        return 0;
    }
}
