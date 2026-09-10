package com.interrupt.dungeoneer.entities.triggers;

import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.entities.Player;
import com.interrupt.dungeoneer.game.PlayerClassManager;

public class ClassSelector extends Trigger {

    /** ID of the JSON character class this selector applies. */
    @EditorProperty(group = "Class")
    public String classId = "warrior";

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

        PlayerClassManager.applyClass(player, classId);


        // Keep normal Delver Trigger functionality.
        super.use(player, projx, projy);
    }
}
