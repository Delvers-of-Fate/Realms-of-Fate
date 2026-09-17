package com.realmsoffate.toolkit;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public final class ToolkitLauncher {
    private ToolkitLauncher() {}

    public static void main(String[] args) {
        FlatDarkLaf.setup();
        UIManager.put("Component.arc", 8);
        UIManager.put("Button.arc", 8);
        UIManager.put("TextComponent.arc", 6);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                ToolkitWindow window = new ToolkitWindow();
                window.setVisible(true);
            }
        });
    }
}
