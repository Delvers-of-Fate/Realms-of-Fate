package com.realmsoffate.toolkit.project;

import java.io.File;

public class ModProject {
    private final String name;
    private final File rootDirectory;

    public ModProject(String name, File rootDirectory) {
        this.name = name;
        this.rootDirectory = rootDirectory;
    }

    public String getName() {
        return name;
    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    public File getToolkitDirectory() {
        return new File(rootDirectory, ".rof-toolkit");
    }

    public File getDataDirectory() {
        return new File(rootDirectory, "data");
    }

    public File getTexturesDirectory() {
        return new File(rootDirectory, "textures");
    }
}
