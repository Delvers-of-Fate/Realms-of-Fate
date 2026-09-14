package com.realmsoffate.toolkit.content.metadata;

public enum PropertyVisibility {

    EDITOR(
        "Editor"
    ),

    ADVANCED(
        "Advanced"
    ),

    HIDDEN(
        "Hidden"
    );

    private final String displayName;

    PropertyVisibility(
        String displayName
    ) {

        this.displayName =
            displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
