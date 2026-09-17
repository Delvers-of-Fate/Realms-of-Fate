package com.realmsoffate.toolkit;

import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.project.ModProjectManager;
import com.realmsoffate.toolkit.project.RecentProjects;
import com.realmsoffate.toolkit.ui.HomePanel;
import com.realmsoffate.toolkit.ui.NewModDialog;
import com.realmsoffate.toolkit.ui.OpenModDialog;
import com.realmsoffate.toolkit.ui.WorkspacePanel;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.Dimension;
import java.io.File;

public class ToolkitWindow extends JFrame {
    private final ModProjectManager projectManager = new ModProjectManager();
    private ModProject currentProject;

    public ToolkitWindow() {
        super("Realms of Fate Modding Toolkit");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        showHome();
    }

    public void showHome() {
        currentProject = null;
        setTitle("Realms of Fate Modding Toolkit");
        setContentPane(new HomePanel(new HomePanel.Listener() {
            @Override
            public void onCreateMod() { createMod(); }
            @Override
            public void onOpenMod() { openMod(); }
            @Override
            public void onOpenRecent(File directory) { openMod(directory); }
        }));
        revalidate();
        repaint();
    }

    private void createMod() {
        NewModDialog dialog = new NewModDialog(this);
        dialog.setVisible(true);
        if (!dialog.isApproved()) return;

        try {
            currentProject = projectManager.createProject(
                    dialog.getProjectName(),
                    dialog.getParentDirectory()
            );
            RecentProjects.remember(currentProject.getRootDirectory());
            showWorkspace(currentProject);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Could Not Create Mod",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void openMod() {
        OpenModDialog dialog = new OpenModDialog(this);
        dialog.setVisible(true);
        if (!dialog.isApproved()) return;
        openMod(dialog.getSelectedDirectory());
    }

    private void openMod(File directory) {
        try {
            currentProject = projectManager.openProject(directory);
            RecentProjects.remember(currentProject.getRootDirectory());
            showWorkspace(currentProject);
        }
        catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could Not Open Mod", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWorkspace(ModProject project) {
        setTitle(project.getName() + " - Realms of Fate Modding Toolkit");
        setContentPane(new WorkspacePanel(project, new WorkspacePanel.Listener() {
            @Override
            public void onBackHome() {
                showHome();
            }
        }));
        revalidate();
        repaint();
    }
}
