package com.realmsoffate.toolkit.ui.editors;

import com.interrupt.dungeoneer.entities.items.Wand;

import com.realmsoffate.toolkit.content.metadata.ContentPropertyRegistry;
import com.realmsoffate.toolkit.content.metadata.EnginePropertyScanner;
import com.realmsoffate.toolkit.content.metadata.PropertyCategory;
import com.realmsoffate.toolkit.content.metadata.PropertyDefinition;
import com.realmsoffate.toolkit.content.metadata.PropertyVisibility;

import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.ToolkitStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WeaponEditorPanel extends JPanel {

    private final List<PropertyDefinition> properties;

    /*
     * Keeps track of which Swing control belongs
     * to which engine property.
     *
     * Later this is how Save will read the values.
     */
    private final Map<String, JComponent> propertyControls =
        new LinkedHashMap<String, JComponent>();

    private JPanel normalPropertiesPanel;
    private JPanel advancedPropertiesPanel;

    private JButton saveButton;
    private JButton advancedButton;

    private boolean advancedVisible = false;

    public WeaponEditorPanel() {

        /*
         * Wand is being used as our first weapon test class
         * because it gives us normal Item/Entity inheritance
         * plus the magic-related weapon properties we've
         * already been working with.
         *
         * Later the editor will choose the actual engine class
         * based on Weapon Type.
         */
        properties =
            EnginePropertyScanner.scan(
                Wand.class
            );

        buildEditor();
    }

    private void buildEditor() {

        setLayout(
            new BorderLayout()
        );

        setBackground(
            ToolkitColors.BACKGROUND
        );

        add(
            createHeader(),
            BorderLayout.NORTH
        );

        add(
            createScrollableEditor(),
            BorderLayout.CENTER
        );

        add(
            createFooter(),
            BorderLayout.SOUTH
        );
    }

    private JPanel createHeader() {

        JPanel header =
            new JPanel();

        header.setOpaque(false);

        header.setBorder(
            new EmptyBorder(
                22,
                28,
                14,
                28
            )
        );

        header.setLayout(
            new BoxLayout(
                header,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "WEAPON EDITOR"
            );

        title.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        title.setFont(
            title
                .getFont()
                .deriveFont(
                    Font.BOLD,
                    24f
                )
        );

        JLabel description =
            new JLabel(
                "Create and configure a weapon for your mod."
            );

        ToolkitStyles.styleDescription(
            description
        );

        title.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        description.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        header.add(
            title
        );

        header.add(
            Box.createVerticalStrut(
                5
            )
        );

        header.add(
            description
        );

        return header;
    }

    private JScrollPane createScrollableEditor() {

        JPanel container =
            new JPanel();

        container.setBackground(
            ToolkitColors.BACKGROUND
        );

        container.setBorder(
            new EmptyBorder(
                5,
                28,
                20,
                28
            )
        );

        container.setLayout(
            new BoxLayout(
                container,
                BoxLayout.Y_AXIS
            )
        );

        normalPropertiesPanel =
            createNormalPropertiesPanel();

        advancedPropertiesPanel =
            createAdvancedPropertiesPanel();

        advancedPropertiesPanel.setVisible(
            false
        );

        normalPropertiesPanel.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        advancedPropertiesPanel.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        container.add(
            normalPropertiesPanel
        );

        container.add(
            Box.createVerticalStrut(
                16
            )
        );

        container.add(
            advancedPropertiesPanel
        );

        container.add(
            Box.createVerticalGlue()
        );

        JScrollPane scrollPane =
            new JScrollPane(
                container
            );

        scrollPane.setBorder(
            BorderFactory.createEmptyBorder()
        );

        scrollPane.getVerticalScrollBar()
            .setUnitIncrement(
                18
            );

        scrollPane.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        return scrollPane;
    }

    private JPanel createNormalPropertiesPanel() {

        JPanel container =
            new JPanel();

        container.setOpaque(false);

        container.setLayout(
            new BoxLayout(
                container,
                BoxLayout.Y_AXIS
            )
        );

        Map<PropertyCategory, List<PropertyDefinition>> grouped =
            groupNormalProperties();

        for (
            PropertyCategory category
            : PropertyCategory.values()
        ) {

            if (
                category
                    == PropertyCategory.ADVANCED
            ) {

                continue;
            }

            List<PropertyDefinition> categoryProperties =
                grouped.get(
                    category
                );

            if (
                categoryProperties == null
                    || categoryProperties.isEmpty()
            ) {

                continue;
            }

            JPanel section =
                createPropertySection(
                    category.getDisplayName(),
                    categoryProperties,
                    false
                );

            section.setAlignmentX(
                Component.LEFT_ALIGNMENT
            );

            container.add(
                section
            );

            container.add(
                Box.createVerticalStrut(
                    14
                )
            );
        }

        return container;
    }

    private JPanel createAdvancedPropertiesPanel() {

        List<PropertyDefinition> advancedProperties =
            new ArrayList<PropertyDefinition>();

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

                advancedProperties.add(
                    property
                );
            }
        }

        JPanel container =
            new JPanel();

        container.setOpaque(false);

        container.setLayout(
            new BoxLayout(
                container,
                BoxLayout.Y_AXIS
            )
        );

        if (
            advancedProperties.isEmpty()
        ) {

            JLabel empty =
                new JLabel(
                    "No advanced properties."
                );

            ToolkitStyles.styleDescription(
                empty
            );

            container.add(
                empty
            );

            return container;
        }

        JPanel section =
            createPropertySection(
                "Advanced Properties",
                advancedProperties,
                true
            );

        section.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        container.add(
            section
        );

        return container;
    }

    private Map<PropertyCategory, List<PropertyDefinition>>
    groupNormalProperties() {

        Map<PropertyCategory, List<PropertyDefinition>> grouped =
            new EnumMap<PropertyCategory, List<PropertyDefinition>>(
                PropertyCategory.class
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
                    != PropertyVisibility.EDITOR
            ) {

                continue;
            }

            PropertyCategory category =
                presentation.getCategory();

            List<PropertyDefinition> categoryProperties =
                grouped.get(
                    category
                );

            if (
                categoryProperties == null
            ) {

                categoryProperties =
                    new ArrayList<PropertyDefinition>();

                grouped.put(
                    category,
                    categoryProperties
                );
            }

            categoryProperties.add(
                property
            );
        }

        return grouped;
    }

    private JPanel createPropertySection(
        String sectionName,
        List<PropertyDefinition> sectionProperties,
        boolean advanced
    ) {

        JPanel section =
            new JPanel();

        ToolkitStyles.styleCard(
            section
        );

        section.setLayout(
            new BoxLayout(
                section,
                BoxLayout.Y_AXIS
            )
        );

        section.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                Integer.MAX_VALUE
            )
        );

        JLabel title =
            new JLabel(
                sectionName.toUpperCase()
            );

        ToolkitStyles.styleSectionTitle(
            title
        );

        title.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        section.add(
            title
        );

        section.add(
            Box.createVerticalStrut(
                14
            )
        );

        for (
            int i = 0;
            i < sectionProperties.size();
            i++
        ) {

            PropertyDefinition property =
                sectionProperties.get(
                    i
                );

            section.add(
                createPropertyRow(
                    property,
                    advanced
                )
            );

            if (
                i
                    < sectionProperties.size() - 1
            ) {

                section.add(
                    Box.createVerticalStrut(
                        8
                    )
                );
            }
        }

        return section;
    }

    private JPanel createPropertyRow(
        PropertyDefinition property,
        boolean advanced
    ) {

        JPanel row =
            new JPanel(
                new BorderLayout(
                    14,
                    0
                )
            );

        row.setOpaque(false);

        row.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                38
            )
        );

        ContentPropertyRegistry.PropertyPresentation presentation =
            ContentPropertyRegistry.getPresentation(
                property
            );

        JLabel label =
            new JLabel(
                presentation.getDisplayName()
            );

        label.setForeground(
            advanced
                ? ToolkitColors.TEXT_SECONDARY
                : ToolkitColors.TEXT_PRIMARY
        );

        label.setPreferredSize(
            new Dimension(
                190,
                30
            )
        );

        JComponent control =
            PropertyControlFactory.createControl(
                property
            );

        control.setPreferredSize(
            new Dimension(
                280,
                30
            )
        );

        /*
         * This map becomes important when we implement
         * loading and saving actual values.
         */
        propertyControls.put(
            property.getName(),
            control
        );

        JLabel typeLabel =
            new JLabel(
                property
                    .getJavaType()
                    .getSimpleName()
            );

        typeLabel.setForeground(
            ToolkitColors.TEXT_SECONDARY
        );

        typeLabel.setPreferredSize(
            new Dimension(
                100,
                30
            )
        );

        typeLabel.setToolTipText(
            "Declared by "
                + property
                .getDeclaringClass()
                .getSimpleName()
        );

        row.add(
            label,
            BorderLayout.WEST
        );

        row.add(
            control,
            BorderLayout.CENTER
        );

        row.add(
            typeLabel,
            BorderLayout.EAST
        );

        return row;
    }

    private JPanel createFooter() {

        JPanel footer =
            new JPanel(
                new BorderLayout()
            );

        footer.setBackground(
            ToolkitColors.PANEL
        );

        footer.setBorder(
            new EmptyBorder(
                10,
                18,
                10,
                18
            )
        );

        advancedButton =
            new JButton(
                "Show Advanced Properties"
            );

        saveButton =
            new JButton(
                "Save Weapon"
            );

        ToolkitStyles.styleSecondaryButton(
            advancedButton
        );

        ToolkitStyles.stylePrimaryButton(
            saveButton
        );

        advancedButton.addActionListener(
            e -> toggleAdvancedProperties()
        );

        /*
         * Saving actual Delver data comes after we've proven
         * the generated editor is correct.
         */
        saveButton.setEnabled(
            false
        );

        saveButton.setToolTipText(
            "Saving will be enabled after property binding is connected."
        );

        footer.add(
            advancedButton,
            BorderLayout.WEST
        );

        footer.add(
            saveButton,
            BorderLayout.EAST
        );

        return footer;
    }

    private void toggleAdvancedProperties() {

        advancedVisible =
            !advancedVisible;

        advancedPropertiesPanel.setVisible(
            advancedVisible
        );

        if (advancedVisible) {

            advancedButton.setText(
                "Hide Advanced Properties"
            );

        }
        else {

            advancedButton.setText(
                "Show Advanced Properties"
            );
        }

        revalidate();
        repaint();
    }

    public Map<String, JComponent> getPropertyControls() {
        return propertyControls;
    }

    public JButton getSaveButton() {
        return saveButton;
    }
}
