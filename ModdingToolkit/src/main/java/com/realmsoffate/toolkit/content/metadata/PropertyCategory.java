package com.realmsoffate.toolkit.content.metadata;

public enum PropertyCategory {

    GENERAL(
        "General"
    ),

    VISUAL(
        "Visual"
    ),

    COMBAT(
        "Combat"
    ),

    MAGIC(
        "Magic"
    ),

    AUDIO(
        "Audio"
    ),

    BEHAVIOR(
        "Behavior"
    ),

    ADVANCED(
        "Advanced"
    );

    private final String displayName;

    PropertyCategory(
        String displayName
    ) {

        this.displayName =
            displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
