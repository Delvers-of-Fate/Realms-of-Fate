package com.realmsoffate.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;

import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class BotMain extends ListenerAdapter {

    public static void main(String[] args) throws Exception {

        String token = System.getenv("ROF_DISCORD_TOKEN");

        if(token == null || token.trim().isEmpty()) {
            System.err.println("ROF_DISCORD_TOKEN is not set.");
            return;
        }

        JDA jda = JDABuilder
            .createDefault(token)
            .setActivity(Activity.playing("Realms of Fate"))
            .addEventListeners(new BotMain())
            .build();

        jda.awaitReady();

        BotState state = StateStore.load();

        System.out.println(
            "Loaded bot state: "
                + state.events.size()
                + " event(s), "
                + state.claims.size()
                + " claim(s)."
        );

        System.out.println(
            "Connected to Discord. Found "
                + jda.getGuilds().size()
                + " server(s)."
        );

        for(Guild guild : jda.getGuilds()) {

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
                    success -> System.out.println(
                        "/status registered in "
                            + guild.getName()
                    ),
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

    @Override
    public void onCommandAutoCompleteInteraction(
        CommandAutoCompleteInteractionEvent event) {

        boolean championCommand =
            event.getName().equals("champion")
                && (
                "add".equals(event.getSubcommandName())
                    || "remove".equals(event.getSubcommandName())
            );

        boolean championsView =
            event.getName().equals("champions");

        if(!championCommand && !championsView) {
            return;
        }

        if(!event.getFocusedOption()
            .getName()
            .equals("event")) {

            return;
        }

        BotState state = StateStore.load();

        String typed =
            event.getFocusedOption()
                .getValue()
                .toLowerCase();

        java.util.List<
            net.dv8tion.jda.api.interactions.commands.Command.Choice
            > choices =
            new java.util.ArrayList<
                net.dv8tion.jda.api.interactions.commands.Command.Choice
                >();

        for(BotState.EventData communityEvent :
            state.events) {

            if(communityEvent.name == null ||
                communityEvent.eventId == null) {

                continue;
            }

            // /champion add should only show active events.
            if(event.getName().equals("champion")
                && "add".equals(event.getSubcommandName())
                && !communityEvent.active) {

                continue;
            }

            String eventName =
                communityEvent.name.toLowerCase();

            String eventId =
                communityEvent.eventId.toLowerCase();

            if(!typed.isEmpty()
                && !eventName.contains(typed)
                && !eventId.contains(typed)) {

                continue;
            }

            String displayName =
                communityEvent.name;

            // Make inactive events obvious.
            if(!communityEvent.active) {
                displayName += " [INACTIVE]";
            }

            choices.add(
                new net.dv8tion.jda.api.interactions.commands.Command.Choice(
                    displayName,
                    communityEvent.eventId
                )
            );

            // Discord allows at most 25 autocomplete choices.
            if(choices.size() >= 25) {
                break;
            }
        }

        event.replyChoices(choices).queue();
    }

    @Override
    public void onSlashCommandInteraction(
        SlashCommandInteractionEvent event) {

        System.out.println(
            "Received command: /" + event.getName()
        );

        if(event.getName().equals("status")) {

            event.reply(
                "Realms of Fate community system is online."
            ).queue();
        }

        if(event.getName().equals("events")) {
            showActiveEvents(event);
        }

        if(event.getName().equals("champions")) {
            showChampions(event);
            return;
        }

        if(event.getName().equals("champion")) {

            if("add".equals(event.getSubcommandName())) {
                addChampion(event);
            }

            return;
        }

        if("remove".equals(event.getSubcommandName())) {
            removeChampion(event);
            return;
        }

    }

    private void showActiveEvents(
        SlashCommandInteractionEvent interaction) {

        BotState state = StateStore.load();

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle("⚔ Active Realms of Fate Events");

        int activeEventCount = 0;

        for(BotState.EventData event : state.events) {

            if(!event.active) {
                continue;
            }

            activeEventCount++;

            int activeChampions = 0;

            if(event.champions != null) {

                for(BotState.ChampionData champion : event.champions) {

                    if("ACTIVE".equalsIgnoreCase(champion.status)) {
                        activeChampions++;
                    }
                }
            }

            StringBuilder eventInfo = new StringBuilder();

            eventInfo.append("Champions: ")
                .append(activeChampions)
                .append(" / ")
                .append(event.championSlots)
                .append("\n");

            eventInfo.append("Victories: ")
                .append(event.wins)
                .append("\n");

            eventInfo.append("Deaths: ")
                .append(event.deaths);

            embed.addField(
                event.name,
                eventInfo.toString(),
                false
            );
        }

        if(activeEventCount == 0) {

            interaction.reply(
                "There are currently no active community events."
            ).setEphemeral(true).queue();

            return;
        }

        embed.setDescription(
            "Current community challenges across Realms of Fate."
        );

        embed.setFooter(
            activeEventCount + " active event(s)"
        );

        embed.setTimestamp(Instant.now());

        interaction.replyEmbeds(
            embed.build()
        ).queue();
    }

    private void addChampion(
        SlashCommandInteractionEvent interaction) {

        // Only server managers can change champion rosters.
        if(interaction.getMember() == null ||
            !interaction.getMember().hasPermission(
                Permission.MANAGE_SERVER)) {

            interaction.reply(
                "You do not have permission to manage champions."
            ).setEphemeral(true).queue();

            return;
        }

        String eventId =
            interaction.getOption("event").getAsString();

        User user =
            interaction.getOption("user").getAsUser();

        BotState state = StateStore.load();

        BotState.EventData targetEvent = null;

        for(BotState.EventData event : state.events) {

            if(event.eventId != null &&
                event.eventId.equalsIgnoreCase(eventId)) {

                targetEvent = event;
                break;
            }
        }

        if(targetEvent == null) {

            interaction.reply(
                "I could not find that community event."
            ).setEphemeral(true).queue();

            return;
        }

        if(!targetEvent.active) {

            interaction.reply(
                targetEvent.name + " is not currently active."
            ).setEphemeral(true).queue();

            return;
        }

        // Make sure the champion list exists.
        if(targetEvent.champions == null) {
            targetEvent.champions =
                new java.util.ArrayList<BotState.ChampionData>();
        }

        // Prevent the same person from being added twice.
        for(BotState.ChampionData champion :
            targetEvent.champions) {

            if(user.getId().equals(champion.discordId)) {

                interaction.reply(
                    user.getName()
                        + " is already a champion in "
                        + targetEvent.name
                        + "."
                ).setEphemeral(true).queue();

                return;
            }
        }

        // Find the first available champion slot.
        int availableSlot = -1;

        for(int slot = 1;
            slot <= targetEvent.championSlots;
            slot++) {

            boolean occupied = false;

            for(BotState.ChampionData champion :
                targetEvent.champions) {

                if(champion.slot == slot &&
                    "ACTIVE".equalsIgnoreCase(
                        champion.status)) {

                    occupied = true;
                    break;
                }
            }

            if(!occupied) {
                availableSlot = slot;
                break;
            }
        }

        if(availableSlot == -1) {

            interaction.reply(
                targetEvent.name
                    + " already has all "
                    + targetEvent.championSlots
                    + " champion slots filled."
            ).setEphemeral(true).queue();

            return;
        }

        BotState.ChampionData champion =
            new BotState.ChampionData();

        champion.discordId = user.getId();
        champion.playerName = user.getName();
        champion.slot = availableSlot;
        champion.status = "ACTIVE";

        targetEvent.champions.add(champion);

        StateStore.save(state);

        interaction.reply(
            "⚔ **" + user.getName()
                + "** has been chosen as Champion #"
                + availableSlot
                + " for **"
                + targetEvent.name
                + "**!"
        ).queue();
    }

    private void showChampions(
        SlashCommandInteractionEvent interaction) {

        String eventId =
            interaction.getOption("event").getAsString();

        BotState state = StateStore.load();

        BotState.EventData targetEvent = null;

        for(BotState.EventData event : state.events) {

            if(event.eventId != null &&
                event.eventId.equalsIgnoreCase(eventId)) {

                targetEvent = event;
                break;
            }
        }

        if(targetEvent == null) {

            interaction.reply(
                "I could not find that community event."
            ).setEphemeral(true).queue();

            return;
        }

        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(
            "⚔ Champions of " + targetEvent.name
        );

        StringBuilder roster = new StringBuilder();

        int activeChampions = 0;

        for(int slot = 1;
            slot <= targetEvent.championSlots;
            slot++) {

            BotState.ChampionData slotChampion = null;

            if(targetEvent.champions != null) {

                for(BotState.ChampionData champion :
                    targetEvent.champions) {

                    if(champion.slot == slot) {
                        slotChampion = champion;
                        break;
                    }
                }
            }

            if(slotChampion == null) {

                roster.append("**")
                    .append(slot)
                    .append(".** — Empty\n");

                continue;
            }

            boolean active =
                "ACTIVE".equalsIgnoreCase(
                    slotChampion.status
                );

            if(active) {
                activeChampions++;
            }

            String icon =
                active ? "⚔" : "💀";

            roster.append(icon)
                .append(" **")
                .append(slot)
                .append(".** ")
                .append(slotChampion.playerName);

            if(!active) {
                roster.append(" — DEAD");
            }

            roster.append("\n");
        }

        embed.setDescription(
            roster.toString()
        );

        embed.addField(
            "Active Champions",
            activeChampions
                + " / "
                + targetEvent.championSlots,
            true
        );

        embed.addField(
            "Victories",
            String.valueOf(targetEvent.wins),
            true
        );

        embed.addField(
            "Deaths",
            String.valueOf(targetEvent.deaths),
            true
        );

        embed.addField(
            "Event Status",
            targetEvent.active ? "ACTIVE" : "INACTIVE",
            true
        );

        embed.setTimestamp(Instant.now());

        interaction.replyEmbeds(
            embed.build()
        ).queue();
    }

    private void removeChampion(
        SlashCommandInteractionEvent interaction) {

        // Acknowledge the command immediately so Discord
        // does not time out while we process it.
        interaction.deferReply(true).queue(hook -> {

            try {

                if(interaction.getMember() == null ||
                    !interaction.getMember().hasPermission(
                        Permission.MANAGE_SERVER)) {

                    hook.editOriginal(
                        "You do not have permission to manage champions."
                    ).queue();

                    return;
                }

                if(interaction.getOption("event") == null) {

                    hook.editOriginal(
                        "No community event was selected."
                    ).queue();

                    return;
                }

                if(interaction.getOption("user") == null) {

                    hook.editOriginal(
                        "No champion was selected."
                    ).queue();

                    return;
                }

                String eventId =
                    interaction.getOption("event").getAsString();

                User user =
                    interaction.getOption("user").getAsUser();

                System.out.println(
                    "Removing champion "
                        + user.getName()
                        + " from event "
                        + eventId
                );

                BotState state = StateStore.load();

                BotState.EventData targetEvent = null;

                for(BotState.EventData communityEvent :
                    state.events) {

                    if(communityEvent.eventId != null &&
                        communityEvent.eventId.equalsIgnoreCase(
                            eventId)) {

                        targetEvent = communityEvent;
                        break;
                    }
                }

                if(targetEvent == null) {

                    hook.editOriginal(
                        "I could not find the event `" +
                            eventId + "`."
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

                BotState.ChampionData targetChampion = null;

                for(BotState.ChampionData champion :
                    targetEvent.champions) {

                    if(champion.discordId != null &&
                        champion.discordId.equals(
                            user.getId())) {

                        targetChampion = champion;
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

                targetEvent.champions.remove(
                    targetChampion
                );

                StateStore.save(state);

                System.out.println(
                    "Champion removed successfully: "
                        + oldName
                );

                hook.editOriginal(
                    "🛡 **"
                        + oldName
                        + "** has been removed from **"
                        + targetEvent.name
                        + "**.\n"
                        + "Champion Slot #"
                        + oldSlot
                        + " is now empty."
                ).queue();

            }
            catch(Exception e) {

                System.err.println(
                    "ERROR while removing champion:"
                );

                e.printStackTrace();

                hook.editOriginal(
                    "Something went wrong while removing the champion. "
                        + "Check the bot console for the error."
                ).queue();
            }
        });
    }
}
