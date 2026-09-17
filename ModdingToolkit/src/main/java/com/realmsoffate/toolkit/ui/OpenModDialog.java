package com.realmsoffate.toolkit.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class OpenModDialog extends JDialog {
    private final JTextField pathField = new JTextField();
    private boolean approved;
    private File selectedDirectory;

    public OpenModDialog(Window owner) {
        super(owner, "Open Existing Mod", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(650, 285);
        setMinimumSize(new Dimension(560, 270));
        setLocationRelativeTo(owner);

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(BorderFactory.createEmptyBorder(22, 24, 20, 24));

        JPanel header = new JPanel(new BorderLayout(0, 5));
        JLabel title = new JLabel("Open an existing Delver / Realms of Fate mod");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 18f));
        header.add(title, BorderLayout.NORTH);
        header.add(new JLabel("Enter the mod's root folder. Existing data and assets will not be overwritten."), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.add(new JLabel("Mod Folder"), BorderLayout.NORTH);
        center.add(pathField, BorderLayout.CENTER);
        JPanel quick = new JPanel(new GridLayout(1, 3, 8, 0));
        JButton desktop = new JButton("Desktop");
        JButton documents = new JButton("Documents");
        JButton home = new JButton("Home");
        desktop.addActionListener(e -> setPath(new File(System.getProperty("user.home", "."), "Desktop")));
        documents.addActionListener(e -> setPath(new File(System.getProperty("user.home", "."), "Documents")));
        home.addActionListener(e -> setPath(new File(System.getProperty("user.home", "."))));
        quick.add(desktop); quick.add(documents); quick.add(home);
        center.add(quick, BorderLayout.SOUTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton cancel = new JButton("Cancel");
        JButton open = new JButton("Open Mod");
        cancel.addActionListener(e -> dispose());
        open.addActionListener(e -> approve());
        buttons.add(cancel); buttons.add(open);
        getRootPane().setDefaultButton(open);

        content.add(header, BorderLayout.NORTH); content.add(center, BorderLayout.CENTER); content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void setPath(File file) { if (file != null) pathField.setText(file.getAbsolutePath()); }

    private void approve() {
        String text = pathField.getText() == null ? "" : pathField.getText().trim();
        if (text.isEmpty()) { JOptionPane.showMessageDialog(this, "Enter the mod folder path.", "Open Mod", JOptionPane.WARNING_MESSAGE); return; }
        File dir = new File(text).getAbsoluteFile();
        if (!dir.isDirectory()) { JOptionPane.showMessageDialog(this, "That path is not an existing folder.", "Open Mod", JOptionPane.WARNING_MESSAGE); return; }
        selectedDirectory = dir; approved = true; dispose();
    }

    public boolean isApproved() { return approved; }
    public File getSelectedDirectory() { return selectedDirectory; }
}
