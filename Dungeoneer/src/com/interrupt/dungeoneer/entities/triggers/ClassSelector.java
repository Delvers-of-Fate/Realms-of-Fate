package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Item;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.PlayerClass;

public class ClassSelector extends Trigger {

    /** Class this selector gives the player. */
    @EditorProperty(group = "Class")
    public PlayerClass characterClass = PlayerClass.WARRIOR;

    /** Maximum health to give this class */
    @EditorProperty(group = "Class")
    public int startingHealth = 8;

    /** Maximum mana to give this class. */
    @EditorProperty(group = "Class")
    public int startingMana = 20;

    @EditorProperty(group = "Class Loadout")
    public String startingWeapon = "";

    @EditorProperty(group = "Class Loadout")
    public String startingArmor = "";

    public ClassSelector() {
        super();

        triggerType = TriggerType.USE;
        triggerResets = false;
        useVerb = "Choose Class";
    }

    @Override
    public void use(Player player, float projx, float projy) {

        if(player == null) {
            return;
        }

        applyClass(player);

        // Keep normal Delver Trigger functionality.
        // This lets the selector activate doors, movers,
        // effects, other triggers, etc.
        super.use(player, projx, projy);
    }

    private void applyClass(Player player) {

        player.playerClass = characterClass;

        // Apply class health.
        player.maxHp = Math.max(1, startingHealth);
        player.hp = player.maxHp;

        // Apply class mana and refill it.
        player.setMaxMana(Math.max(0, startingMana), true);

        // Give starting equipment.
        giveStartingItem(player, startingWeapon);
        giveStartingItem(player, startingArmor);
    }
    private void giveStartingItem(Player player, String itemName) {

        // Ignore empty editor fields.
        if(itemName == null || itemName.trim().isEmpty()) {
            return;
        }

        Item item = Game.GetItemManager().FindItem(
            itemName.trim(),
            Item.ItemCondition.normal
        );

        if(item == null) {
            return;
        }

        player.addToInventory(item);
    }
}
