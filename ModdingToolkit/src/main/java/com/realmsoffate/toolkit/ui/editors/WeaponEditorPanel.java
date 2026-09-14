package com.realmsoffate.toolkit.ui.editors;

import com.realmsoffate.toolkit.content.metadata.ContentPropertyRegistry;
import com.realmsoffate.toolkit.content.metadata.EnginePropertyScanner;
import com.realmsoffate.toolkit.content.metadata.PropertyCategory;
import com.realmsoffate.toolkit.content.metadata.PropertyDefinition;
import com.realmsoffate.toolkit.content.metadata.PropertyVisibility;

import com.realmsoffate.toolkit.content.weapon.WeaponDefinition;
import com.realmsoffate.toolkit.content.weapon.WeaponJsonAdapter;
import com.realmsoffate.toolkit.content.weapon.WeaponKind;

import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.ToolkitStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WeaponEditorPanel extends JPanel {

    private WeaponKind selectedWeaponKind =
        WeaponKind.SWORD;

    private List<PropertyDefinition> properties;

    private PropertyValueBinder valueBinder;

    private WeaponDefinition weapon;

    private final Map<String, JComponent> propertyControls =
        new LinkedHashMap<String, JComponent>();

    private JComboBox<WeaponKind> weaponTypeComboBox;

    private JPanel editorHost;

    private JPanel advancedPropertiesPanel;

    private JTextArea jsonPreviewArea;

    private JButton saveButton;
    private JButton advancedButton;

    private boolean advancedVisible = false;

    private boolean changingWeaponType = false;

    public WeaponEditorPanel() {

        properties =
            EnginePropertyScanner.scan(
                selectedWeaponKind.getEngineClass()
            );

        valueBinder =
            new PropertyValueBinder(
                properties
            );

        weapon =
            createDefaultWeapon(
                selectedWeaponKind
            );

        buildEditor();

        rebuildEditorArea();
    }

    private WeaponDefinition createDefaultWeapon(
        WeaponKind kind
    ) {

        String json =
            "{\n"
                + "  \"class\": \""
                + kind.getEngineClassName()
                + "\",\n"
                + "  \"itemType\": \""
                + kind.getDefaultItemType()
                + "\",\n"
                + "  \"name\": \"New "
                + kind.getDisplayName()
                + "\",\n"
                + "  \"tex\": 0\n"
                + "}";

        return WeaponJsonAdapter.fromJson(
            json
        );
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

        editorHost =
            new JPanel(
                new BorderLayout()
            );

        editorHost.setBackground(
            ToolkitColors.BACKGROUND
        );

        add(
            editorHost,
            BorderLayout.CENTER
        );

        add(
            createFooter(),
            BorderLayout.SOUTH
        );
    }

    private JPanel createHeader() {

        JPanel header =
            new JPanel(
                new BorderLayout(
                    30,
                    0
                )
            );

        header.setOpaque(false);

        header.setBorder(
            new EmptyBorder(
                20,
                28,
                14,
                28
            )
        );

        JPanel titlePanel =
            new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
            new BoxLayout(
                titlePanel,
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
            title.getFont().deriveFont(
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

        titlePanel.add(title);

        titlePanel.add(
            Box.createVerticalStrut(
                5
            )
        );

        titlePanel.add(description);

        JPanel typePanel =
            new JPanel();

        typePanel.setOpaque(false);

        typePanel.setLayout(
            new BoxLayout(
                typePanel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel typeLabel =
            new JLabel(
                "WEAPON TYPE"
            );

        ToolkitStyles.styleSectionTitle(
            typeLabel
        );

        weaponTypeComboBox =
            new JComboBox<WeaponKind>(
                WeaponKind.values()
            );

        weaponTypeComboBox.setSelectedItem(
            selectedWeaponKind
        );

        weaponTypeComboBox.setPreferredSize(
            new Dimension(
                180,
                32
            )
        );

        weaponTypeComboBox.setMaximumSize(
            new Dimension(
                180,
                32
            )
        );

        weaponTypeComboBox.addActionListener(
            e -> weaponTypeChanged()
        );

        typePanel.add(typeLabel);

        typePanel.add(
            Box.createVerticalStrut(
                6
            )
        );

        typePanel.add(
            weaponTypeComboBox
        );

        header.add(
            titlePanel,
            BorderLayout.CENTER
        );

        header.add(
            typePanel,
            BorderLayout.EAST
        );

        return header;
    }

    private void weaponTypeChanged() {

        if (changingWeaponType) {
            return;
        }

        WeaponKind newKind =
            (WeaponKind) weaponTypeComboBox
                .getSelectedItem();

        if (
            newKind == null
                || newKind == selectedWeaponKind
        ) {

            return;
        }

        changeWeaponType(
            newKind
        );
    }

    private void changeWeaponType(
        WeaponKind newKind
    ) {

        changingWeaponType =
            true;

        try {

            /*
             * First save the currently visible controls into
             * the existing model.
             */
            valueBinder.saveWeapon(
                weapon,
                propertyControls
            );

            List<PropertyDefinition> oldProperties =
                properties;

            List<PropertyDefinition> newProperties =
                EnginePropertyScanner.scan(
                    newKind.getEngineClass()
                );

            /*
             * Remove known fields that belonged only to the
             * old subtype.
             *
             * Example:
             *
             * Wand -> Sword
             *
             * Wand-only spell / charge fields should not remain
             * inside the new Sword JSON.
             *
             * Common inherited Weapon/Item properties survive.
             */
            removeOldSubtypeProperties(
                oldProperties,
                newProperties
            );

            selectedWeaponKind =
                newKind;

            weapon.setClassName(
                newKind.getEngineClassName()
            );

            weapon.setItemType(
                newKind.getDefaultItemType()
            );

            properties =
                newProperties;

            valueBinder =
                new PropertyValueBinder(
                    properties
                );

            advancedVisible =
                false;

            advancedButton.setText(
                "Show Advanced Properties"
            );

            rebuildEditorArea();

        }
        finally {

            changingWeaponType =
                false;
        }
    }

    private void removeOldSubtypeProperties(
        List<PropertyDefinition> oldProperties,
        List<PropertyDefinition> newProperties
    ) {

        Set<String> newPropertyNames =
            new HashSet<String>();

        for (
            PropertyDefinition property
            : newProperties
        ) {

            newPropertyNames.add(
                property.getName()
            );
        }

        for (
            PropertyDefinition oldProperty
            : oldProperties
        ) {

            String propertyName =
                oldProperty.getName();

            if (
                newPropertyNames.contains(
                    propertyName
                )
            ) {

                continue;
            }

            /*
             * These core typed fields are controlled separately.
             */
            if (
                propertyName.equals("class")
                    || propertyName.equals("name")
                    || propertyName.equals("itemType")
                    || propertyName.equals("tex")
                    || propertyName.equals("texAtlas")
            ) {

                continue;
            }

            weapon.removeExtraProperty(
                propertyName
            );
        }
    }

    private void rebuildEditorArea() {

        propertyControls.clear();

        editorHost.removeAll();

        JSplitPane mainContent =
            createMainContent();

        editorHost.add(
            mainContent,
            BorderLayout.CENTER
        );

        valueBinder.loadWeapon(
            weapon,
            propertyControls
        );

        updateJsonPreview();

        editorHost.revalidate();
        editorHost.repaint();
    }

    private JSplitPane createMainContent() {

        JScrollPane editorScroll =
            createScrollableEditor();

        JPanel previewPanel =
            createJsonPreviewPanel();

        JSplitPane splitPane =
            new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                editorScroll,
                previewPanel
            );

        splitPane.setResizeWeight(
            0.72
        );

        splitPane.setDividerLocation(
            650
        );

        splitPane.setBorder(
            BorderFactory.createEmptyBorder()
        );

        return splitPane;
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
                18
            )
        );

        container.setLayout(
            new BoxLayout(
                container,
                BoxLayout.Y_AXIS
            )
        );

        JPanel normalPropertiesPanel =
            createNormalPropertiesPanel();

        advancedPropertiesPanel =
            createAdvancedPropertiesPanel();

        advancedPropertiesPanel.setVisible(
            advancedVisible
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

        scrollPane
            .getVerticalScrollBar()
            .setUnitIncrement(
                18
            );

        scrollPane.setHorizontalScrollBarPolicy(
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        );

        return scrollPane;
    }

    private JPanel createJsonPreviewPanel() {

        JPanel panel =
            new JPanel(
                new BorderLayout()
            );

        panel.setBackground(
            ToolkitColors.PANEL
        );

        panel.setBorder(
            new EmptyBorder(
                18,
                18,
                18,
                18
            )
        );

        JLabel title =
            new JLabel(
                "JSON PREVIEW"
            );

        ToolkitStyles.styleSectionTitle(
            title
        );

        jsonPreviewArea =
            new JTextArea();

        jsonPreviewArea.setEditable(
            false
        );

        jsonPreviewArea.setFont(
            new Font(
                Font.MONOSPACED,
                Font.PLAIN,
                12
            )
        );

        jsonPreviewArea.setBackground(
            ToolkitColors.BACKGROUND
        );

        jsonPreviewArea.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        JScrollPane scrollPane =
            new JScrollPane(
                jsonPreviewArea
            );

        scrollPane.setBorder(
            BorderFactory.createEmptyBorder()
        );

        panel.add(
            title,
            BorderLayout.NORTH
        );

        panel.add(
            scrollPane,
            BorderLayout.CENTER
        );

        return panel;
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

            container.add(section);

            container.add(
                Box.createVerticalStrut(
                    14
                )
            );
        }

        return container;
    }

    private JPanel createAdvancedPropertiesPanel() {

        List<PropertyDefinition> advanced =
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

                advanced.add(
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

        if (advanced.isEmpty()) {

            JLabel empty =
                new JLabel(
                    "No advanced properties."
                );

            ToolkitStyles.styleDescription(
                empty
            );

            container.add(empty);

            return container;
        }

        JPanel section =
            createPropertySection(
                "Advanced Properties",
                advanced,
                true
            );

        section.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        container.add(section);

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

            if (categoryProperties == null) {

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

        section.add(title);

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
                sectionProperties.get(i);

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

        saveButton.addActionListener(
            e -> saveWeapon()
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

    private void saveWeapon() {

        valueBinder.saveWeapon(
            weapon,
            propertyControls
        );

        updateJsonPreview();

        JOptionPane.showMessageDialog(
            this,
            selectedWeaponKind.getDisplayName()
                + " data updated successfully.",
            "Weapon Updated",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateJsonPreview() {

        jsonPreviewArea.setText(
            WeaponJsonAdapter.toJson(
                weapon
            )
        );

        jsonPreviewArea.setCaretPosition(
            0
        );
    }

    private void toggleAdvancedProperties() {

        advancedVisible =
            !advancedVisible;

        advancedPropertiesPanel.setVisible(
            advancedVisible
        );

        advancedButton.setText(
            advancedVisible
                ? "Hide Advanced Properties"
                : "Show Advanced Properties"
        );

        revalidate();
        repaint();
    }

    public WeaponDefinition getWeapon() {
        return weapon;
    }

    public WeaponKind getSelectedWeaponKind() {
        return selectedWeaponKind;
    }

    public String getWeaponJson() {

        return WeaponJsonAdapter.toJson(
            weapon
        );
    }
}
