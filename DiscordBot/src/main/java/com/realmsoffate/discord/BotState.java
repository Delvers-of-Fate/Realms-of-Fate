package com.realmsoffate.discord;

import java.util.ArrayList;
import java.util.List;

public class BotState {

    public List<EventData> events =
        new ArrayList<EventData>();

    public List<ClaimData> claims =
        new ArrayList<ClaimData>();

    /**
     * Linked Realms of Fate player profiles.
     */
    public List<ProfileData> profiles =
        new ArrayList<ProfileData>();


    public static class EventData {

        public String eventId;

        public String name;

        public String description;

        public boolean active = false;

        public int championSlots = 5;

        public int wins = 0;

        public int deaths = 0;

        /**
         * Existing event objective structure used by BotMain.
         */
        public ObjectiveData objectives =
            new ObjectiveData();

        /**
         * Existing reward information.
         */
        public String rewardKey;

        public String rewardName;

        /**
         * New global contribution total used by profile syncing.
         */
        public int communityKills = 0;

        /**
         * Optional target used by profile syncing / community state.
         */
        public int requiredKills = 0;

        public List<ChampionData> champions =
            new ArrayList<ChampionData>();
    }


    /**
     * Matches the structure already expected by BotMain:
     *
     * communityEvent.objectives.kills
     * communityEvent.objectives.floorsCleared
     */
    public static class ObjectiveData {

        public int kills = 0;

        public int floorsCleared = 0;
    }


    public static class ChampionData {

        public String discordId;

        public String playerName;

        public int slot;

        public String status;
    }


    public static class ClaimData {

        public String codeHash;

        public String discordId;

        public String eventId;

        public String result;

        public boolean used;
    }


    /**
     * One player's persistent Realms of Fate profile link.
     */
    public static class ProfileData {

        public String communityId;

        public String discordId;

        public String discordName;

        public long linkedAt = 0L;

        public long lastSyncAt = 0L;

        /**
         * Last accepted progress for every event this
         * player has submitted.
         */
        public List<AcceptedEventProgress> events =
            new ArrayList<AcceptedEventProgress>();
    }


    /**
     * Prevents duplicate profile uploads from contributing
     * the same progress more than once.
     */
    public static class AcceptedEventProgress {

        public String eventId;

        public int acceptedKills = 0;

        public long lastUpdatedAt = 0L;
    }
}
