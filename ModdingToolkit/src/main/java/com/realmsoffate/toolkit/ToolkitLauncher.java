package com.realmsoffate.toolkit;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class ToolkitLauncher {

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        }
        catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf.");
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            ToolkitWindow window = new ToolkitWindow();
            window.setVisible(true);
        });
    }
}
