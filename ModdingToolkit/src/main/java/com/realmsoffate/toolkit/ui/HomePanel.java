package com.realmsoffate.toolkit.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class HomePanel extends JPanel {

    private JButton createModButton;
    private JButton openModButton;
    private JButton documentationButton;
    private JButton settingsButton;

    public HomePanel() {

        setLayout(
            new BorderLayout()
        );

        setBackground(
            ToolkitColors.BACKGROUND
        );

        setBorder(
            new EmptyBorder(
                32,
                55,
                22,
                55
            )
        );

        add(
            createHeader(),
            BorderLayout.NORTH
        );

        add(
            createMainContent(),
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

        header.setLayout(
            new BoxLayout(
                header,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "REALMS OF FATE"
            );

        title.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        ToolkitStyles.stylePageTitle(
            title
        );

        JLabel subtitle =
            new JLabel(
                "MODDING TOOLKIT"
            );

        subtitle.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        ToolkitStyles.styleSubtitle(
            subtitle
        );

        JLabel description =
            new JLabel(
                "Create, edit, and test your own adventures."
            );

        description.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        ToolkitStyles.styleDescription(
            description
        );

        header.add(title);

        header.add(
            Box.createVerticalStrut(
                3
            )
        );

        header.add(subtitle);

        header.add(
            Box.createVerticalStrut(
                12
            )
        );

        header.add(description);

        header.add(
            Box.createVerticalStrut(
                30
            )
        );

        return header;
    }

    private JPanel createMainContent() {

        JPanel wrapper =
            new JPanel(
                new GridBagLayout()
            );

        wrapper.setOpaque(false);

        JPanel content =
            new JPanel();

        content.setOpaque(false);

        content.setLayout(
            new BoxLayout(
                content,
                BoxLayout.Y_AXIS
            )
        );

        content.setPreferredSize(
            new Dimension(
                900,
                470
            )
        );

        content.add(
            createActionPanel()
        );

        content.add(
            Box.createVerticalStrut(
                24
            )
        );

        content.add(
            createLowerSection()
        );

        wrapper.add(content);

        return wrapper;
    }

    private JPanel createActionPanel() {

        JPanel actionPanel =
            new JPanel(
                new GridLayout(
                    1,
                    2,
                    18,
                    0
                )
            );

        actionPanel.setOpaque(false);

        actionPanel.setMaximumSize(
            new Dimension(
                900,
                115
            )
        );

        actionPanel.add(
            createNewModCard()
        );

        actionPanel.add(
            createOpenModCard()
        );

        return actionPanel;
    }

    private JPanel createNewModCard() {

        JPanel card =
            new JPanel(
                new BorderLayout(
                    0,
                    10
                )
            );

        ToolkitStyles.styleCard(
            card
        );

        JLabel title =
            new JLabel(
                "Create New Mod"
            );

        title.setFont(
            title.getFont().deriveFont(
                Font.BOLD,
                18f
            )
        );

        title.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        JLabel description =
            new JLabel(
                "Start a brand new mod project."
            );

        ToolkitStyles.styleSecondaryText(
            description
        );

        createModButton =
            new JButton(
                "Create New Mod"
            );

        ToolkitStyles.stylePrimaryButton(
            createModButton
        );

        JPanel textPanel =
            new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
            new BoxLayout(
                textPanel,
                BoxLayout.Y_AXIS
            )
        );

        textPanel.add(title);

        textPanel.add(
            Box.createVerticalStrut(
                4
            )
        );

        textPanel.add(description);

        card.add(
            textPanel,
            BorderLayout.CENTER
        );

        card.add(
            createModButton,
            BorderLayout.SOUTH
        );

        return card;
    }

    private JPanel createOpenModCard() {

        JPanel card =
            new JPanel(
                new BorderLayout(
                    0,
                    10
                )
            );

        ToolkitStyles.styleCard(
            card
        );

        JLabel title =
            new JLabel(
                "Open Existing Mod"
            );

        title.setFont(
            title.getFont().deriveFont(
                Font.BOLD,
                18f
            )
        );

        title.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        JLabel description =
            new JLabel(
                "Continue working on an existing project."
            );

        ToolkitStyles.styleSecondaryText(
            description
        );

        openModButton =
            new JButton(
                "Open Mod"
            );

        ToolkitStyles.styleSecondaryButton(
            openModButton
        );

        JPanel textPanel =
            new JPanel();

        textPanel.setOpaque(false);

        textPanel.setLayout(
            new BoxLayout(
                textPanel,
                BoxLayout.Y_AXIS
            )
        );

        textPanel.add(title);

        textPanel.add(
            Box.createVerticalStrut(
                4
            )
        );

        textPanel.add(description);

        card.add(
            textPanel,
            BorderLayout.CENTER
        );

        card.add(
            openModButton,
            BorderLayout.SOUTH
        );

        return card;
    }

    private JPanel createLowerSection() {

        JPanel panel =
            new JPanel(
                new GridLayout(
                    1,
                    2,
                    18,
                    0
                )
            );

        panel.setOpaque(false);

        panel.add(
            createRecentProjectsCard()
        );

        panel.add(
            createGettingStartedCard()
        );

        return panel;
    }

    private JPanel createRecentProjectsCard() {

        JPanel card =
            new JPanel(
                new BorderLayout(
                    0,
                    15
                )
            );

        ToolkitStyles.styleCard(
            card
        );

        JLabel title =
            new JLabel(
                "RECENT PROJECTS"
            );

        ToolkitStyles.styleSectionTitle(
            title
        );

        JPanel emptyPanel =
            new JPanel();

        emptyPanel.setOpaque(false);

        emptyPanel.setLayout(
            new BoxLayout(
                emptyPanel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel noProjects =
            new JLabel(
                "No recent projects yet."
            );

        noProjects.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        noProjects.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        JLabel explanation =
            new JLabel(
                "Projects you open will appear here."
            );

        explanation.setAlignmentX(
            Component.CENTER_ALIGNMENT
        );

        ToolkitStyles.styleSecondaryText(
            explanation
        );

        emptyPanel.add(
            Box.createVerticalGlue()
        );

        emptyPanel.add(
            noProjects
        );

        emptyPanel.add(
            Box.createVerticalStrut(
                6
            )
        );

        emptyPanel.add(
            explanation
        );

        emptyPanel.add(
            Box.createVerticalGlue()
        );

        card.add(
            title,
            BorderLayout.NORTH
        );

        card.add(
            emptyPanel,
            BorderLayout.CENTER
        );

        return card;
    }

    private JPanel createGettingStartedCard() {

        JPanel card =
            new JPanel();

        ToolkitStyles.styleCard(
            card
        );

        card.setLayout(
            new BoxLayout(
                card,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "GETTING STARTED"
            );

        ToolkitStyles.styleSectionTitle(
            title
        );

        card.add(title);

        card.add(
            Box.createVerticalStrut(
                18
            )
        );

        card.add(
            createHelpRow(
                "Create your first mod project"
            )
        );

        card.add(
            Box.createVerticalStrut(
                12
            )
        );

        card.add(
            createHelpRow(
                "Create your first item"
            )
        );

        card.add(
            Box.createVerticalStrut(
                12
            )
        );

        card.add(
            createHelpRow(
                "Learn how sprite sheets work"
            )
        );

        return card;
    }

    private JPanel createHelpRow(
        String text
    ) {

        JPanel row =
            new JPanel(
                new BorderLayout()
            );

        row.setOpaque(false);

        JLabel marker =
            new JLabel(
                "›"
            );

        marker.setForeground(
            ToolkitColors.ACCENT
        );

        marker.setFont(
            marker.getFont().deriveFont(
                Font.BOLD,
                18f
            )
        );

        JLabel label =
            new JLabel(
                text
            );

        label.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        row.add(
            marker,
            BorderLayout.WEST
        );

        row.add(
            label,
            BorderLayout.CENTER
        );

        return row;
    }

    private JPanel createFooter() {

        JPanel footer =
            new JPanel(
                new BorderLayout()
            );

        footer.setOpaque(false);

        footer.setBorder(
            new EmptyBorder(
                12,
                0,
                0,
                0
            )
        );

        JLabel status =
            new JLabel(
                "Ready"
            );

        status.setForeground(
            ToolkitColors.SUCCESS
        );

        JPanel buttons =
            new JPanel(
                new FlowLayout(
                    FlowLayout.RIGHT,
                    8,
                    0
                )
            );

        buttons.setOpaque(false);

        documentationButton =
            new JButton(
                "Documentation"
            );

        settingsButton =
            new JButton(
                "Settings"
            );

        ToolkitStyles.styleSecondaryButton(
            documentationButton
        );

        ToolkitStyles.styleSecondaryButton(
            settingsButton
        );

        buttons.add(
            documentationButton
        );

        buttons.add(
            settingsButton
        );

        footer.add(
            status,
            BorderLayout.WEST
        );

        footer.add(
            buttons,
            BorderLayout.EAST
        );

        return footer;
    }

    public JButton getCreateModButton() {
        return createModButton;
    }

    public JButton getOpenModButton() {
        return openModButton;
    }

    public JButton getDocumentationButton() {
        return documentationButton;
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }
}
