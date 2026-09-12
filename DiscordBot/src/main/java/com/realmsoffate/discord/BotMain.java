package com.realmsoffate.discord;

import com.google.gson.Gson;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;

import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import net.dv8tion.jda.api.hooks.ListenerAdapter;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BotMain extends ListenerAdapter {

    // =========================================================
    // MAIN
    // =========================================================

    public static void main(String[] args) throws Exception {

        String token =
            System.getenv("ROF_DISCORD_TOKEN");

        if(token == null ||
            token.trim().isEmpty()) {

            System.err.println(
                "ROF_DISCORD_TOKEN is not set."
            );

            return;
        }

        JDA jda =
            JDABuilder
                .createDefault(token)
                .setActivity(
                    Activity.playing("Realms of Fate")
                )
                .addEventListeners(
                    new BotMain()
                )
                .build();

        jda.awaitReady();

        BotState state =
            StateStore.load();

        System.out.println(
            "Loaded bot state: "
                + state.events.size()
                + " event(s)."
        );

        System.out.println(
            "Connected to Discord. Found "
                + jda.getGuilds().size()
                + " server(s)."
        );

        for(Guild guild :
            jda.getGuilds()) {

            System.out.println(
                "Registering commands in: "
                    + guild.getName()
            );

            guild.updateCommands()
                .addCommands(

                    Commands.slash(
                        "status",
                        "Check the Realms of Fate community system."
                    ),

                    Commands.slash(
                        "events",
                        "View all active Realms of Fate community events."
                    ),

                    Commands.slash(
                            "event",
                            "Realms of Fate community event commands."
                        )
                        .addSubcommands(

                            new SubcommandData(
                                "submit",
                                "Submit a Realms of Fate event file."
                            )
                                .addOption(
                                    OptionType.ATTACHMENT,
                                    "file",
                                    "Upload your .rofevent file.",
                                    true
                                )
                        ),

                    Commands.slash(
                            "champions",
                            "View the champion roster for a community event."
                        )
                        .addOption(
                            OptionType.STRING,
                            "event",
                            "Choose a community event.",
                            true,
                            true
                        ),

                    Commands.slash(
                            "champion",
                            "Manage Realms of Fate champions."
                        )
                        .addSubcommands(

                            new SubcommandData(
                                "add",
                                "Add a champion to an event."
                            )
                                .addOption(
                                    OptionType.STRING,
                                    "event",
                                    "Choose a community event.",
                                    true,
                                    true
                                )
                                .addOption(
                                    OptionType.USER,
                                    "user",
                                    "The Discord user to make a champion.",
                                    true
                                ),

                            new SubcommandData(
                                "remove",
                                "Remove a champion from an event."
                            )
                                .addOption(
                                    OptionType.STRING,
                                    "event",
                                    "Choose a community event.",
                                    true,
                                    true
                                )
                                .addOption(
                                    OptionType.USER,
                                    "user",
                                    "The champion to remove.",
                                    true
                                )
                        )
                )
                .queue(

                    success -> {

                        System.out.println(
                            "Commands registered in "
                                + guild.getName()
                        );
                    },

                    error -> {

                        System.err.println(
                            "Failed to register commands:"
                        );

                        error.printStackTrace();
                    }
                );
        }

        System.out.println(
            "Realms of Fate Discord Bot is online!"
        );
    }

    // =========================================================
    // AUTOCOMPLETE
    // =========================================================

    @Override
    public void onCommandAutoCompleteInteraction(
        CommandAutoCompleteInteractionEvent event) {

        boolean championCommand =
            event.getName().equals("champion")
                &&
                (
                    "add".equals(
                        event.getSubcommandName()
                    )
                        ||
                        "remove".equals(
                            event.getSubcommandName()
                        )
                );

        boolean championsCommand =
            event.getName().equals(
                "champions"
            );

        if(!championCommand &&
            !championsCommand) {

            return;
        }

        if(!event
            .getFocusedOption()
            .getName()
            .equals("event")) {

            return;
        }

        BotState state =
            StateStore.load();

        String typed =
            event
                .getFocusedOption()
                .getValue()
                .toLowerCase();

        List<
            net.dv8tion.jda.api.interactions.commands.Command.Choice
            > choices =
            new ArrayList<
                net.dv8tion.jda.api.interactions.commands.Command.Choice
                >();

        for(BotState.EventData communityEvent :
            state.events) {

            if(communityEvent.name == null ||
                communityEvent.eventId == null) {

                continue;
            }

            if(event.getName().equals("champion")
                &&
                "add".equals(
                    event.getSubcommandName()
                )
                &&
                !communityEvent.active) {

                continue;
            }

            String eventName =
                communityEvent.name.toLowerCase();

            String eventId =
                communityEvent.eventId.toLowerCase();

            if(!typed.isEmpty()
                &&
                !eventName.contains(typed)
                &&
                !eventId.contains(typed)) {

                continue;
            }

            String displayName =
                communityEvent.name;

            if(!communityEvent.active) {

                displayName +=
                    " [INACTIVE]";
            }

            choices.add(
                new net.dv8tion.jda.api.interactions.commands.Command.Choice(
                    displayName,
                    communityEvent.eventId
                )
            );

            if(choices.size() >= 25) {
                break;
            }
        }

        event
            .replyChoices(choices)
            .queue();
    }

    // =========================================================
    // SLASH COMMAND ROUTING
    // =========================================================

    @Override
    public void onSlashCommandInteraction(
        SlashCommandInteractionEvent event) {

        System.out.println(
            "Received command: /"
                + event.getName()
        );

        if(event.getName().equals("status")) {

            event.reply(
                    "✅ Realms of Fate community system is online."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        if(event.getName().equals("events")) {

            showActiveEvents(event);

            return;
        }

        if(event.getName().equals("event")) {

            if("submit".equals(
                event.getSubcommandName())) {

                submitEventFile(event);
            }

            return;
        }

        if(event.getName().equals("champions")) {

            showChampions(event);

            return;
        }

        if(event.getName().equals("champion")) {

            if("add".equals(
                event.getSubcommandName())) {

                addChampion(event);

                return;
            }

            if("remove".equals(
                event.getSubcommandName())) {

                removeChampion(event);

                return;
            }
        }
    }

    // =========================================================
    // BUTTON ROUTING
    // =========================================================

    @Override
    public void onButtonInteraction(
        ButtonInteractionEvent event) {

        String buttonId =
            event.getComponentId();

        // -----------------------------------------------------
        // ALL EVENTS BUTTON
        // -----------------------------------------------------

        if(buttonId.equals(
            "rof_all_events")) {

            BotState state =
                StateStore.load();

            EmbedBuilder embed =
                buildActiveEventsEmbed(
                    state
                );

            event.replyEmbeds(
                    embed.build()
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        // -----------------------------------------------------
        // EVENT DETAILS BUTTON
        // -----------------------------------------------------

        if(buttonId.startsWith(
            "rof_event_details:")) {

            String eventId =
                buttonId.substring(
                    "rof_event_details:"
                        .length()
                );

            BotState state =
                StateStore.load();

            BotState.EventData targetEvent =
                findEvent(
                    state,
                    eventId
                );

            if(targetEvent == null) {

                event.reply(
                        "That event could not be found."
                    )
                    .setEphemeral(true)
                    .queue();

                return;
            }

            event.replyEmbeds(
                    buildEventDetailsEmbed(
                        targetEvent
                    ).build()
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        // -----------------------------------------------------
        // CHAMPIONS BUTTON
        // -----------------------------------------------------

        if(buttonId.startsWith(
            "rof_event_champions:")) {

            String eventId =
                buttonId.substring(
                    "rof_event_champions:"
                        .length()
                );

            BotState state =
                StateStore.load();

            BotState.EventData targetEvent =
                findEvent(
                    state,
                    eventId
                );

            if(targetEvent == null) {

                event.reply(
                        "That event could not be found."
                    )
                    .setEphemeral(true)
                    .queue();

                return;
            }

            event.replyEmbeds(
                    buildChampionsEmbed(
                        targetEvent
                    ).build()
                )
                .setEphemeral(true)
                .queue();
        }
    }

    // =========================================================
    // ACTIVE EVENTS
    // =========================================================

    private void showActiveEvents(
        SlashCommandInteractionEvent interaction) {

        BotState state =
            StateStore.load();

        EmbedBuilder embed =
            buildActiveEventsEmbed(
                state
            );

        interaction
            .replyEmbeds(
                embed.build()
            )
            .setEphemeral(true)
            .queue();
    }

    private EmbedBuilder buildActiveEventsEmbed(
        BotState state) {

        EmbedBuilder embed =
            new EmbedBuilder();

        embed.setTitle(
            "⚔️ Realms of Fate Community Events"
        );

        embed.setColor(
            0x5865F2
        );

        embed.setDescription(
            "**ACTIVE COMMUNITY OPERATIONS**\n"
                + divider()
                + "\n"
                + "Choose an event and join the fight."
        );

        int activeCount =
            0;

        for(BotState.EventData communityEvent :
            state.events) {

            if(!communityEvent.active) {
                continue;
            }

            activeCount++;

            StringBuilder info =
                new StringBuilder();

            if(communityEvent.description != null
                &&
                !communityEvent.description
                    .trim()
                    .isEmpty()) {

                info.append(
                    communityEvent.description
                );

                info.append(
                    "\n\n"
                );
            }

            info.append(
                "**Objectives**\n"
            );

            if(communityEvent.objectives != null) {

                if(communityEvent.objectives.kills > 0) {

                    info.append(
                        "⚔️ "
                    );

                    info.append(
                        communityEvent
                            .objectives
                            .kills
                    );

                    info.append(
                        " enemies slain\n"
                    );
                }

                if(communityEvent
                    .objectives
                    .floorsCleared > 0) {

                    info.append(
                        "🗺️ "
                    );

                    info.append(
                        communityEvent
                            .objectives
                            .floorsCleared
                    );

                    info.append(
                        " floors cleared\n"
                    );
                }
            }

            if(communityEvent.rewardName != null
                &&
                !communityEvent.rewardName
                    .trim()
                    .isEmpty()) {

                info.append(
                    "\n🎁 **Reward:** "
                );

                info.append(
                    communityEvent.rewardName
                );
            }

            embed.addField(
                communityEvent.name,
                info.toString(),
                false
            );
        }

        if(activeCount == 0) {

            embed.setDescription(
                "There are currently no active community events."
            );
        }

        embed.setFooter(
            activeCount
                + " active event(s) • Realms of Fate"
        );

        embed.setTimestamp(
            Instant.now()
        );

        return embed;
    }

    // =========================================================
    // EVENT SUBMISSION
    // =========================================================

    private void submitEventFile(
        SlashCommandInteractionEvent interaction) {

        if(interaction.getOption("file") == null) {

            interaction.reply(
                    "You must attach a `.rofevent` file."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        Message.Attachment attachment =
            interaction
                .getOption("file")
                .getAsAttachment();

        String fileName =
            attachment.getFileName();

        if(fileName == null ||
            !fileName
                .toLowerCase()
                .endsWith(".rofevent")) {

            interaction.reply(
                    "That is not a valid `.rofevent` file."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        if(attachment.getSize() >
            1024 * 1024) {

            interaction.reply(
                    "That event file is too large."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        interaction
            .deferReply(true)
            .queue();

        attachment
            .getProxy()
            .download()
            .thenAccept(
                inputStream -> {

                    try {

                        Gson gson =
                            new Gson();

                        InputStreamReader reader =
                            new InputStreamReader(
                                inputStream
                            );

                        EventSubmission submission =
                            gson.fromJson(
                                reader,
                                EventSubmission.class
                            );

                        reader.close();

                        if(submission == null) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "I could not read that event file."
                                )
                                .queue();

                            return;
                        }

                        if(submission.formatVersion != 1) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "Unsupported event file version: "
                                        + submission.formatVersion
                                )
                                .queue();

                            return;
                        }

                        if(submission.communityId == null ||
                            submission.communityId
                                .trim()
                                .isEmpty()) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "This event file does not contain a community ID."
                                )
                                .queue();

                            return;
                        }

                        if(submission.eventId == null ||
                            submission.eventId
                                .trim()
                                .isEmpty()) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "This event file does not contain an event ID."
                                )
                                .queue();

                            return;
                        }

                        BotState state =
                            StateStore.load();

                        BotState.EventData targetEvent =
                            findEvent(
                                state,
                                submission.eventId
                            );

                        if(targetEvent == null) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "I could not find the event `"
                                        + submission.eventId
                                        + "`."
                                )
                                .queue();

                            return;
                        }

                        if(!targetEvent.active) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "**"
                                        + targetEvent.name
                                        + "** is not currently active."
                                )
                                .queue();

                            return;
                        }

                        int kills =
                            0;

                        int floorsCleared =
                            0;

                        if(submission.progress != null) {

                            kills =
                                submission
                                    .progress
                                    .kills;

                            floorsCleared =
                                submission
                                    .progress
                                    .floorsCleared;
                        }

                        if(kills < 0 ||
                            floorsCleared < 0) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "That event file contains invalid progress values."
                                )
                                .queue();

                            return;
                        }

                        int requiredKills =
                            0;

                        int requiredFloors =
                            0;

                        if(targetEvent.objectives != null) {

                            requiredKills =
                                targetEvent
                                    .objectives
                                    .kills;

                            requiredFloors =
                                targetEvent
                                    .objectives
                                    .floorsCleared;
                        }

                        if(requiredKills <= 0 &&
                            requiredFloors <= 0) {

                            interaction
                                .getHook()
                                .editOriginal(
                                    "This event does not have any objectives configured."
                                )
                                .queue();

                            return;
                        }

                        boolean killsComplete =
                            requiredKills <= 0
                                ||
                                kills >= requiredKills;

                        boolean floorsComplete =
                            requiredFloors <= 0
                                ||
                                floorsCleared >=
                                    requiredFloors;

                        boolean eventComplete =
                            killsComplete
                                &&
                                floorsComplete;

                        EmbedBuilder embed =
                            buildSubmissionEmbed(
                                targetEvent,
                                submission,
                                kills,
                                floorsCleared,
                                requiredKills,
                                requiredFloors,
                                killsComplete,
                                floorsComplete,
                                eventComplete
                            );

                        Button detailsButton =
                            Button.primary(
                                "rof_event_details:"
                                    + targetEvent.eventId,
                                "Event Details"
                            );

                        Button championsButton =
                            Button.secondary(
                                "rof_event_champions:"
                                    + targetEvent.eventId,
                                "Champions"
                            );

                        Button eventsButton =
                            Button.secondary(
                                "rof_all_events",
                                "All Events"
                            );

                        interaction
                            .getHook()
                            .editOriginalEmbeds(
                                embed.build()
                            )
                            .setActionRow(
                                detailsButton,
                                championsButton,
                                eventsButton
                            )
                            .queue();
                    }
                    catch(Exception exception) {

                        exception.printStackTrace();

                        interaction
                            .getHook()
                            .editOriginal(
                                "There was an error reading that `.rofevent` file."
                            )
                            .queue();
                    }
                }
            )
            .exceptionally(
                error -> {

                    error.printStackTrace();

                    interaction
                        .getHook()
                        .editOriginal(
                            "I could not download that `.rofevent` file."
                        )
                        .queue();

                    return null;
                }
            );
    }

    // =========================================================
    // SUBMISSION EMBED
    // =========================================================

    private EmbedBuilder buildSubmissionEmbed(
        BotState.EventData targetEvent,
        EventSubmission submission,
        int kills,
        int floorsCleared,
        int requiredKills,
        int requiredFloors,
        boolean killsComplete,
        boolean floorsComplete,
        boolean eventComplete) {

        EmbedBuilder embed =
            new EmbedBuilder();

        embed.setTitle(
            eventComplete
                ? "🏆 " + targetEvent.name
                : "⚔️ " + targetEvent.name
        );

        if(eventComplete) {

            embed.setColor(
                0x57F287
            );

            embed.setDescription(
                "**COMMUNITY EVENT REPORT**\n"
                    + divider()
                    + "\n"
                    + "✅ **EVENT COMPLETED**\n\n"
                    + safeDescription(
                    targetEvent.description
                )
            );
        }
        else {

            embed.setColor(
                0xFEE75C
            );

            embed.setDescription(
                "**COMMUNITY EVENT REPORT**\n"
                    + divider()
                    + "\n"
                    + "⏳ **EVENT IN PROGRESS**\n\n"
                    + safeDescription(
                    targetEvent.description
                )
            );
        }

        // -----------------------------------------------------
        // PLAYER
        // -----------------------------------------------------

        embed.addField(
            "👤 PLAYER",
            "`"
                + submission.communityId
                + "`",
            false
        );

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        // -----------------------------------------------------
        // OBJECTIVES
        // -----------------------------------------------------

        StringBuilder objectives =
            new StringBuilder();

        if(requiredKills > 0) {

            objectives.append(
                "**⚔️ Enemies Slain**\n"
            );

            objectives.append(
                progressBar(
                    kills,
                    requiredKills
                )
            );

            objectives.append(
                "\n"
            );

            objectives.append(
                Math.min(
                    kills,
                    requiredKills
                )
            );

            objectives.append(
                " / "
            );

            objectives.append(
                requiredKills
            );

            objectives.append(
                "   "
            );

            objectives.append(
                killsComplete
                    ? "✅ Complete"
                    : "❌ Incomplete"
            );

            objectives.append(
                "\n\n"
            );
        }

        if(requiredFloors > 0) {

            objectives.append(
                "**🗺️ Floors Cleared**\n"
            );

            objectives.append(
                progressBar(
                    floorsCleared,
                    requiredFloors
                )
            );

            objectives.append(
                "\n"
            );

            objectives.append(
                Math.min(
                    floorsCleared,
                    requiredFloors
                )
            );

            objectives.append(
                " / "
            );

            objectives.append(
                requiredFloors
            );

            objectives.append(
                "   "
            );

            objectives.append(
                floorsComplete
                    ? "✅ Complete"
                    : "❌ Incomplete"
            );
        }

        embed.addField(
            "📜 OBJECTIVES",
            objectives.toString(),
            false
        );

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        // -----------------------------------------------------
        // OVERALL PROGRESS
        // -----------------------------------------------------

        int completedObjectives =
            0;

        int totalObjectives =
            0;

        if(requiredKills > 0) {

            totalObjectives++;

            if(killsComplete) {

                completedObjectives++;
            }
        }

        if(requiredFloors > 0) {

            totalObjectives++;

            if(floorsComplete) {

                completedObjectives++;
            }
        }

        int completionPercent =
            totalObjectives == 0
                ? 0
                : (int)(
                (
                    completedObjectives
                    / (double)totalObjectives
                )
                * 100.0
            );

        embed.addField(
            "📊 OVERALL PROGRESS",
            overallProgressBar(
                completionPercent
            )
                + "\n"
                + "**"
                + completionPercent
                + "% Complete**",
            false
        );

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        // -----------------------------------------------------
        // REWARD
        // -----------------------------------------------------

        if(targetEvent.rewardName != null &&
            !targetEvent.rewardName
                .trim()
                .isEmpty()) {

            if(eventComplete) {

                embed.addField(
                    "🎁 REWARD UNLOCKED",
                    "✅ **"
                        + targetEvent.rewardName
                        + "**",
                    false
                );
            }
            else {

                embed.addField(
                    "🎁 EVENT REWARD",
                    "🔒 **"
                        + targetEvent.rewardName
                        + "**\n"
                        + "_Complete all objectives to unlock this reward._",
                    false
                );
            }
        }

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        // -----------------------------------------------------
        // STATUS
        // -----------------------------------------------------

        if(eventComplete) {

            embed.addField(
                "🏆 STATUS",
                "**MISSION COMPLETE**\n"
                    + "All required objectives have been verified.",
                false
            );
        }
        else {

            embed.addField(
                "⚔️ STATUS",
                "**MISSION ACTIVE**\n"
                    + "Return when all objectives have been completed.",
                false
            );
        }

        embed.setFooter(
            "Realms of Fate • Community Operations"
        );

        embed.setTimestamp(
            Instant.now()
        );

        return embed;
    }

    // =========================================================
    // EVENT DETAILS EMBED
    // =========================================================

    private EmbedBuilder buildEventDetailsEmbed(
        BotState.EventData targetEvent) {

        EmbedBuilder embed =
            new EmbedBuilder();

        embed.setTitle(
            "📜 " + targetEvent.name
        );

        embed.setColor(
            targetEvent.active
                ? 0x5865F2
                : 0x747F8D
        );

        String description =
            safeDescription(
                targetEvent.description
            );

        embed.setDescription(
            "**EVENT BRIEFING**\n"
                + divider()
                + "\n"
                + description
        );

        StringBuilder objectives =
            new StringBuilder();

        if(targetEvent.objectives != null) {

            if(targetEvent.objectives.kills > 0) {

                objectives.append(
                    "⚔️ Defeat **"
                );

                objectives.append(
                    targetEvent
                        .objectives
                        .kills
                );

                objectives.append(
                    "** enemies\n"
                );
            }

            if(targetEvent.objectives.floorsCleared > 0) {

                objectives.append(
                    "🗺️ Clear **"
                );

                objectives.append(
                    targetEvent
                        .objectives
                        .floorsCleared
                );

                objectives.append(
                    "** floors\n"
                );
            }
        }

        embed.addField(
            "📜 OBJECTIVES",
            objectives.length() == 0
                ? "No objectives configured."
                : objectives.toString(),
            false
        );

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        if(targetEvent.rewardName != null &&
            !targetEvent.rewardName
                .trim()
                .isEmpty()) {

            embed.addField(
                "🎁 EVENT REWARD",
                "**"
                    + targetEvent.rewardName
                    + "**",
                false
            );
        }

        embed.addField(
            "📡 EVENT STATUS",
            targetEvent.active
                ? "🟢 **ACTIVE**"
                : "🔴 **INACTIVE**",
            false
        );

        embed.setFooter(
            "Realms of Fate • Event Briefing"
        );

        embed.setTimestamp(
            Instant.now()
        );

        return embed;
    }

    // =========================================================
    // CHAMPIONS
    // =========================================================

    private void showChampions(
        SlashCommandInteractionEvent interaction) {

        String eventId =
            interaction
                .getOption("event")
                .getAsString();

        BotState state =
            StateStore.load();

        BotState.EventData targetEvent =
            findEvent(
                state,
                eventId
            );

        if(targetEvent == null) {

            interaction.reply(
                    "I could not find that community event."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        interaction.replyEmbeds(
                buildChampionsEmbed(
                    targetEvent
                ).build()
            )
            .setEphemeral(true)
            .queue();
    }

    private EmbedBuilder buildChampionsEmbed(
        BotState.EventData targetEvent) {

        EmbedBuilder embed =
            new EmbedBuilder();

        embed.setTitle(
            "⚔️ Champions of "
                + targetEvent.name
        );

        embed.setColor(
            0x5865F2
        );

        embed.setDescription(
            "**CHAMPION ROSTER**\n"
                + divider()
        );

        StringBuilder roster =
            new StringBuilder();

        int activeChampions =
            0;

        for(int slot = 1;
            slot <= targetEvent.championSlots;
            slot++) {

            BotState.ChampionData slotChampion =
                null;

            if(targetEvent.champions != null) {

                for(BotState.ChampionData champion :
                    targetEvent.champions) {

                    if(champion.slot == slot) {

                        slotChampion =
                            champion;

                        break;
                    }
                }
            }

            if(slotChampion == null) {

                roster.append(
                    "**Slot "
                );

                roster.append(
                    slot
                );

                roster.append(
                    "**  •  _Available_\n"
                );

                continue;
            }

            boolean active =
                "ACTIVE".equalsIgnoreCase(
                    slotChampion.status
                );

            if(active) {

                activeChampions++;
            }

            roster.append(
                active
                    ? "⚔️ "
                    : "💀 "
            );

            roster.append(
                "**Slot "
            );

            roster.append(
                slot
            );

            roster.append(
                "**  •  "
            );

            roster.append(
                slotChampion.playerName
            );

            if(!active) {

                roster.append(
                    "  •  Fallen"
                );
            }

            roster.append(
                "\n"
            );
        }

        embed.addField(
            "🛡️ ROSTER",
            roster.toString(),
            false
        );

        embed.addField(
            "\u200B",
            divider(),
            false
        );

        embed.addField(
            "⚔️ Active Champions",
            "**"
                + activeChampions
                + " / "
                + targetEvent.championSlots
                + "**",
            true
        );

        embed.addField(
            "📡 Event Status",
            targetEvent.active
                ? "🟢 Active"
                : "🔴 Inactive",
            true
        );

        embed.setFooter(
            "Realms of Fate • Champion Roster"
        );

        embed.setTimestamp(
            Instant.now()
        );

        return embed;
    }

    // =========================================================
    // ADD CHAMPION
    // =========================================================

    private void addChampion(
        SlashCommandInteractionEvent interaction) {

        if(interaction.getMember() == null ||
            !interaction
                .getMember()
                .hasPermission(
                    Permission.MANAGE_SERVER
                )) {

            interaction.reply(
                    "You do not have permission to manage champions."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        String eventId =
            interaction
                .getOption("event")
                .getAsString();

        User user =
            interaction
                .getOption("user")
                .getAsUser();

        BotState state =
            StateStore.load();

        BotState.EventData targetEvent =
            findEvent(
                state,
                eventId
            );

        if(targetEvent == null) {

            interaction.reply(
                    "I could not find that community event."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        if(!targetEvent.active) {

            interaction.reply(
                    targetEvent.name
                        + " is not currently active."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        if(targetEvent.champions == null) {

            targetEvent.champions =
                new ArrayList<
                    BotState.ChampionData
                    >();
        }

        for(BotState.ChampionData champion :
            targetEvent.champions) {

            if(user
                .getId()
                .equals(
                    champion.discordId
                )) {

                interaction.reply(
                        user.getName()
                            + " is already a champion in "
                            + targetEvent.name
                            + "."
                    )
                    .setEphemeral(true)
                    .queue();

                return;
            }
        }

        int availableSlot =
            -1;

        for(int slot = 1;
            slot <= targetEvent.championSlots;
            slot++) {

            boolean occupied =
                false;

            for(BotState.ChampionData champion :
                targetEvent.champions) {

                if(champion.slot == slot
                    &&
                    "ACTIVE".equalsIgnoreCase(
                        champion.status
                    )) {

                    occupied =
                        true;

                    break;
                }
            }

            if(!occupied) {

                availableSlot =
                    slot;

                break;
            }
        }

        if(availableSlot == -1) {

            interaction.reply(
                    targetEvent.name
                        + " already has all "
                        + targetEvent.championSlots
                        + " champion slots filled."
                )
                .setEphemeral(true)
                .queue();

            return;
        }

        BotState.ChampionData champion =
            new BotState.ChampionData();

        champion.discordId =
            user.getId();

        champion.playerName =
            user.getName();

        champion.slot =
            availableSlot;

        champion.status =
            "ACTIVE";

        targetEvent.champions.add(
            champion
        );

        StateStore.save(
            state
        );

        interaction.reply(
            "⚔️ **"
                + user.getName()
                + "** has been chosen as Champion #"
                + availableSlot
                + " for **"
                + targetEvent.name
                + "**!"
        ).queue();
    }

    // =========================================================
    // REMOVE CHAMPION
    // =========================================================

    private void removeChampion(
        SlashCommandInteractionEvent interaction) {

        interaction
            .deferReply(true)
            .queue(
                hook -> {

                    try {

                        if(interaction.getMember() == null
                            ||
                            !interaction
                                .getMember()
                                .hasPermission(
                                    Permission.MANAGE_SERVER
                                )) {

                            hook.editOriginal(
                                "You do not have permission to manage champions."
                            ).queue();

                            return;
                        }

                        String eventId =
                            interaction
                                .getOption("event")
                                .getAsString();

                        User user =
                            interaction
                                .getOption("user")
                                .getAsUser();

                        BotState state =
                            StateStore.load();

                        BotState.EventData targetEvent =
                            findEvent(
                                state,
                                eventId
                            );

                        if(targetEvent == null) {

                            hook.editOriginal(
                                "I could not find that community event."
                            ).queue();

                            return;
                        }

                        if(targetEvent.champions == null ||
                            targetEvent.champions.isEmpty()) {

                            hook.editOriginal(
                                "**"
                                    + targetEvent.name
                                    + "** does not currently have any champions."
                            ).queue();

                            return;
                        }

                        BotState.ChampionData targetChampion =
                            null;

                        for(BotState.ChampionData champion :
                            targetEvent.champions) {

                            if(champion.discordId != null &&
                                champion.discordId.equals(
                                    user.getId()
                                )) {

                                targetChampion =
                                    champion;

                                break;
                            }
                        }

                        if(targetChampion == null) {

                            hook.editOriginal(
                                "**"
                                    + user.getName()
                                    + "** is not a champion in **"
                                    + targetEvent.name
                                    + "**."
                            ).queue();

                            return;
                        }

                        int oldSlot =
                            targetChampion.slot;

                        String oldName =
                            targetChampion.playerName;

                        targetEvent
                            .champions
                            .remove(
                                targetChampion
                            );

                        StateStore.save(
                            state
                        );

                        hook.editOriginal(
                            "🛡️ **"
                                + oldName
                                + "** has been removed from **"
                                + targetEvent.name
                                + "**.\n"
                                + "Champion Slot #"
                                + oldSlot
                                + " is now available."
                        ).queue();
                    }
                    catch(Exception exception) {

                        exception.printStackTrace();

                        hook.editOriginal(
                            "Something went wrong while removing the champion."
                        ).queue();
                    }
                }
            );
    }

    // =========================================================
    // FIND EVENT
    // =========================================================

    private BotState.EventData findEvent(
        BotState state,
        String eventId) {

        if(state == null ||
            state.events == null ||
            eventId == null) {

            return null;
        }

        for(BotState.EventData communityEvent :
            state.events) {

            if(communityEvent.eventId != null &&
                communityEvent.eventId
                    .equalsIgnoreCase(
                        eventId
                    )) {

                return communityEvent;
            }
        }

        return null;
    }

    // =========================================================
    // PROGRESS BAR
    // =========================================================

    private String progressBar(
        int current,
        int required) {

        if(required <= 0) {

            return "━━━━━━━━━━";
        }

        double ratio =
            current / (double)required;

        if(ratio < 0.0) {

            ratio =
                0.0;
        }

        if(ratio > 1.0) {

            ratio =
                1.0;
        }

        int filled =
            (int)Math.round(
                ratio * 10.0
            );

        StringBuilder bar =
            new StringBuilder();

        for(int i = 0;
            i < 10;
            i++) {

            if(i < filled) {

                bar.append(
                    "█"
                );
            }
            else {

                bar.append(
                    "░"
                );
            }
        }

        int percent =
            (int)Math.round(
                ratio * 100.0
            );

        return bar.toString()
            + "  "
            + percent
            + "%";
    }

    // =========================================================
    // OVERALL PROGRESS BAR
    // =========================================================

    private String overallProgressBar(
        int percent) {

        if(percent < 0) {

            percent =
                0;
        }

        if(percent > 100) {

            percent =
                100;
        }

        int filled =
            (int)Math.round(
                percent / 10.0
            );

        StringBuilder bar =
            new StringBuilder();

        for(int i = 0;
            i < 10;
            i++) {

            if(i < filled) {

                bar.append(
                    "█"
                );
            }
            else {

                bar.append(
                    "░"
                );
            }
        }

        return bar.toString();
    }

    // =========================================================
    // DIVIDER
    // =========================================================

    private String divider() {

        return "━━━━━━━━━━━━━━━━━━━━";
    }

    // =========================================================
    // SAFE DESCRIPTION
    // =========================================================

    private String safeDescription(
        String description) {

        if(description == null ||
            description.trim().isEmpty()) {

            return
                "Complete the assigned objectives "
                    + "to finish this community event.";
        }

        return description;
    }
}
