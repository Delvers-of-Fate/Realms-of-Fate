package com.realmsoffate.toolkit.content.weapon;

public class WeaponJsonAdapterTest {

    public static void main(
        String[] args
    ) {

        String originalJson =
            "{\n"
                + "  \"class\": \"com.interrupt.dungeoneer.entities.items.Wand\",\n"
                + "  \"itemType\": \"wand\",\n"
                + "  \"name\": \"Test Wand\",\n"
                + "  \"tex\": 12,\n"
                + "  \"texAtlas\": \"items\",\n"
                + "  \"magicStatBoostMod\": 6,\n"
                + "  \"usesCharges\": false,\n"
                + "  \"usesMana\": true,\n"
                + "  \"customProperty\": \"DO NOT LOSE ME\",\n"
                + "  \"spell\": {\n"
                + "    \"damageType\": \"MAGIC\",\n"
                + "    \"manaCost\": 5,\n"
                + "    \"someFutureSpellField\": 123\n"
                + "  }\n"
                + "}";

        System.out.println(
            "===== ORIGINAL ====="
        );

        System.out.println(
            originalJson
        );

        WeaponDefinition weapon =
            WeaponJsonAdapter.fromJson(
                originalJson
            );

        System.out.println();
        System.out.println(
            "===== TOOLKIT READ ====="
        );

        System.out.println(
            "Name: "
                + weapon.getName()
        );

        System.out.println(
            "Class: "
                + weapon.getClassName()
        );

        System.out.println(
            "Item Type: "
                + weapon.getItemType()
        );

        System.out.println(
            "Texture: "
                + weapon.getTex()
        );

        System.out.println(
            "Texture Atlas: "
                + weapon.getTexAtlas()
        );

        System.out.println(
            "Unknown properties preserved: "
                + weapon
                .getExtraProperties()
                .size()
        );

        /*
         * Simulate changing something through the visual editor.
         */
        weapon.setName(
            "Edited Test Wand"
        );

        String savedJson =
            WeaponJsonAdapter.toJson(
                weapon
            );

        System.out.println();
        System.out.println(
            "===== SAVED ====="
        );

        System.out.println(
            savedJson
        );

        System.out.println();
        System.out.println(
            "===== PRESERVATION CHECK ====="
        );

        check(
            savedJson,
            "\"customProperty\"",
            "Custom property"
        );

        check(
            savedJson,
            "\"magicStatBoostMod\"",
            "Magic stat property"
        );

        check(
            savedJson,
            "\"usesCharges\"",
            "Charges property"
        );

        check(
            savedJson,
            "\"usesMana\"",
            "Mana property"
        );

        check(
            savedJson,
            "\"spell\"",
            "Nested spell object"
        );

        check(
            savedJson,
            "\"someFutureSpellField\"",
            "Unknown nested spell property"
        );

        check(
            savedJson,
            "Edited Test Wand",
            "Edited name"
        );
    }

    private static void check(
        String json,
        String expected,
        String description
    ) {

        if (
            json.contains(
                expected
            )
        ) {

            System.out.println(
                "[PASS] "
                    + description
            );
        }
        else {

            System.err.println(
                "[FAIL] "
                    + description
            );
        }
    }
}
