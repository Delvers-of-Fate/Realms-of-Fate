package com.realmsoffate.discord;

import java.util.ArrayList;
import java.util.List;

public class BotState {

    public List<EventData> events = new ArrayList<EventData>();
    public List<ClaimData> claims = new ArrayList<ClaimData>();

    public static class EventData {
        public String eventId;
        public String name;
        public boolean active = false;

        public Objectives objectives = new Objectives();
        public String rewardKey;
        public String rewardName;
        public String description;

        public int kills = 0;
        public int floorsCleared = 0;
        public int championSlots = 5;

        //public int wins = 0;
        //public int deaths = 0;

        public List<ChampionData> champions =
            new ArrayList<ChampionData>();
    }


    public static class Objectives {
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
}
