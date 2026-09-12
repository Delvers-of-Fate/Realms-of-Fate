package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.Gdx;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.profile.ProfileManager;
import com.interrupt.dungeoneer.entities.triggers.Trigger;

public class CampChest extends Trigger {

    public CampChest() {

        triggerType = TriggerType.USE;
        useVerb = "Open";
        triggerResets = true;

        // Static world object.
        isDynamic = false;
        isSolid = true;

        // Give it normal entity art for now.
        // We'll replace this with proper chest art later.
        artType = ArtType.entity;

        // Temporary sprite index.
        // This only needs to make the entity visible while testing.
        tex = 0;

        // Physical size of the chest.
        collision.set(
            0.35f,
            0.35f,
            0.45f
        );

        // Don't let the player step onto it.
        canStepUpOn = false;
    }

    @Override
    public void use(
        Player player,
        float projx,
        float projy) {

        if(player == null) {
            return;
        }

        int potionCount =
            ProfileManager.getCampChestAmount(
                "Health Potion"
            );

        Gdx.app.log(
            "CampChest",
            "Camp Chest used. Health Potions: "
                + potionCount
        );

        Game.ShowMessage(
            "Camp Chest - Health Potions: "
                + potionCount,
            3,
            1f
        );
    }
}
