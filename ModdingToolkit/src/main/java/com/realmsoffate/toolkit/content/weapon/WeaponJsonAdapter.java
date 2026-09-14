package com.realmsoffate.toolkit.content.weapon;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.Map;

public final class WeaponJsonAdapter {

    private WeaponJsonAdapter() {
    }

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

        /*
         * First record every property that actually existed.
         */
        JsonValue child =
            root.child;

        while (child != null) {

            weapon.markPropertyPresent(
                child.name
            );

            child =
                child.next;
        }

        if (root.has("class")) {

            weapon.setClassName(
                getOptionalString(
                    root,
                    "class"
                )
            );
        }

        if (root.has("name")) {

            weapon.setName(
                getOptionalString(
                    root,
                    "name"
                )
            );
        }

        if (root.has("itemType")) {

            weapon.setItemType(
                getOptionalString(
                    root,
                    "itemType"
                )
            );
        }

        if (root.has("tex")) {

            weapon.setTex(
                root.getInt(
                    "tex",
                    0
                )
            );
        }

        if (root.has("texAtlas")) {

            weapon.setTexAtlas(
                getOptionalString(
                    root,
                    "texAtlas"
                )
            );
        }

        /*
         * Preserve every unknown property.
         */
        child =
            root.child;

        while (child != null) {

            if (
                !isKnownProperty(
                    child.name
                )
            ) {

                weapon.putExtraProperty(
                    child.name,
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

        if (
            weapon.wasPropertyPresent(
                "class"
            )
        ) {

            addString(
                root,
                "class",
                weapon.getClassName()
            );
        }

        if (
            weapon.wasPropertyPresent(
                "itemType"
            )
        ) {

            addString(
                root,
                "itemType",
                weapon.getItemType()
            );
        }

        if (
            weapon.wasPropertyPresent(
                "name"
            )
        ) {

            addString(
                root,
                "name",
                weapon.getName()
            );
        }

        if (
            weapon.wasPropertyPresent(
                "tex"
            )
        ) {

            root.addChild(
                "tex",
                new JsonValue(
                    (long) weapon.getTex()
                )
            );
        }

        if (
            weapon.wasPropertyPresent(
                "texAtlas"
            )
        ) {

            addString(
                root,
                "texAtlas",
                weapon.getTexAtlas()
            );
        }

        /*
         * Restore all advanced / unsupported properties.
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

    private static void addString(
        JsonValue root,
        String propertyName,
        String value
    ) {

        if (value == null) {

            root.addChild(
                propertyName,
                new JsonValue(
                    JsonValue.ValueType.nullValue
                )
            );

            return;
        }

        root.addChild(
            propertyName,
            new JsonValue(
                value
            )
        );
    }

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
