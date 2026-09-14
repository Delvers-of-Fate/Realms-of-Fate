package com.realmsoffate.toolkit.content.metadata;

import com.interrupt.dungeoneer.entities.items.Wand;

import java.util.List;

public class EnginePropertyScannerTest {

    public static void main(
        String[] args
    ) {

        System.out.println(
            "===== ENGINE PROPERTY SCANNER ====="
        );

        System.out.println(
            "Scanning: "
                + Wand.class.getName()
        );

        System.out.println();

        List<PropertyDefinition> properties =
            EnginePropertyScanner.scan(
                Wand.class
            );

        System.out.println(
            "Properties found: "
                + properties.size()
        );

        System.out.println();

        Class<?> previousClass =
            null;

        for (
            PropertyDefinition property
            : properties
        ) {

            Class<?> declaringClass =
                property.getDeclaringClass();

            if (
                previousClass == null
                    || previousClass != declaringClass
            ) {

                System.out.println();

                System.out.println(
                    "----- "
                        + declaringClass.getName()
                        + " -----"
                );

                previousClass =
                    declaringClass;
            }

            System.out.println(
                property.getName()
                    + " | "
                    + property.getPropertyType()
                    + " | "
                    + property
                    .getJavaType()
                    .getSimpleName()
                    + " | editable="
                    + property.isNormallyEditable()
            );

            if (property.isEnum()) {

                System.out.print(
                    "    enum values: "
                );

                Object[] values =
                    property.getEnumValues();

                for (
                    int i = 0;
                    i < values.length;
                    i++
                ) {

                    if (i > 0) {
                        System.out.print(", ");
                    }

                    System.out.print(
                        values[i]
                    );
                }

                System.out.println();
            }
        }

        System.out.println();

        System.out.println(
            "===== DONE ====="
        );
    }
}
