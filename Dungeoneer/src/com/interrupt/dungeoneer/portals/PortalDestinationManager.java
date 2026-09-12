package com.interrupt.dungeoneer.portals;

import com.badlogic.gdx.utils.Array;

public class PortalDestinationManager {

    private static final Array<PortalDestination> destinations =
        new Array<PortalDestination>();

    static {
        registerDefaultDestinations();
    }

    private static void registerDefaultDestinations() {

        destinations.add(
            new PortalDestination(
                "test_dungeon",
                "Test Dungeon",
                "Starting Region",
                "fixed",
                "test",
                true
            )
        );
    }

    public static Array<PortalDestination> getDestinations() {
        return destinations;
    }

    public static Array<PortalDestination> getUnlockedDestinations() {

        Array<PortalDestination> unlocked =
            new Array<PortalDestination>();

        for(PortalDestination destination : destinations) {

            if(destination != null && destination.unlocked) {
                unlocked.add(destination);
            }
        }

        return unlocked;
    }

    public static PortalDestination getDestination(String id) {

        if(id == null) {
            return null;
        }

        for(PortalDestination destination : destinations) {

            if(destination != null
                && destination.id != null
                && destination.id.equalsIgnoreCase(id)) {

                return destination;
            }
        }

        return null;
    }

    public static void addDestination(PortalDestination destination) {

        if(destination == null) {
            return;
        }

        destinations.add(destination);
    }
}
