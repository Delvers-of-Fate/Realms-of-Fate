package com.realmsoffate.toolkit;

import com.realmsoffate.toolkit.content.ContentType;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.project.ModProjectManager;
import com.realmsoffate.toolkit.project.ProjectCreationException;
import com.realmsoffate.toolkit.ui.HomePanel;
import com.realmsoffate.toolkit.ui.WorkspacePanel;
import com.realmsoffate.toolkit.ui.dialogs.CreateContentDialog;
import com.realmsoffate.toolkit.ui.dialogs.NewModDialog;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class ToolkitWindow extends JFrame {

    private HomePanel homePanel;
    private WorkspacePanel workspacePanel;

    private ModProject currentProject;

    public ToolkitWindow() {

        configureWindow();

        showHomeScreen();
    }

    private void configureWindow() {

        setTitle(
            "Realms of Fate Modding Toolkit"
        );

        setDefaultCloseOperation(
            JFrame.EXIT_ON_CLOSE
        );

        setSize(
            1200,
            760
        );

        setMinimumSize(
            new Dimension(
                950,
                650
            )
        );

        setLocationRelativeTo(null);
    }

    private void showHomeScreen() {

        currentProject = null;

        homePanel =
            new HomePanel();

        homePanel
            .getCreateModButton()
            .addActionListener(
                e -> showNewModDialog()
            );

        setContentPane(
            homePanel
        );

        setTitle(
            "Realms of Fate Modding Toolkit"
        );

        revalidate();
        repaint();
    }

    private void showNewModDialog() {

        NewModDialog dialog =
            new NewModDialog(
                this
            );

        dialog.setVisible(true);

        if (!dialog.isApproved()) {
            return;
        }

        try {

            File parentDirectory =
                new File(
                    dialog.getProjectFolder()
                );

            ModProject project =
                ModProjectManager.createProject(
                    dialog.getModName(),
                    dialog.getInternalId(),
                    dialog.getAuthor(),
                    dialog.getDescription(),
                    parentDirectory
                );

            showWorkspace(
                project
            );

        }
        catch (
            ProjectCreationException e
        ) {

            JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Could Not Create Mod",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void showWorkspace(
        ModProject project
    ) {

        currentProject =
            project;

        workspacePanel =
            new WorkspacePanel(
                project
            );

        workspacePanel
            .getCreateContentButton()
            .addActionListener(
                e -> showCreateContentDialog()
            );

        workspacePanel
            .getTestModButton()
            .addActionListener(
                e -> showTestModPlaceholder()
            );

        setContentPane(
            workspacePanel
        );

        setTitle(
            "Realms of Fate Modding Toolkit - "
                + project.getName()
        );

        revalidate();
        repaint();
    }

    private void showCreateContentDialog() {

        if (
            currentProject == null
        ) {

            return;
        }

        CreateContentDialog dialog =
            new CreateContentDialog(
                this
            );

        dialog.setVisible(true);

        if (!dialog.hasSelection()) {
            return;
        }

        ContentType selectedType =
            dialog.getSelectedType();

        handleContentTypeSelection(
            selectedType
        );
    }

    private void handleContentTypeSelection(
        ContentType type
    ) {

        if (
            type == ContentType.WEAPON
        ) {

            showWeaponPlaceholder();

            return;
        }

        JOptionPane.showMessageDialog(
            this,
            type.getDisplayName()
                + " editor will be added later.",
            "Coming Soon",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showWeaponPlaceholder() {

        JOptionPane.showMessageDialog(
            this,
            "Weapon editor is next.",
            "Create Weapon",
            JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void showTestModPlaceholder() {

        JOptionPane.showMessageDialog(
            this,
            "Test Mod will be connected to Delver later.",
            "Test Mod",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
