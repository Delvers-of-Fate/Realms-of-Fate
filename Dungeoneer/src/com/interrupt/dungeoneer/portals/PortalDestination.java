package com.interrupt.dungeoneer.portals;

public class PortalDestination {

    public String id = "";
    public String name = "Unnamed Destination";

    public String region = "";

    // "fixed" or "generated"
    public String type = "fixed";

    // Used for fixed destinations.
    public String level = "";

    // Used later for generated destinations.
    public String generator = "";

    public boolean unlocked = true;

    public PortalDestination() {
    }

    public PortalDestination(
        String id,
        String name,
        String region,
        String type,
        String level,
        boolean unlocked
    ) {

        this.id = id;
        this.name = name;
        this.region = region;
        this.type = type;
        this.level = level;
        this.unlocked = unlocked;
    }

    public boolean isFixed() {
        return type != null
            && type.equalsIgnoreCase("fixed");
    }

    public boolean isGenerated() {
        return type != null
            && type.equalsIgnoreCase("generated");
    }
}
