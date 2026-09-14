package com.realmsoffate.toolkit.content.metadata;

import java.util.HashMap;
import java.util.Map;

public final class ContentPropertyRegistry {

    private static final Map<String, PropertyPresentation> presentations =
        new HashMap<String, PropertyPresentation>();

    static {

        /*
         * =========================================================
         * GENERAL
         * =========================================================
         */

        registerEditor(
            "name",
            "Name",
            PropertyCategory.GENERAL
        );

        registerAdvanced(
            "itemType",
            "Item Type",
            PropertyCategory.GENERAL
        );

        registerEditor(
            "description",
            "Description",
            PropertyCategory.GENERAL
        );

        registerAdvanced(
            "id",
            "ID",
            PropertyCategory.GENERAL
        );


        /*
         * =========================================================
         * VISUAL
         * =========================================================
         */

        registerEditor(
            "tex",
            "Sprite Index",
            PropertyCategory.VISUAL
        );

        registerEditor(
            "texAtlas",
            "Sprite Sheet",
            PropertyCategory.VISUAL
        );

        registerEditor(
            "color",
            "Color",
            PropertyCategory.VISUAL
        );

        registerEditor(
            "scale",
            "Scale",
            PropertyCategory.VISUAL
        );

        registerAdvanced(
            "fullbrite",
            "Fullbright",
            PropertyCategory.VISUAL
        );

        registerAdvanced(
            "drawable",
            "Drawable",
            PropertyCategory.VISUAL
        );

        registerAdvanced(
            "detailLevel",
            "Detail Level",
            PropertyCategory.VISUAL
        );


        /*
         * =========================================================
         * COMBAT
         * =========================================================
         */

        registerEditor(
            "damage",
            "Damage",
            PropertyCategory.COMBAT
        );

        registerEditor(
            "range",
            "Range",
            PropertyCategory.COMBAT
        );

        registerEditor(
            "attackSpeed",
            "Attack Speed",
            PropertyCategory.COMBAT
        );

        registerEditor(
            "knockback",
            "Knockback",
            PropertyCategory.COMBAT
        );


        /*
         * =========================================================
         * MAGIC
         * =========================================================
         */

        registerEditor(
            "usesMana",
            "Uses Mana",
            PropertyCategory.MAGIC
        );

        registerEditor(
            "manaCost",
            "Mana Cost",
            PropertyCategory.MAGIC
        );

        registerEditor(
            "usesCharges",
            "Uses Charges",
            PropertyCategory.MAGIC
        );

        registerEditor(
            "magicStatBoostMod",
            "Magic Stat Boost",
            PropertyCategory.MAGIC
        );

        registerEditor(
            "spell",
            "Spell",
            PropertyCategory.MAGIC
        );


        /*
         * =========================================================
         * AUDIO
         * =========================================================
         */

        registerEditor(
            "hitSound",
            "Hit Sound",
            PropertyCategory.AUDIO
        );

        registerEditor(
            "attackSound",
            "Attack Sound",
            PropertyCategory.AUDIO
        );

        registerAdvanced(
            "dropSound",
            "Drop Sound",
            PropertyCategory.AUDIO
        );


        /*
         * =========================================================
         * BEHAVIOR
         * =========================================================
         */

        registerEditor(
            "speed",
            "Speed",
            PropertyCategory.BEHAVIOR
        );

        registerEditor(
            "lifetime",
            "Lifetime",
            PropertyCategory.BEHAVIOR
        );

        registerAdvanced(
            "floating",
            "Floating",
            PropertyCategory.BEHAVIOR
        );

        registerAdvanced(
            "bounces",
            "Bounces",
            PropertyCategory.BEHAVIOR
        );


        /*
         * =========================================================
         * HIDDEN RUNTIME / ENGINE STATE
         *
         * These fields may exist on Entity or inherited classes,
         * but they are generally runtime state rather than useful
         * mod-authoring properties.
         * =========================================================
         */

        registerHidden(
            "attached"
        );

        registerHidden(
            "attachmentTransform"
        );

        registerHidden(
            "calcStepHeight"
        );

        registerHidden(
            "canSleep"
        );

        registerHidden(
            "canStepUpOn"
        );

        registerHidden(
            "collidesWith"
        );

        registerHidden(
            "collision"
        );

        registerHidden(
            "drawDistance"
        );

        registerHidden(
            "drawUpdateTimer"
        );

        registerHidden(
            "editorState"
        );

        registerHidden(
            "hitLoc"
        );

        registerHidden(
            "ignorePlayerCollision"
        );

        registerHidden(
            "inwater"
        );

        registerHidden(
            "isActive"
        );

        registerHidden(
            "isDynamic"
        );

        registerHidden(
            "isOnEntity"
        );

        registerHidden(
            "isOnFloor"
        );

        registerHidden(
            "isSolid"
        );

        registerHidden(
            "isStatic"
        );

        registerHidden(
            "lastSplashTime"
        );

        registerHidden(
            "lastZ"
        );

        registerHidden(
            "lastX"
        );

        registerHidden(
            "lastY"
        );

        registerHidden(
            "stepHeight"
        );

        registerHidden(
            "xa"
        );

        registerHidden(
            "ya"
        );

        registerHidden(
            "za"
        );
    }

