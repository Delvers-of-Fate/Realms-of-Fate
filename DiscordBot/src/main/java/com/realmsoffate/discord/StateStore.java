package com.realmsoffate.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class StateStore {

    private static final File RUNTIME_DIR =
        new File("DiscordBot/runtime");

    private static final File STATE_FILE =
        new File(RUNTIME_DIR, "bot_state.json");

    private static final Gson GSON =
        new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static synchronized BotState load() {

        ensureRuntimeDirectory();

        if(!STATE_FILE.exists()) {
            BotState state = new BotState();
            save(state);
            return state;
        }

        try {
            InputStreamReader reader =
                new InputStreamReader(
                    new FileInputStream(STATE_FILE),
                    StandardCharsets.UTF_8
                );

            BotState state =
                GSON.fromJson(reader, BotState.class);

            reader.close();

            if(state == null) {
                state = new BotState();
            }

            if(state.events == null) {
                state.events =
                    new java.util.ArrayList<BotState.EventData>();
            }

            if(state.claims == null) {
                state.claims =
                    new java.util.ArrayList<BotState.ClaimData>();
            }

            return state;
        }
        catch(Exception e) {
            System.err.println(
                "Failed to load bot_state.json"
            );
            e.printStackTrace();

            return new BotState();
        }
    }

    public static synchronized void save(BotState state) {

        ensureRuntimeDirectory();

        try {
            OutputStreamWriter writer =
                new OutputStreamWriter(
                    new FileOutputStream(STATE_FILE),
                    StandardCharsets.UTF_8
                );

            GSON.toJson(state, writer);

            writer.flush();
            writer.close();
        }
        catch(Exception e) {
            System.err.println(
                "Failed to save bot_state.json"
            );
            e.printStackTrace();
        }
    }

    private static void ensureRuntimeDirectory() {

        if(!RUNTIME_DIR.exists()) {

            boolean created =
                RUNTIME_DIR.mkdirs();

            if(!created && !RUNTIME_DIR.exists()) {
                System.err.println(
                    "Could not create bot runtime directory."
                );
            }
        }
    }
}
