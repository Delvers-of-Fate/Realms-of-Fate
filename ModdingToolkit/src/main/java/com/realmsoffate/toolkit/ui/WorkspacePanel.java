package com.realmsoffate.toolkit.ui;

import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.editors.WeaponEditorPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class WorkspacePanel extends JPanel {

    private final ModProject project;

    private JButton testModButton;
    private JButton createContentButton;

    private JTextField searchField;

    private JPanel workspaceContainer;

    public WorkspacePanel(
        ModProject project
    ) {

        this.project =
            project;

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
            createMainArea(),
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
                new BorderLayout()
            );

        header.setBackground(
            ToolkitColors.PANEL
        );

        header.setBorder(
            new EmptyBorder(
                12,
                18,
                12,
                18
            )
        );

        JLabel toolkitTitle =
            new JLabel(
                "Realms of Fate Modding Toolkit"
            );

        toolkitTitle.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        toolkitTitle.setFont(
            toolkitTitle.getFont().deriveFont(
                Font.BOLD,
                15f
            )
        );

        JLabel projectName =
            new JLabel(
                project.getName(),
                SwingConstants.CENTER
            );

        projectName.setForeground(
            ToolkitColors.ACCENT
        );

        projectName.setFont(
            projectName.getFont().deriveFont(
                Font.BOLD,
                16f
            )
        );

        testModButton =
            new JButton(
                "Test Mod"
            );

        ToolkitStyles.stylePrimaryButton(
            testModButton
        );

        header.add(
            toolkitTitle,
            BorderLayout.WEST
        );

        header.add(
            projectName,
            BorderLayout.CENTER
        );

        header.add(
            testModButton,
            BorderLayout.EAST
        );

        return header;
    }

    private JPanel createMainArea() {

        JPanel main =
            new JPanel(
                new BorderLayout()
            );

        main.setBackground(
            ToolkitColors.BACKGROUND
        );

        main.add(
            createNavigationPanel(),
            BorderLayout.WEST
        );

        workspaceContainer =
            new JPanel(
                new BorderLayout()
            );

        workspaceContainer.setBackground(
            ToolkitColors.BACKGROUND
        );

        main.add(
            workspaceContainer,
            BorderLayout.CENTER
        );

        showProjectHome();

        return main;
    }

    private JPanel createNavigationPanel() {

        JPanel panel =
            new JPanel();

        panel.setBackground(
            ToolkitColors.PANEL
        );

        panel.setPreferredSize(
            new Dimension(
                220,
                100
            )
        );

        panel.setBorder(
            new EmptyBorder(
                18,
                16,
                18,
                16
            )
        );

        panel.setLayout(
            new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel contentTitle =
            new JLabel(
                "CONTENT"
            );

        ToolkitStyles.styleSectionTitle(
            contentTitle
        );

        contentTitle.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        searchField =
            new JTextField();

        searchField.setToolTipText(
            "Search project content"
        );

        searchField.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                32
            )
        );

        searchField.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        createContentButton =
            new JButton(
                "+ Create"
            );

        ToolkitStyles.stylePrimaryButton(
            createContentButton
        );

        createContentButton.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        panel.add(contentTitle);

        panel.add(
            Box.createVerticalStrut(12)
        );

        panel.add(searchField);

        panel.add(
            Box.createVerticalStrut(14)
        );

        panel.add(createContentButton);

        panel.add(
            Box.createVerticalStrut(20)
        );

        panel.add(
            createNavButton("Items")
        );

        panel.add(
            Box.createVerticalStrut(6)
        );

        panel.add(
            createNavButton("Magic")
        );

        panel.add(
            Box.createVerticalStrut(6)
        );

        panel.add(
            createNavButton("Creatures")
        );

        panel.add(
            Box.createVerticalStrut(28)
        );

        JLabel assetsTitle =
            new JLabel(
                "ASSETS"
            );

        ToolkitStyles.styleSectionTitle(
            assetsTitle
        );

        assetsTitle.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        panel.add(assetsTitle);

        panel.add(
            Box.createVerticalStrut(10)
        );

        panel.add(
            createNavButton(
                "Sprite Sheets"
            )
        );

        panel.add(
            Box.createVerticalStrut(6)
        );

        panel.add(
            createNavButton(
                "Sounds"
            )
        );

        panel.add(
            Box.createVerticalGlue()
        );

        return panel;
    }

    private JButton createNavButton(
        String text
    ) {

        JButton button =
            new JButton(
                text
            );

        ToolkitStyles.styleSecondaryButton(
            button
        );

        button.setHorizontalAlignment(
            SwingConstants.LEFT
        );

        button.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                34
            )
        );

        button.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        return button;
    }

    public void showProjectHome() {

        workspaceContainer.removeAll();

        JPanel wrapper =
            new JPanel(
                new GridBagLayout()
            );

        wrapper.setBackground(
            ToolkitColors.BACKGROUND
        );

        JPanel content =
            new JPanel();

        content.setOpaque(false);

        content.setLayout(
            new BoxLayout(
                content,
                BoxLayout.Y_AXIS
            )
        );

        JLabel projectTitle =
            new JLabel(
                project.getName()
            );

        projectTitle.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        projectTitle.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        projectTitle.setFont(
            projectTitle.getFont().deriveFont(
                Font.BOLD,
                28f
            )
        );

        JLabel emptyMessage =
            new JLabel(
                "No content yet."
            );

        emptyMessage.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        ToolkitStyles.styleDescription(
            emptyMessage
        );

        JButton createButton =
            new JButton(
                "Create Content"
            );

        ToolkitStyles.stylePrimaryButton(
            createButton
        );

        createButton.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        createButton.addActionListener(
            e -> createContentButton.doClick()
        );

        content.add(projectTitle);

        content.add(
            Box.createVerticalStrut(10)
        );

        content.add(emptyMessage);

        content.add(
            Box.createVerticalStrut(22)
        );

        content.add(createButton);

        wrapper.add(content);

        workspaceContainer.add(
            wrapper,
            BorderLayout.CENTER
        );

        refreshWorkspace();
    }

    public void showWeaponEditor() {

        workspaceContainer.removeAll();

        WeaponEditorPanel weaponEditor =
            new WeaponEditorPanel();

        workspaceContainer.add(
            weaponEditor,
            BorderLayout.CENTER
        );

        refreshWorkspace();
    }

    private void refreshWorkspace() {

        workspaceContainer.revalidate();
        workspaceContainer.repaint();
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
                8,
                16,
                8,
                16
            )
        );

        JLabel status =
            new JLabel(
                "Ready"
            );

        status.setForeground(
            ToolkitColors.SUCCESS
        );

        JLabel version =
            new JLabel(
                "Toolkit v0.1.0"
            );

        version.setForeground(
            ToolkitColors.TEXT_SECONDARY
        );

        footer.add(
            status,
            BorderLayout.WEST
        );

        footer.add(
            version,
            BorderLayout.EAST
        );

        return footer;
    }

    public JButton getTestModButton() {
        return testModButton;
    }

    public JButton getCreateContentButton() {
        return createContentButton;
    }

    public JTextField getSearchField() {
        return searchField;
    }
}