    private ContentPropertyRegistry() {
    }

    private static void registerEditor(
        String propertyName,
        String displayName,
        PropertyCategory category
    ) {

        register(
            propertyName,
            displayName,
            category,
            PropertyVisibility.EDITOR
        );
    }

    private static void registerAdvanced(
        String propertyName,
        String displayName,
        PropertyCategory category
    ) {

        register(
            propertyName,
            displayName,
            category,
            PropertyVisibility.ADVANCED
        );
    }

    private static void registerHidden(
        String propertyName
    ) {

        register(
            propertyName,
            makeDisplayName(
                propertyName
            ),
            PropertyCategory.ADVANCED,
            PropertyVisibility.HIDDEN
        );
    }

    private static void register(
        String propertyName,
        String displayName,
        PropertyCategory category,
        PropertyVisibility visibility
    ) {

        presentations.put(
            propertyName,
            new PropertyPresentation(
                propertyName,
                displayName,
                category,
                visibility
            )
        );
    }

    public static PropertyPresentation getPresentation(
        PropertyDefinition property
    ) {

        if (property == null) {

            throw new IllegalArgumentException(
                "Property cannot be null."
            );
        }

        PropertyPresentation presentation =
            presentations.get(
                property.getName()
            );

        if (presentation != null) {
            return presentation;
        }

        /*
         * Unknown engine properties are NOT hidden.
         *
         * They automatically appear under Advanced.
         *
         * This is important because new Realms of Fate fields
         * should become accessible without requiring the toolkit
         * to know about them ahead of time.
         */
        return new PropertyPresentation(
            property.getName(),
            property.getDisplayName(),
            PropertyCategory.ADVANCED,
            PropertyVisibility.ADVANCED
        );
    }

    public static PropertyPresentation getPresentation(
        String propertyName
    ) {

        PropertyPresentation presentation =
            presentations.get(
                propertyName
            );

        if (presentation != null) {
            return presentation;
        }

        return new PropertyPresentation(
            propertyName,
            makeDisplayName(
                propertyName
            ),
            PropertyCategory.ADVANCED,
            PropertyVisibility.ADVANCED
        );
    }

    public static boolean shouldShowInEditor(
        PropertyDefinition property
    ) {

        PropertyPresentation presentation =
            getPresentation(
                property
            );

        return presentation.getVisibility()
            == PropertyVisibility.EDITOR;
    }

    public static boolean shouldShowInAdvanced(
        PropertyDefinition property
    ) {

        PropertyPresentation presentation =
            getPresentation(
                property
            );

        return presentation.getVisibility()
            == PropertyVisibility.ADVANCED;
    }

    public static boolean isHidden(
        PropertyDefinition property
    ) {

        PropertyPresentation presentation =
            getPresentation(
                property
            );

        return presentation.getVisibility()
            == PropertyVisibility.HIDDEN;
    }

    private static String makeDisplayName(
        String propertyName
    ) {

        if (
            propertyName == null
                || propertyName.isEmpty()
        ) {

            return "";
        }

        StringBuilder builder =
            new StringBuilder();

        char[] characters =
            propertyName.toCharArray();

        for (
            int i = 0;
            i < characters.length;
            i++
        ) {

            char current =
                characters[i];

            if (
                i > 0
                    && Character.isUpperCase(
                    current
                )
            ) {

                builder.append(
                    ' '
                );
            }

            if (i == 0) {

                builder.append(
                    Character.toUpperCase(
                        current
                    )
                );

            }
            else {

                builder.append(
                    current
                );
            }
        }

        return builder.toString();
    }

    public static class PropertyPresentation {

        private final String propertyName;
        private final String displayName;

        private final PropertyCategory category;
        private final PropertyVisibility visibility;

        public PropertyPresentation(
            String propertyName,
            String displayName,
            PropertyCategory category,
            PropertyVisibility visibility
        ) {

            this.propertyName =
                propertyName;

            this.displayName =
                displayName;

            this.category =
                category;

            this.visibility =
                visibility;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public PropertyCategory getCategory() {
            return category;
        }

        public PropertyVisibility getVisibility() {
            return visibility;
        }
    }
}
