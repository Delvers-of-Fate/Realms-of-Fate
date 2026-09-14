package com.realmsoffate.toolkit.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;

public final class ToolkitStyles {

    private ToolkitStyles() {
    }

    public static void stylePageTitle(JLabel label) {

        label.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.BOLD,
                34f
            )
        );
    }

    public static void styleSubtitle(JLabel label) {

        label.setForeground(
            ToolkitColors.ACCENT
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.BOLD,
                18f
            )
        );
    }

    public static void styleDescription(JLabel label) {

        label.setForeground(
            ToolkitColors.TEXT_SECONDARY
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.PLAIN,
                14f
            )
        );
    }

    public static void styleSectionTitle(JLabel label) {

        label.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.BOLD,
                13f
            )
        );
    }

    public static void styleSecondaryText(JLabel label) {

        label.setForeground(
            ToolkitColors.TEXT_SECONDARY
        );
    }

    public static void stylePrimaryButton(
        JButton button
    ) {

        button.setBackground(
            ToolkitColors.ACCENT
        );

        button.setForeground(
            new Color(
                30,
                30,
                30
            )
        );

        button.setFont(
            button.getFont().deriveFont(
                Font.BOLD,
                14f
            )
        );

        button.setFocusPainted(false);

        button.setCursor(
            Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
            )
        );

        button.setBorder(
            BorderFactory.createEmptyBorder(
                12,
                22,
                12,
                22
            )
        );
    }

    public static void styleSecondaryButton(
        JButton button
    ) {

        button.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        button.setFont(
            button.getFont().deriveFont(
                Font.BOLD,
                13f
            )
        );

        button.setFocusPainted(false);

        button.setCursor(
            Cursor.getPredefinedCursor(
                Cursor.HAND_CURSOR
            )
        );
    }

    public static void styleCard(
        JPanel panel
    ) {

        panel.setBackground(
            ToolkitColors.PANEL
        );

        panel.setBorder(
            createCardBorder()
        );
    }

    public static Border createCardBorder() {

        Border outer =
            BorderFactory.createLineBorder(
                ToolkitColors.BORDER
            );

        Border inner =
            BorderFactory.createEmptyBorder(
                18,
                20,
                18,
                20
            );

        return BorderFactory.createCompoundBorder(
            outer,
            inner
        );
    }

    public static void makeTransparent(
        JComponent component
    ) {

        component.setOpaque(false);
    }
}
