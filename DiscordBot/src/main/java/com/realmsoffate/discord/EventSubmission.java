package com.realmsoffate.discord;

public class EventSubmission {

    public int formatVersion = 1;

    public String communityId;

    public String eventId;

    public Progress progress = new Progress();

    public static class Progress {

        public int kills = 0;

        public int floorsCleared = 0;
    }
}
