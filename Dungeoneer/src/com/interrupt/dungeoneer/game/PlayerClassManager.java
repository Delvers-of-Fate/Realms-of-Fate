package com.interrupt.dungeoneer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ArrayMap;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.utils.JsonUtil;
import com.interrupt.dungeoneer.entities.Item;

import java.util.Map;

import com.interrupt.dungeoneer.entities.items.Weapon.DamageType;

public class PlayerClassManager {

    private static final ArrayMap<String, PlayerClassDefinition> classes =
        new ArrayMap<String, PlayerClassDefinition>();

    private static boolean loaded = false;

    public static void loadClasses() {

        classes.clear();

        if(Game.modManager == null) {
            Gdx.app.error("PlayerClasses", "Game.modManager is null. Cannot load classes.");
            return;
        }

        ArrayMap<FileHandle, FileHandle[]> classFiles =
            Game.modManager.getFilesForModsWithSuffix(
                "data/classes",
                "json"
            );

        for(int directoryIndex = 0;
            directoryIndex < classFiles.size;
            directoryIndex++) {

            FileHandle[] files = classFiles.getValueAt(directoryIndex);

            if(files == null) {
                continue;
            }

            for(FileHandle file : files) {

                try {

                    PlayerClassDefinition definition =
                        JsonUtil.fromJson(
                            PlayerClassDefinition.class,
                            file
                        );

                    if(definition == null) {
                        continue;
                    }

                    if(definition.id == null ||
                        definition.id.trim().isEmpty()) {

                        Gdx.app.error(
                            "PlayerClasses",
                            "Class file has no id: " + file.path()
                        );

                        continue;
                    }

                    definition.id = definition.id.trim();

                    // Later-loaded mods with the same ID override
                    // earlier definitions.
                    classes.put(definition.id, definition);

                    Gdx.app.log(
                        "PlayerClasses",
                        "Loaded class: " + definition.id
                    );
                }
                catch(Exception ex) {

                    Gdx.app.error(
                        "PlayerClasses",
                        "Failed loading class: " + file.path(),
                        ex
                    );
                }
            }
        }

        loaded = true;

        Gdx.app.log(
            "PlayerClasses",
            "Loaded " + classes.size + " character classes."
        );
    }

    public static PlayerClassDefinition get(String id) {

        if(!loaded) {
            loadClasses();
        }

        if(id == null) {
            return null;
        }

        return classes.get(id.trim());
    }

    public static void applyClass(Player player, String id) {

        if(player == null) {
            return;
        }

        PlayerClassDefinition definition = get(id);

        if(definition == null) {

            Gdx.app.error(
                "PlayerClasses",
                "Unknown character class: " + id
            );

            return;
        }

        player.playerClass = definition.id;

        player.maxHp = Math.max(1, definition.hp);
        player.hp = player.maxHp;

        player.setMaxMana(
            Math.max(0, definition.mp),
            true
        );

        // Base class stats
        player.stats.ATK = definition.atk;
        player.stats.DEF = definition.def;
        player.stats.DEX = definition.dex;
        player.stats.SPD = definition.spd;
        player.stats.MAG = definition.mag;
        player.stats.END = definition.end;

        // Give starting inventory items
        if(definition.startingItems != null) {

            for(String itemName : definition.startingItems) {

                if(itemName == null || itemName.trim().isEmpty()) {
                    continue;
                }

                Item item = Game.GetItemManager().FindItem(
                    itemName.trim(),
                    Item.ItemCondition.normal
                );

                if(item != null) {

                    player.addToInventory(item);

                    Gdx.app.log(
                        "PlayerClasses",
                        "Gave starting item '"
                            + itemName
                            + "' to class "
                            + definition.id
                    );
                }
                else {

                    Gdx.app.error(
                        "PlayerClasses",
                        "Could not find starting item '"
                            + itemName
                            + "' for class "
                            + definition.id
                    );
                }
            }
        }

        player.jumpHeight = definition.jumpHeight;

        player.sprintSpeedMultiplier = definition.sprintSpeedMultiplier;
        player.crouchSpeedMultiplier = definition.crouchSpeedMultiplier;

        player.manaRegenerationEnabled = definition.manaRegenerationEnabled;
        player.manaRegenRate = definition.manaRegenRate;

        // Clear resistance values from any previously selected class.
        player.classResistances.clear();

        if(definition.resistances != null) {

            for(Map.Entry<String, Float> entry :
                definition.resistances.entrySet()) {

                try {

                    DamageType damageType =
                        DamageType.valueOf(entry.getKey().toUpperCase());

                    float resistance = entry.getValue();

                    player.classResistances.put(
                        damageType,
                        resistance
                    );
                }
                catch(Exception ex) {

                    Gdx.app.error(
                        "PlayerClasses",
                        "Invalid resistance type '"
                            + entry.getKey()
                            + "' in class "
                            + definition.id
                    );
                }
            }
        }
    }

    public static ArrayMap<String, PlayerClassDefinition> getClasses() {

        if(!loaded) {
            loadClasses();
        }

        return classes;
    }

    public static void reload() {
        loaded = false;
        loadClasses();
    }
}
