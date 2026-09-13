package com.interrupt.dungeoneer.community;

import java.util.HashMap;

public class CommunityState {

    public int formatVersion = 1;

    public long lastUpdated = 0L;

    public HashMap<String, EventState> events =
        new HashMap<String, EventState>();


    public static class EventState {

        public boolean enabled = false;

        public String status = "";

        public int phase = 0;

        public EventProgress progress =
            new EventProgress();

        public Announcement announcement =
            new Announcement();
    }


    public static class EventProgress {

        public int kills = 0;

        public int requiredKills = 0;

        // Temporary fields.
        // We can clean these up after everything works.
        public int floorsCleared = 0;

        public int requiredFloors = 0;
    }


    public static class Announcement {

        public String title = "";

        public String message = "";
    }
}
