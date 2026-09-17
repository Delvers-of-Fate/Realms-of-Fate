package com.realmsoffate.toolkit.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

public class NewModDialog extends JDialog {

    private static File lastParentDirectory;

    private final JTextField nameField = new JTextField();
    private final JTextField locationField = new JTextField();

    private boolean approved;
    private File parentDirectory;

    public NewModDialog(JFrame owner) {
        super(owner, "Create New Mod", true);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(620, 260);
        setMinimumSize(new Dimension(560, 240));
        setLocationRelativeTo(owner);

        if (lastParentDirectory != null && lastParentDirectory.isDirectory()) {
            parentDirectory = lastParentDirectory;
            locationField.setText(parentDirectory.getAbsolutePath());
        }

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(
            BorderFactory.createEmptyBorder(22, 24, 20, 24)
        );

        JPanel form = new JPanel(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        form.add(new JLabel("Mod Name"), c);

        c.gridx = 1;
        c.weightx = 1;
        c.gridwidth = 2;
        form.add(nameField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        c.gridwidth = 1;
        form.add(new JLabel("Location"), c);

        c.gridx = 1;
        c.weightx = 1;

        locationField.setEditable(true);
        locationField.setToolTipText(
            "Parent folder where the new mod folder will be created."
        );

        form.add(locationField, c);

        JButton changeButton = new JButton("Change...");
        changeButton.addActionListener(e -> chooseLocation());

        c.gridx = 2;
        c.weightx = 0;
        form.add(changeButton, c);

        JPanel buttons = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );

        JButton cancel = new JButton("Cancel");
        JButton create = new JButton("Create Mod");

        cancel.addActionListener(e -> dispose());
        create.addActionListener(e -> approve());

        buttons.add(cancel);
        buttons.add(create);

        content.add(form, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        setContentPane(content);
        getRootPane().setDefaultButton(create);
    }

    private void chooseLocation() {
        File currentDirectory = resolveLocationField();

        ModLocationDialog dialog =
            new ModLocationDialog(this, currentDirectory);

        dialog.setVisible(true);

        if (!dialog.isApproved()) {
            return;
        }

        parentDirectory = dialog.getSelectedDirectory();

        if (parentDirectory != null) {
            locationField.setText(
                parentDirectory.getAbsolutePath()
            );
        }
    }

    private File resolveLocationField() {
        String text = locationField.getText();

        if (text != null && !text.trim().isEmpty()) {
            File typed = new File(text.trim());

            if (typed.isDirectory()) {
                return typed;
            }
        }

        if (parentDirectory != null &&
            parentDirectory.isDirectory()) {
            return parentDirectory;
        }

        if (lastParentDirectory != null &&
            lastParentDirectory.isDirectory()) {
            return lastParentDirectory;
        }

        return null;
    }

    private void approve() {
        String name =
            nameField.getText() == null
                ? ""
                : nameField.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Enter a mod name.",
                "Create Mod",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String location =
            locationField.getText() == null
                ? ""
                : locationField.getText().trim();

        if (location.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Choose where the mod should be created.",
                "Create Mod",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        File directory = new File(location);

        if (!directory.exists()) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "The location does not exist.\n\nCreate it?",
                "Create Location",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            if (!directory.mkdirs()) {
                JOptionPane.showMessageDialog(
                    this,
                    "The location could not be created.",
                    "Create Mod",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        }

        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(
                this,
                "The selected location is not a folder.",
                "Create Mod",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        if (!directory.canWrite()) {
            JOptionPane.showMessageDialog(
                this,
                "The selected location is not writable.",
                "Create Mod",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        parentDirectory = directory.getAbsoluteFile();
        lastParentDirectory = parentDirectory;

        approved = true;
        dispose();
    }

    public boolean isApproved() {
        return approved;
    }

    public String getProjectName() {
        return nameField.getText().trim();
    }

    public File getParentDirectory() {
        return parentDirectory;
    }
}
