package com.realmsoffate.toolkit.project;

import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public final class ModProjectManager {

    private ModProjectManager() {
    }

    public static ModProject createProject(
        String name,
        String internalId,
        String author,
        String description,
        File parentDirectory
    ) throws ProjectCreationException {

        validateParentDirectory(
            parentDirectory
        );

        File projectDirectory =
            new File(
                parentDirectory,
                internalId
            );

        if (projectDirectory.exists()) {

            throw new ProjectCreationException(
                "A folder named \""
                    + internalId
                    + "\" already exists in the selected location."
            );
        }

        ModProject project =
            new ModProject(
                name,
                internalId,
                author,
                description,
                projectDirectory
            );

        try {

            createProjectDirectories(
                project
            );

            writeProjectMetadata(
                project
            );

            return project;

        }
        catch (Exception e) {

            deleteDirectory(
                projectDirectory
            );

            throw new ProjectCreationException(
                "The mod project could not be created.",
                e
            );
        }
    }

    private static void validateParentDirectory(
        File parentDirectory
    ) throws ProjectCreationException {

        if (parentDirectory == null) {

            throw new ProjectCreationException(
                "No project folder was selected."
            );
        }

        if (
            parentDirectory.exists()
                && !parentDirectory.isDirectory()
        ) {

            throw new ProjectCreationException(
                "The selected project location is not a folder."
            );
        }

        if (
            !parentDirectory.exists()
                && !parentDirectory.mkdirs()
        ) {

            throw new ProjectCreationException(
                "The selected project folder could not be created."
            );
        }
    }

    private static void createProjectDirectories(
        ModProject project
    ) throws ProjectCreationException {

        createDirectory(
            project.getProjectDirectory(),
            "project"
        );

        createDirectory(
            project.getToolkitDirectory(),
            "toolkit metadata"
        );

        createDirectory(
            project.getDataDirectory(),
            "data"
        );

        createDirectory(
            project.getLevelsDirectory(),
            "levels"
        );

        createDirectory(
            project.getTexturesDirectory(),
            "textures"
        );
    }

    private static void createDirectory(
        File directory,
        String description
    ) throws ProjectCreationException {

        if (directory.exists()) {

            if (!directory.isDirectory()) {

                throw new ProjectCreationException(
                    "Could not create "
                        + description
                        + " directory because a file already exists at:\n"
                        + directory.getAbsolutePath()
                );
            }

            return;
        }

        if (!directory.mkdirs()) {

            throw new ProjectCreationException(
                "Could not create "
                    + description
                    + " directory:\n"
                    + directory.getAbsolutePath()
            );
        }
    }

    private static void writeProjectMetadata(
        ModProject project
    ) throws IOException {

        Json json =
            new Json();

        json.setOutputType(
            JsonWriter.OutputType.json
        );

        ProjectMetadata metadata =
            new ProjectMetadata();

        metadata.toolkitVersion =
            "0.1.0";

        metadata.name =
            project.getName();

        metadata.internalId =
            project.getInternalId();

        metadata.author =
            project.getAuthor();

        metadata.description =
            project.getDescription();

        String output =
            json.prettyPrint(
                metadata
            );

        FileWriter writer =
            new FileWriter(
                project.getProjectFile()
            );

        try {

            writer.write(
                output
            );

        }
        finally {

            writer.close();
        }
    }

    private static void deleteDirectory(
        File file
    ) {

        if (
            file == null
                || !file.exists()
        ) {
            return;
        }

        if (file.isDirectory()) {

            File[] children =
                file.listFiles();

            if (children != null) {

                for (File child : children) {

                    deleteDirectory(
                        child
                    );
                }
            }
        }

        file.delete();
    }

    private static class ProjectMetadata {

        public String toolkitVersion;

        public String name;
        public String internalId;

        public String author;
        public String description;
    }
}
