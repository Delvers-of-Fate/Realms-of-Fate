package com.interrupt.dungeoneer.community;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.interrupt.dungeoneer.game.Game;

import java.util.HashMap;

public class CommunitySyncManager {

    private static final String COMMUNITY_STATE_URL =
        "https://delvers-of-fate.github.io/"
            + "Realms-of-Fate-Community/"
            + "api/community_state.json";

    private static final String CACHE_FILE =
        "community/community_cache.json";

    private static CommunityState communityState;

    private static Json json;


    public static void initialize() {

        Gdx.app.log(
            "RealmsOfFate",
            "Initializing community sync..."
        );

        ensureJson();

        loadCachedState();

        refresh();
    }


    public static void refresh() {

        Gdx.app.log(
            "RealmsOfFate",
            "Checking for community updates..."
        );

        Net.HttpRequest request =
            new Net.HttpRequest(
                Net.HttpMethods.GET
            );

        String requestUrl =
            COMMUNITY_STATE_URL
                + "?t="
                + System.currentTimeMillis();

        request.setUrl(
            requestUrl
        );

        request.setHeader(
            "Cache-Control",
            "no-cache"
        );

        request.setHeader(
            "Pragma",
            "no-cache"
        );

        request.setTimeOut(
            10000
        );

        Gdx.net.sendHttpRequest(
            request,
            new Net.HttpResponseListener() {

                @Override
                public void handleHttpResponse(
                    Net.HttpResponse response
                ) {

                    int statusCode =
                        response
                            .getStatus()
                            .getStatusCode();

                    if(statusCode !=
                        HttpStatus.SC_OK) {

                        Gdx.app.error(
                            "RealmsOfFate",
                            "Community request returned HTTP "
                                + statusCode
                        );

                        return;
                    }

                    try {

                        String contents =
                            response.getResultAsString();

                        CommunityState downloaded =
                            json.fromJson(
                                CommunityState.class,
                                contents
                            );

                        if(downloaded == null) {

                            Gdx.app.error(
                                "RealmsOfFate",
                                "Downloaded community state was empty."
                            );

                            return;
                        }

                        communityState =
                            downloaded;

                        repairState();

                        saveCache();

                        Gdx.app.log(
                            "RealmsOfFate",
                            "Community state downloaded successfully."
                        );

                        printDebugState();
                    }
                    catch(Exception exception) {

                        Gdx.app.error(
                            "RealmsOfFate",
                            "Could not parse community state.",
                            exception
                        );
                    }
                }


                @Override
                public void failed(
                    Throwable throwable
                ) {

                    Gdx.app.error(
                        "RealmsOfFate",
                        "Could not download community state.",
                        throwable
                    );

                    if(communityState != null) {

                        Gdx.app.log(
                            "RealmsOfFate",
                            "Using cached community state."
                        );

                        printDebugState();
                    }
                }


                @Override
                public void cancelled() {

                    Gdx.app.log(
                        "RealmsOfFate",
                        "Community state request cancelled."
                    );
                }
            }
        );
    }


    public static CommunityState getState() {

        if(communityState == null) {

            loadCachedState();
        }

        return communityState;
    }


    public static CommunityState.EventState getEvent(
        String eventId
    ) {

        if(eventId == null ||
            eventId.trim().isEmpty()) {

            return null;
        }

        CommunityState state =
            getState();

        if(state == null ||
            state.events == null) {

            return null;
        }

        return state.events.get(
            eventId
        );
    }


    public static boolean isEventEnabled(
        String eventId
    ) {

        CommunityState.EventState event =
            getEvent(eventId);

        return event != null
            && event.enabled;
    }


    public static boolean isEventActive(
        String eventId
    ) {

        CommunityState.EventState event =
            getEvent(eventId);

        if(event == null ||
            event.status == null) {

            return false;
        }

        return event.enabled
            && event.status.equalsIgnoreCase(
            "ACTIVE"
        );
    }


    public static int getEventKills(
        String eventId
    ) {

        CommunityState.EventState event =
            getEvent(eventId);

        if(event == null ||
            event.progress == null) {

            return 0;
        }

        return event.progress.kills;
    }


    public static int getRequiredEventKills(
        String eventId
    ) {

        CommunityState.EventState event =
            getEvent(eventId);

        if(event == null ||
            event.progress == null) {

            return 0;
        }

        return event.progress.requiredKills;
    }


    public static int getEventPhase(
        String eventId
    ) {

        CommunityState.EventState event =
            getEvent(eventId);

        if(event == null) {

            return 0;
        }

        return event.phase;
    }


    private static void loadCachedState() {

        ensureJson();

        FileHandle file =
            Game.getFile(
                CACHE_FILE
            );

        if(!file.exists()) {

            Gdx.app.log(
                "RealmsOfFate",
                "No cached community state found."
            );

            return;
        }

        try {

            String contents =
                file.readString(
                    "UTF-8"
                );

            CommunityState loaded =
                json.fromJson(
                    CommunityState.class,
                    contents
                );

            if(loaded != null) {

                communityState =
                    loaded;

                repairState();

                Gdx.app.log(
                    "RealmsOfFate",
                    "Loaded cached community state."
                );
            }
        }
        catch(Exception exception) {

            Gdx.app.error(
                "RealmsOfFate",
                "Could not load cached community state.",
                exception
            );
        }
    }


    private static void saveCache() {

        if(communityState == null) {

            return;
        }

        ensureJson();

        FileHandle file =
            Game.getFile(
                CACHE_FILE
            );

        try {

            if(file.parent() != null &&
                !file.parent().exists()) {

                file.parent().mkdirs();
            }

            String contents =
                json.prettyPrint(
                    communityState
                );

            file.writeString(
                contents,
                false,
                "UTF-8"
            );

            Gdx.app.log(
                "RealmsOfFate",
                "Community state cached at: "
                    + file.file().getAbsolutePath()
            );
        }
        catch(Exception exception) {

            Gdx.app.error(
                "RealmsOfFate",
                "Could not save community cache.",
                exception
            );
        }
    }


    private static void repairState() {

        if(communityState == null) {

            communityState =
                new CommunityState();
        }

        if(communityState.events == null) {

            communityState.events =
                new HashMap<
                    String,
                    CommunityState.EventState
                    >();
        }

        for(
            CommunityState.EventState event :
            communityState.events.values()
        ) {

            if(event == null) {

                continue;
            }

            if(event.progress == null) {

                event.progress =
                    new CommunityState.EventProgress();
            }

            if(event.announcement == null) {

                event.announcement =
                    new CommunityState.Announcement();
            }
        }
    }


    private static void printDebugState() {

        CommunityState.EventState event =
            getEvent(
                "blackstone_fortress"
            );

        if(event == null) {

            Gdx.app.log(
                "RealmsOfFate",
                "Blackstone event was not found."
            );

            return;
        }

        Gdx.app.log(
            "RealmsOfFate",
            "========== COMMUNITY EVENT =========="
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Event: blackstone_fortress"
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Enabled: "
                + event.enabled
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Status: "
                + event.status
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Phase: "
                + event.phase
        );

        Gdx.app.log(
            "RealmsOfFate",
            "Kills: "
                + event.progress.kills
                + " / "
                + event.progress.requiredKills
        );

        if(event.announcement != null) {

            Gdx.app.log(
                "RealmsOfFate",
                "Announcement: "
                    + event.announcement.title
            );
        }

        Gdx.app.log(
            "RealmsOfFate",
            "====================================="
        );
    }


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
}
