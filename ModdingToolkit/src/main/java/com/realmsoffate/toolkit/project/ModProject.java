package com.realmsoffate.toolkit.project;

import java.io.File;

public class ModProject {

    private String name;
    private String internalId;
    private String author;
    private String description;

    private File projectDirectory;

    public ModProject() {
    }

    public ModProject(
        String name,
        String internalId,
        String author,
        String description,
        File projectDirectory
    ) {

        this.name = name;
        this.internalId = internalId;
        this.author = author;
        this.description = description;
        this.projectDirectory = projectDirectory;
    }

    public String getName() {
        return name;
    }

    public String getInternalId() {
        return internalId;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }

    public File getToolkitDirectory() {
        return new File(
            projectDirectory,
            ".rof-toolkit"
        );
    }

    public File getDataDirectory() {
        return new File(
            projectDirectory,
            "data"
        );
    }

    public File getLevelsDirectory() {
        return new File(
            projectDirectory,
            "levels"
        );
    }

    public File getTexturesDirectory() {
        return new File(
            projectDirectory,
            "textures"
        );
    }

    public File getProjectFile() {
        return new File(
            getToolkitDirectory(),
            "project.json"
        );
    }

    @Override
    public String toString() {
        return name;
    }
}
