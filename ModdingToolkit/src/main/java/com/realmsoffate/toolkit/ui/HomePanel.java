package com.realmsoffate.toolkit.ui;

import com.realmsoffate.toolkit.project.RecentProjects;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;

public class HomePanel extends JPanel {
    public interface Listener {
        void onCreateMod();
        void onOpenMod();
        void onOpenRecent(File directory);
    }

    public HomePanel(final Listener listener) {
        setLayout(new BorderLayout());
        setBackground(ToolkitColors.BACKGROUND);

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(55, 90, 55, 90));

        JLabel title = new JLabel("REALMS OF FATE", SwingConstants.CENTER);
        title.setForeground(ToolkitColors.TEXT_PRIMARY); title.setFont(title.getFont().deriveFont(Font.BOLD, 34f)); title.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel subtitle = new JLabel("Modding Toolkit", SwingConstants.CENTER);
        subtitle.setForeground(ToolkitColors.ACCENT); subtitle.setFont(subtitle.getFont().deriveFont(Font.BOLD, 19f)); subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel description = new JLabel("Create and edit Delver mods without manually editing .dat JSON files.");
        description.setForeground(ToolkitColors.TEXT_SECONDARY); description.setFont(description.getFont().deriveFont(14f)); description.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel actions = new JPanel(new GridLayout(1, 2, 10, 0)); actions.setOpaque(false); actions.setMaximumSize(new Dimension(500, 44));
        JButton create = new JButton("Create New Mod"); JButton open = new JButton("Open Existing Mod");
        create.addActionListener(e -> listener.onCreateMod()); open.addActionListener(e -> listener.onOpenMod());
        actions.add(create); actions.add(open);

        center.add(title); center.add(Box.createVerticalStrut(4)); center.add(subtitle); center.add(Box.createVerticalStrut(20)); center.add(description); center.add(Box.createVerticalStrut(30)); center.add(actions);

        List<File> recent = RecentProjects.load();
        if (!recent.isEmpty()) {
            center.add(Box.createVerticalStrut(34));
            JLabel recentTitle = new JLabel("RECENT MODS"); recentTitle.setForeground(ToolkitColors.TEXT_SECONDARY); recentTitle.setFont(recentTitle.getFont().deriveFont(Font.BOLD, 12f)); recentTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            center.add(recentTitle); center.add(Box.createVerticalStrut(8));
            JPanel recentPanel = new JPanel(); recentPanel.setOpaque(false); recentPanel.setLayout(new BoxLayout(recentPanel, BoxLayout.Y_AXIS)); recentPanel.setMaximumSize(new Dimension(620, 250)); recentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
            for (final File file : recent) {
                boolean exists = file.isDirectory();
                JButton row = new JButton((exists ? "" : "[Missing]  ") + file.getName() + "   —   " + file.getAbsolutePath());
                row.setHorizontalAlignment(SwingConstants.LEFT); row.setEnabled(exists); row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
                row.addActionListener(e -> listener.onOpenRecent(file)); recentPanel.add(row); recentPanel.add(Box.createVerticalStrut(5));
            }
            center.add(recentPanel);
        }
        add(center, BorderLayout.CENTER);
    }
}
