package com.realmsoffate.toolkit.ui.dialogs;

import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.ToolkitStyles;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.File;

public class NewModDialog extends JDialog {

    private JTextField modNameField;
    private JTextField internalIdField;
    private JTextField authorField;
    private JTextArea descriptionArea;
    private JTextField projectFolderField;

    private JButton browseButton;
    private JButton cancelButton;
    private JButton createButton;

    private JLabel errorLabel;

    private boolean internalIdManuallyEdited = false;
    private boolean suppressInternalIdChange = false;

    private boolean approved = false;

    public NewModDialog(Window owner) {
        super(owner, "Create New Mod", ModalityType.APPLICATION_MODAL);

        buildDialog();
    }

    private void buildDialog() {

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setSize(650, 610);

        setMinimumSize(
            new Dimension(
                600,
                560
            )
        );

        setLocationRelativeTo(getOwner());

        JPanel root = new JPanel(
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
            createForm(),
            BorderLayout.CENTER
        );

        root.add(
            createFooter(),
            BorderLayout.SOUTH
        );

        setContentPane(root);

        wireEvents();
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel();

        panel.setOpaque(false);

        panel.setLayout(
            new BoxLayout(
                panel,
                BoxLayout.Y_AXIS
            )
        );

        JLabel title =
            new JLabel(
                "CREATE NEW MOD"
            );

        title.setAlignmentX(
            Component.LEFT_ALIGNMENT
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
                "Set up the basic information for your mod project."
            );

        description.setAlignmentX(
            Component.LEFT_ALIGNMENT
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

    private JPanel createForm() {

        JPanel panel =
            new JPanel(
                new GridBagLayout()
            );

        panel.setOpaque(false);

        GridBagConstraints gbc =
            new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;

        gbc.weightx = 1.0;

        gbc.fill =
            GridBagConstraints.HORIZONTAL;

        gbc.anchor =
            GridBagConstraints.NORTHWEST;

        gbc.insets =
            new Insets(
                0,
                0,
                7,
                0
            );

        // Mod name
        panel.add(
            createFieldLabel(
                "Mod Name"
            ),
            gbc
        );

        gbc.gridy++;

        modNameField =
            new JTextField();

        modNameField.setToolTipText(
            "The display name of your mod."
        );

        panel.add(
            modNameField,
            gbc
        );

        gbc.gridy++;

        panel.add(
            createHintLabel(
                "Example: The Forgotten Crypt"
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                18,
                0,
                7,
                0
            );

        // Internal ID
        panel.add(
            createFieldLabel(
                "Internal ID"
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                0,
                0,
                7,
                0
            );

        internalIdField =
            new JTextField();

        internalIdField.setToolTipText(
            "Used internally for files and mod identification."
        );

        panel.add(
            internalIdField,
            gbc
        );

        gbc.gridy++;

        panel.add(
            createHintLabel(
                "Lowercase letters, numbers, and underscores only."
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                18,
                0,
                7,
                0
            );

        // Author
        panel.add(
            createFieldLabel(
                "Author"
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                0,
                0,
                7,
                0
            );

        authorField =
            new JTextField();

        panel.add(
            authorField,
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                18,
                0,
                7,
                0
            );

        // Description
        panel.add(
            createFieldLabel(
                "Description"
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                0,
                0,
                7,
                0
            );

        descriptionArea =
            new JTextArea(
                4,
                20
            );

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        JScrollPane descriptionScroll =
            new JScrollPane(
                descriptionArea
            );

        descriptionScroll.setPreferredSize(
            new Dimension(
                100,
                95
            )
        );

        panel.add(
            descriptionScroll,
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                18,
                0,
                7,
                0
            );

        // Project folder
        panel.add(
            createFieldLabel(
                "Project Location"
            ),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                0,
                0,
                0,
                0
            );

        panel.add(
            createFolderSelector(),
            gbc
        );

        gbc.gridy++;
        gbc.insets =
            new Insets(
                12,
                0,
                0,
                0
            );

        errorLabel =
            new JLabel(" ");

        errorLabel.setForeground(
            ToolkitColors.ERROR
        );

        panel.add(
            errorLabel,
            gbc
        );

        gbc.gridy++;
        gbc.weighty = 1.0;

        panel.add(
            Box.createVerticalGlue(),
            gbc
        );

        return panel;
    }

    private JPanel createFolderSelector() {

        JPanel panel =
            new JPanel(
                new BorderLayout(
                    8,
                    0
                )
            );

        panel.setOpaque(false);

        projectFolderField =
            new JTextField();

        browseButton =
            new JButton(
                "Browse..."
            );

        ToolkitStyles.styleSecondaryButton(
            browseButton
        );

        panel.add(
            projectFolderField,
            BorderLayout.CENTER
        );

        panel.add(
            browseButton,
            BorderLayout.EAST
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

        createButton =
            new JButton(
                "Create Mod"
            );

        ToolkitStyles.styleSecondaryButton(
            cancelButton
        );

        ToolkitStyles.stylePrimaryButton(
            createButton
        );

        footer.add(
            cancelButton
        );

        footer.add(
            createButton
        );

        return footer;
    }

    private JLabel createFieldLabel(
        String text
    ) {

        JLabel label =
            new JLabel(
                text
            );

        label.setForeground(
            ToolkitColors.TEXT_PRIMARY
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.BOLD,
                13f
            )
        );

        return label;
    }

    private JLabel createHintLabel(
        String text
    ) {

        JLabel label =
            new JLabel(
                text
            );

        label.setForeground(
            ToolkitColors.TEXT_SECONDARY
        );

        label.setFont(
            label.getFont().deriveFont(
                Font.PLAIN,
                11f
            )
        );

        return label;
    }

    private void wireEvents() {

        cancelButton.addActionListener(
            e -> dispose()
        );

        createButton.addActionListener(
            e -> attemptCreate()
        );

        browseButton.addActionListener(
            e -> browseForFolder()
        );

        modNameField
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(
                        DocumentEvent e
                    ) {
                        updateInternalId();
                    }

                    @Override
                    public void removeUpdate(
                        DocumentEvent e
                    ) {
                        updateInternalId();
                    }

                    @Override
                    public void changedUpdate(
                        DocumentEvent e
                    ) {
                        updateInternalId();
                    }
                }
            );

        internalIdField
            .getDocument()
            .addDocumentListener(
                new DocumentListener() {

                    @Override
                    public void insertUpdate(
                        DocumentEvent e
                    ) {
                        internalIdChanged();
                    }

                    @Override
                    public void removeUpdate(
                        DocumentEvent e
                    ) {
                        internalIdChanged();
                    }

                    @Override
                    public void changedUpdate(
                        DocumentEvent e
                    ) {
                        internalIdChanged();
                    }
                }
            );
    }

    private void internalIdChanged() {

        if (!suppressInternalIdChange) {
            internalIdManuallyEdited = true;
        }
    }

    private void updateInternalId() {

        if (internalIdManuallyEdited) {
            return;
        }

        String generatedId =
            createInternalId(
                modNameField.getText()
            );

        suppressInternalIdChange = true;

        internalIdField.setText(
            generatedId
        );

        suppressInternalIdChange = false;
    }

    private String createInternalId(
        String name
    ) {

        if (name == null) {
            return "";
        }

        String id =
            name
                .trim()
                .toLowerCase();

        id =
            id.replaceAll(
                "[^a-z0-9]+",
                "_"
            );

        id =
            id.replaceAll(
                "^_+|_+$",
                ""
            );

        return id;
    }

    private void browseForFolder() {

        JFileChooser chooser =
            new JFileChooser();

        chooser.setDialogTitle(
            "Choose Project Folder"
        );

        chooser.setFileSelectionMode(
            JFileChooser.DIRECTORIES_ONLY
        );

        chooser.setAcceptAllFileFilterUsed(
            false
        );

        String currentPath =
            projectFolderField
                .getText()
                .trim();

        if (!currentPath.isEmpty()) {

            File current =
                new File(
                    currentPath
                );

            if (current.exists()) {
                chooser.setCurrentDirectory(
                    current
                );
            }
        }

        int result =
            chooser.showOpenDialog(
                this
            );

        if (
            result
                == JFileChooser.APPROVE_OPTION
        ) {

            File selected =
                chooser.getSelectedFile();

            projectFolderField.setText(
                selected.getAbsolutePath()
            );
        }
    }

    private void attemptCreate() {

        String validationError =
            validateForm();

        if (validationError != null) {

            errorLabel.setText(
                validationError
            );

            return;
        }

        errorLabel.setText(" ");

        approved = true;

        dispose();
    }

    private String validateForm() {

        String modName =
            getModName();

        String internalId =
            getInternalId();

        String projectFolder =
            getProjectFolder();

        if (modName.isEmpty()) {
            return "Please enter a mod name.";
        }

        if (internalId.isEmpty()) {
            return "Please enter an internal ID.";
        }

        if (
            !internalId.matches(
                "[a-z0-9_]+"
            )
        ) {

            return "Internal ID can only contain lowercase letters, numbers, and underscores.";
        }

        if (projectFolder.isEmpty()) {
            return "Please choose a project folder.";
        }

        File folder =
            new File(
                projectFolder
            );

        if (
            folder.exists()
                && !folder.isDirectory()
        ) {

            return "The selected project path is not a folder.";
        }

        return null;
    }

    public boolean isApproved() {
        return approved;
    }

    public String getModName() {

        return modNameField
            .getText()
            .trim();
    }

    public String getInternalId() {

        return internalIdField
            .getText()
            .trim();
    }

    public String getAuthor() {

        return authorField
            .getText()
            .trim();
    }

    public String getDescription() {

        return descriptionArea
            .getText()
            .trim();
    }

    public String getProjectFolder() {

        return projectFolderField
            .getText()
            .trim();
    }
}
