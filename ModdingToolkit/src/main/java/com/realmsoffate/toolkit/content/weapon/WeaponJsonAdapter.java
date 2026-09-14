package com.realmsoffate.toolkit.content.weapon;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.Map;

public final class WeaponJsonAdapter {

    private WeaponJsonAdapter() {
    }

    /**
     * Reads a weapon JSON object into a WeaponDefinition.
     *
     * Known properties are placed into typed fields.
     * Every unknown property is preserved inside extraProperties.
     */
    public static WeaponDefinition fromJson(
        String jsonText
    ) {

        if (
            jsonText == null
                || jsonText.trim().isEmpty()
        ) {

            throw new IllegalArgumentException(
                "Weapon JSON cannot be empty."
            );
        }

        JsonReader reader =
            new JsonReader();

        JsonValue root =
            reader.parse(
                jsonText
            );

        return fromJsonValue(
            root
        );
    }

    /**
     * Reads an already-parsed JsonValue.
     */
    public static WeaponDefinition fromJsonValue(
        JsonValue root
    ) {

        if (root == null) {

            throw new IllegalArgumentException(
                "Weapon JSON cannot be null."
            );
        }

        if (!root.isObject()) {

            throw new IllegalArgumentException(
                "Weapon JSON must be an object."
            );
        }

        WeaponDefinition weapon =
            new WeaponDefinition();

        weapon.setClassName(
            getOptionalString(
                root,
                "class"
            )
        );

        weapon.setName(
            getOptionalString(
                root,
                "name"
            )
        );

        weapon.setItemType(
            getOptionalString(
                root,
                "itemType"
            )
        );

        if (root.has("tex")) {

            weapon.setTex(
                root.getInt(
                    "tex",
                    0
                )
            );
        }

        weapon.setTexAtlas(
            getOptionalString(
                root,
                "texAtlas"
            )
        );

        /*
         * Preserve everything that the toolkit does not
         * explicitly understand yet.
         */
        JsonValue child =
            root.child;

        while (child != null) {

            String propertyName =
                child.name;

            if (
                !isKnownProperty(
                    propertyName
                )
            ) {

                weapon.putExtraProperty(
                    propertyName,
                    copyJsonValue(
                        child
                    )
                );
            }

            child =
                child.next;
        }

        return weapon;
    }

    /**
     * Converts a WeaponDefinition back into formatted JSON.
     */
    public static String toJson(
        WeaponDefinition weapon
    ) {

        JsonValue root =
            toJsonValue(
                weapon
            );

        return root.prettyPrint(
            JsonWriter.OutputType.json,
            120
        );
    }

    /**
     * Converts a WeaponDefinition into a JsonValue object.
     */
    public static JsonValue toJsonValue(
        WeaponDefinition weapon
    ) {

        if (weapon == null) {

            throw new IllegalArgumentException(
                "Weapon cannot be null."
            );
        }

        JsonValue root =
            new JsonValue(
                JsonValue.ValueType.object
            );

        addStringIfPresent(
            root,
            "class",
            weapon.getClassName()
        );

        addStringIfPresent(
            root,
            "itemType",
            weapon.getItemType()
        );

        addStringIfPresent(
            root,
            "name",
            weapon.getName()
        );

        root.addChild(
            "tex",
            new JsonValue(
                weapon.getTex()
            )
        );

        addStringIfPresent(
            root,
            "texAtlas",
            weapon.getTexAtlas()
        );

        /*
         * Restore every unsupported / advanced property.
         *
         * This is the important part that makes the adapter
         * lossless for properties the toolkit does not know yet.
         */
        for (
            Map.Entry<String, JsonValue> entry
            : weapon.getExtraProperties().entrySet()
        ) {

            String propertyName =
                entry.getKey();

            JsonValue propertyValue =
                entry.getValue();

            if (
                propertyName == null
                    || propertyValue == null
            ) {

                continue;
            }

            /*
             * Known properties always come from the typed
             * WeaponDefinition fields.
             *
             * Do not allow an extra property to accidentally
             * overwrite one.
             */
            if (
                isKnownProperty(
                    propertyName
                )
            ) {

                continue;
            }

            root.addChild(
                propertyName,
                copyJsonValue(
                    propertyValue
                )
            );
        }

        return root;
    }

    private static boolean isKnownProperty(
        String propertyName
    ) {

        if (propertyName == null) {
            return false;
        }

        return propertyName.equals("class")
            || propertyName.equals("name")
            || propertyName.equals("itemType")
            || propertyName.equals("tex")
            || propertyName.equals("texAtlas");
    }

    private static String getOptionalString(
        JsonValue root,
        String propertyName
    ) {

        JsonValue value =
            root.get(
                propertyName
            );

        if (
            value == null
                || value.isNull()
        ) {

            return null;
        }

        return value.asString();
    }

    private static void addStringIfPresent(
        JsonValue root,
        String propertyName,
        String value
    ) {

        if (
            value == null
                || value.trim().isEmpty()
        ) {

            return;
        }

        root.addChild(
            propertyName,
            new JsonValue(
                value
            )
        );
    }

    /**
     * Makes a deep copy of a JsonValue.
     *
     * We do this instead of reusing the original JsonValue because
     * JsonValue nodes contain parent/next/previous links.
     *
     * Reusing the exact node in another JSON tree could corrupt
     * the original tree.
     */
    private static JsonValue copyJsonValue(
        JsonValue value
    ) {

        String json =
            value.toJson(
                JsonWriter.OutputType.json
            );

        JsonReader reader =
            new JsonReader();

        return reader.parse(
            json
        );
    }
}
