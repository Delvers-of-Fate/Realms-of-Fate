package com.realmsoffate.toolkit.content.metadata;

import com.interrupt.dungeoneer.entities.items.Wand;

import java.util.List;

public class ContentPropertyRegistryTest {

    public static void main(
        String[] args
    ) {

        List<PropertyDefinition> properties =
            EnginePropertyScanner.scan(
                Wand.class
            );

        int editorCount = 0;
        int advancedCount = 0;
        int hiddenCount = 0;

        System.out.println(
            "===== NORMAL EDITOR PROPERTIES ====="
        );

        for (
            PropertyDefinition property
            : properties
        ) {

            ContentPropertyRegistry.PropertyPresentation presentation =
                ContentPropertyRegistry.getPresentation(
                    property
                );

            if (
                presentation.getVisibility()
                    == PropertyVisibility.EDITOR
            ) {

                editorCount++;

                System.out.println(
                    presentation.getDisplayName()
                        + " | "
                        + property.getName()
                        + " | "
                        + property.getPropertyType()
                        + " | "
                        + presentation
                        .getCategory()
                        .getDisplayName()
                );
            }
        }


        System.out.println();
        System.out.println(
            "===== ADVANCED PROPERTIES ====="
        );

        for (
            PropertyDefinition property
            : properties
        ) {

            ContentPropertyRegistry.PropertyPresentation presentation =
                ContentPropertyRegistry.getPresentation(
                    property
                );

            if (
                presentation.getVisibility()
                    == PropertyVisibility.ADVANCED
            ) {

                advancedCount++;

                System.out.println(
                    presentation.getDisplayName()
                        + " | "
                        + property.getName()
                        + " | "
                        + property.getPropertyType()
                );
            }
        }


        System.out.println();
        System.out.println(
            "===== HIDDEN PROPERTIES ====="
        );

        for (
            PropertyDefinition property
            : properties
        ) {

            ContentPropertyRegistry.PropertyPresentation presentation =
                ContentPropertyRegistry.getPresentation(
                    property
                );

            if (
                presentation.getVisibility()
                    == PropertyVisibility.HIDDEN
            ) {

                hiddenCount++;

                System.out.println(
                    property.getName()
                );
            }
        }


        System.out.println();
        System.out.println(
            "===== SUMMARY ====="
        );

        System.out.println(
            "Normal Editor: "
                + editorCount
        );

        System.out.println(
            "Advanced: "
                + advancedCount
        );

        System.out.println(
            "Hidden: "
                + hiddenCount
        );

        System.out.println(
            "Total: "
                + properties.size()
        );

        System.out.println();
        System.out.println(
            "Check: "
                + (
                editorCount
                    + advancedCount
                    + hiddenCount
                    == properties.size()
            )
        );
    }
}
