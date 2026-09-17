package com.realmsoffate.toolkit.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Window;
import java.io.File;

public class ModLocationDialog extends JDialog {

    private static File lastLocation;

    private final JTextField pathField = new JTextField();

    private boolean approved = false;
    private File selectedDirectory;

    public ModLocationDialog(Window owner, File currentDirectory) {
        super(owner, "Choose Mod Location", ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 300);
        setMinimumSize(new Dimension(540, 280));
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));

        content.add(createHeader(), BorderLayout.NORTH);
        content.add(createCenter(currentDirectory), BorderLayout.CENTER);
        content.add(createButtons(), BorderLayout.SOUTH);

        setContentPane(content);
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new BorderLayout(0, 5));

        JLabel title = new JLabel("Where should the mod be created?");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));

        JLabel description = new JLabel(
            "Choose the parent folder. The toolkit will create the mod folder inside it."
        );

        panel.add(title, BorderLayout.NORTH);
        panel.add(description, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenter(File currentDirectory) {
        JPanel panel = new JPanel(new BorderLayout(0, 14));

        JPanel pathPanel = new JPanel(new BorderLayout(8, 5));

        JLabel pathLabel = new JLabel("Location");
        pathPanel.add(pathLabel, BorderLayout.NORTH);
        pathPanel.add(pathField, BorderLayout.CENTER);

        File startingDirectory = chooseStartingDirectory(currentDirectory);

        if (startingDirectory != null) {
            pathField.setText(startingDirectory.getAbsolutePath());
        }

        JPanel shortcuts = new JPanel(new GridLayout(1, 3, 8, 0));

        JButton desktopButton = new JButton("Desktop");
        JButton documentsButton = new JButton("Documents");
        JButton homeButton = new JButton("Home");

        desktopButton.addActionListener(e ->
            setLocation(getDesktopDirectory())
        );

        documentsButton.addActionListener(e ->
            setLocation(getDocumentsDirectory())
        );

        homeButton.addActionListener(e ->
            setLocation(getHomeDirectory())
        );

        shortcuts.add(desktopButton);
        shortcuts.add(documentsButton);
        shortcuts.add(homeButton);

        JPanel shortcutPanel = new JPanel(new BorderLayout(0, 6));
        shortcutPanel.add(new JLabel("Quick Locations"), BorderLayout.NORTH);
        shortcutPanel.add(shortcuts, BorderLayout.CENTER);

        panel.add(pathPanel, BorderLayout.NORTH);
        panel.add(shortcutPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createButtons() {
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        JButton cancel = new JButton("Cancel");
        JButton select = new JButton("Select Folder");

        cancel.addActionListener(e -> dispose());
        select.addActionListener(e -> approve());

        buttons.add(cancel);
        buttons.add(select);

        getRootPane().setDefaultButton(select);

        return buttons;
    }

    private File chooseStartingDirectory(File currentDirectory) {
        if (currentDirectory != null && currentDirectory.isDirectory()) {
            return currentDirectory;
        }

        if (lastLocation != null && lastLocation.isDirectory()) {
            return lastLocation;
        }

        File documents = getDocumentsDirectory();
        if (documents != null && documents.isDirectory()) {
            return documents;
        }

        return getHomeDirectory();
    }

    private void setLocation(File directory) {
        if (directory == null) {
            return;
        }

        pathField.setText(directory.getAbsolutePath());
    }

    private void approve() {
        String path = pathField.getText();

        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Enter or choose a folder.",
                "Choose Mod Location",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File directory = new File(path.trim());

        if (!directory.exists()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "This folder does not exist.\n\nCreate it?",
                "Create Folder",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            if (!directory.mkdirs()) {
                JOptionPane.showMessageDialog(
                    this,
                    "The folder could not be created.",
                    "Choose Mod Location",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(
                this,
                "The selected path is not a folder.",
                "Choose Mod Location",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!directory.canWrite()) {
            JOptionPane.showMessageDialog(
                this,
                "The selected folder is not writable.",
                "Choose Mod Location",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        selectedDirectory = directory.getAbsoluteFile();
        lastLocation = selectedDirectory;
        approved = true;

        dispose();
    }

    private File getHomeDirectory() {
        String home = System.getProperty("user.home");

        if (home == null || home.trim().isEmpty()) {
            return null;
        }

        return new File(home);
    }

    private File getDesktopDirectory() {
        File home = getHomeDirectory();

        if (home == null) {
            return null;
        }

        File desktop = new File(home, "Desktop");

        if (desktop.isDirectory()) {
            return desktop;
        }

        return home;
    }

    private File getDocumentsDirectory() {
        File home = getHomeDirectory();

        if (home == null) {
            return null;
        }

        File documents = new File(home, "Documents");

        if (documents.isDirectory()) {
            return documents;
        }

        return home;
    }

    public boolean isApproved() {
        return approved;
    }

    public File getSelectedDirectory() {
        return selectedDirectory;
    }
}
