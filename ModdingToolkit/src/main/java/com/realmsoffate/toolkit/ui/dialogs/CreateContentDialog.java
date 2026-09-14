package com.realmsoffate.toolkit.ui.dialogs;

import com.realmsoffate.toolkit.content.ContentType;
import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.ToolkitStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class CreateContentDialog extends JDialog {

    private ContentType selectedType;

    private JLabel selectionTitle;
    private JLabel selectionDescription;

    private JButton continueButton;
    private JButton cancelButton;

    public CreateContentDialog(Window owner) {

        super(
            owner,
            "Create Content",
            ModalityType.APPLICATION_MODAL
        );

        buildDialog();
    }

    private void buildDialog() {

        setDefaultCloseOperation(
            DISPOSE_ON_CLOSE
        );

        setSize(
            820,
            580
        );

        setMinimumSize(
            new Dimension(
                760,
                540
            )
        );

        setLocationRelativeTo(
            getOwner()
        );

        JPanel root =
            new JPanel(
                new BorderLayout()
            );

        root.setBackground(
            ToolkitColors.BACKGROUND
        );

        root.setBorder(
            new EmptyBorder(
                24,
                28,
                22,
                28
            )
        );

        root.add(
            createHeader(),
            BorderLayout.NORTH
        );

        root.add(
            createContentArea(),
            BorderLayout.CENTER
        );

        root.add(
            createFooter(),
            BorderLayout.SOUTH
        );

        setContentPane(root);
    }

    private JPanel createHeader() {

        JPanel panel =
            new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
            new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "CREATE CONTENT"
            );

        title.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        title.setFont(
            title.getFont().deriveFont(
                Font.BOLD,
                22f
            )
        );

        JLabel description =
            new JLabel(
                "Choose the type of game content you want to create."
            );

        ToolkitStyles.styleDescription(
            description
        );

        panel.add(title);

        panel.add(
            Box.createVerticalStrut(
                6
            )
        );

        panel.add(description);

        panel.add(
            Box.createVerticalStrut(
                22
            )
        );

        return panel;
    }

    private JPanel createContentArea() {

        JPanel container =
            new JPanel(
                new BorderLayout()
            );

        container.setOpaque(false);

        container.add(
            createCategoryGrid(),
            BorderLayout.CENTER
        );

        container.add(
            createSelectionPanel(),
            BorderLayout.SOUTH
        );

        return container;
    }

    private JPanel createCategoryGrid() {

        JPanel grid =
            new JPanel(
                new GridLayout(
                    1,
                    3,
                    16,
                    0
                )
            );

        grid.setOpaque(false);

        grid.add(
            createCategoryPanel(
                "ITEMS",
                new ContentType[] {
                    ContentType.WEAPON,
                    ContentType.ARMOR,
                    ContentType.CONSUMABLE,
                    ContentType.MISC_ITEM
                }
            )
        );

        grid.add(
            createCategoryPanel(
                "MAGIC",
                new ContentType[] {
                    ContentType.SPELL,
                    ContentType.SPELL_EFFECT,
                    ContentType.STATUS_EFFECT
                }
            )
        );

        grid.add(
            createCategoryPanel(
                "CREATURES",
                new ContentType[] {
                    ContentType.MONSTER,
                    ContentType.NPC
                }
            )
        );

        return grid;
    }

    private JPanel createCategoryPanel(
        String categoryName,
        ContentType[] types
    ) {

        JPanel panel =
            new JPanel();

        ToolkitStyles.styleCard(
            panel
        );

        panel.setLayout(
            new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                categoryName
            );

        ToolkitStyles.styleSectionTitle(
            title
        );

        title.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        panel.add(title);

        panel.add(
            Box.createVerticalStrut(
                14
            )
        );

        for (ContentType type : types) {

            JButton button =
                createContentTypeButton(
                    type
                );

            panel.add(button);

            panel.add(
                Box.createVerticalStrut(
                    8
                )
            );
        }

        panel.add(
            Box.createVerticalGlue()
        );

        return panel;
    }

    private JButton createContentTypeButton(
        ContentType type
    ) {

        JButton button =
            new JButton(
                type.getDisplayName()
            );

        ToolkitStyles.styleSecondaryButton(
            button
        );

        button.setHorizontalAlignment(
            SwingConstants.LEFT
        );

        button.setAlignmentX(
            Component.LEFT_ALIGNMENT
        );

        button.setMaximumSize(
            new Dimension(
                Integer.MAX_VALUE,
                38
            )
        );

        button.addActionListener(
            e -> selectContentType(
                type
            )
        );

        return button;
    }

    private JPanel createSelectionPanel() {

        JPanel panel =
            new JPanel(
                new BorderLayout()
            );

        panel.setOpaque(false);

        panel.setBorder(
            new EmptyBorder(
                20,
                0,
                0,
                0
            )
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

        selectionTitle =
            new JLabel(
                "Nothing selected"
            );

        selectionTitle.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        selectionTitle.setFont(
            selectionTitle
                .getFont()
                .deriveFont(
                    Font.BOLD,
                    14f
                )
        );

        selectionDescription =
            new JLabel(
                "Choose a content type above."
            );

        ToolkitStyles.styleDescription(
            selectionDescription
        );

        textPanel.add(
            selectionTitle
        );

        textPanel.add(
            Box.createVerticalStrut(
                4
            )
        );

        textPanel.add(
            selectionDescription
        );

        panel.add(
            textPanel,
            BorderLayout.CENTER
        );

        return panel;
    }

    private JPanel createFooter() {

        JPanel footer =
            new JPanel(
                new FlowLayout(
                    FlowLayout.RIGHT,
                    8,
                    0
                )
            );

        footer.setOpaque(false);

        footer.setBorder(
            new EmptyBorder(
                18,
                0,
                0,
                0
            )
        );

        cancelButton =
            new JButton(
                "Cancel"
            );

        continueButton =
            new JButton(
                "Continue"
            );

        ToolkitStyles.styleSecondaryButton(
            cancelButton
        );

        ToolkitStyles.stylePrimaryButton(
            continueButton
        );

        continueButton.setEnabled(
            false
        );

        cancelButton.addActionListener(
            e -> dispose()
        );

        continueButton.addActionListener(
            e -> continueWithSelection()
        );

        footer.add(cancelButton);
        footer.add(continueButton);

        return footer;
    }

    private void selectContentType(
        ContentType type
    ) {

        selectedType =
            type;

        selectionTitle.setText(
            type.getDisplayName()
        );

        selectionDescription.setText(
            "<html>"
                + type.getDescription()
                + "</html>"
        );

        continueButton.setEnabled(
            true
        );
    }

    private void continueWithSelection() {

        if (selectedType == null) {
            return;
        }

        dispose();
    }

    public ContentType getSelectedType() {
        return selectedType;
    }

    public boolean hasSelection() {
        return selectedType != null;
    }
}
