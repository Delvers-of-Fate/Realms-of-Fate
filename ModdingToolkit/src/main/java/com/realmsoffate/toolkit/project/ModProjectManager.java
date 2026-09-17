package com.realmsoffate.toolkit.project;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.realmsoffate.toolkit.json.JsonTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

public class ModProjectManager {

    /**
     * Creates a brand-new mod project.
     *
     * Only the mod root and .rof-toolkit metadata are created here.
     * Other folders such as data/ and textures/ are created lazily
     * when the toolkit actually needs them.
     */
    public ModProject createProject(String name, File root) throws IOException {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Mod name cannot be empty.");
        }

        if (root == null) {
            throw new IllegalArgumentException("Mod location cannot be empty.");
        }

        name = name.trim();

        File modRoot = new File(root, name);

        if (modRoot.exists()) {
            throw new IOException(
                "A folder named \"" + name + "\" already exists in that location."
            );
        }

        if (!modRoot.mkdirs()) {
            throw new IOException(
                "Could not create mod folder: " + modRoot.getAbsolutePath()
            );
        }

        ModProject project = new ModProject(name, modRoot);

        File toolkitDirectory = project.getToolkitDirectory();

        if (!toolkitDirectory.exists() && !toolkitDirectory.mkdirs()) {
            throw new IOException(
                "Could not create .rof-toolkit metadata folder."
            );
        }

        writeMetadata(project);

        return project;
    }

    /**
     * Opens an existing Realms of Fate / Delver mod.
     *
     * The selected mod does NOT need to have been created by the toolkit.
     *
     * If .rof-toolkit/project.json already exists, the toolkit reads the
     * stored project name from it.
     *
     * If the metadata does not exist, the toolkit adopts the existing mod
     * non-destructively by creating only:
     *
     * .rof-toolkit/project.json
     *
     * Existing data, textures, sounds and other mod files are not modified
     * by simply opening the project.
     */
    public ModProject openProject(File root) throws IOException {
        if (root == null) {
            throw new IllegalArgumentException("Mod folder cannot be empty.");
        }

        if (!root.exists()) {
            throw new IOException(
                "The selected mod folder does not exist."
            );
        }

        if (!root.isDirectory()) {
            throw new IOException(
                "The selected path is not a folder."
            );
        }

        File toolkitDirectory = new File(root, ".rof-toolkit");
        File metadata = new File(toolkitDirectory, "project.json");

        // Default to the actual mod folder name.
        String name = root.getName();

        /*
         * If toolkit metadata already exists, attempt to read the
         * stored project name.
         *
         * This uses FileInputStream because the LibGDX version used
         * by Delver 1.4.0 does not support JsonReader.parse(File).
         */
        if (metadata.isFile()) {
            try (FileInputStream input = new FileInputStream(metadata)) {
                JsonValue json = new JsonReader().parse(input);

                JsonValue nameValue = json.get("name");

                if (nameValue != null
                    && nameValue.isString()
                    && !nameValue.asString().trim().isEmpty()) {

                    name = nameValue.asString().trim();
                }
            }
            catch (Exception ignored) {
                /*
                 * A damaged toolkit metadata file should not prevent
                 * an otherwise valid Delver mod from being opened.
                 *
                 * In that case we simply keep using the folder name.
                 */
            }
        }

        ModProject project = new ModProject(name, root);

        /*
         * Adopt an existing mod non-destructively.
         *
         * We create only the toolkit's own metadata directory.
         */
        if (!toolkitDirectory.exists()) {
            if (!toolkitDirectory.mkdirs()) {
                throw new IOException(
                    "Could not create .rof-toolkit metadata in the selected mod."
                );
            }
        }

        if (!toolkitDirectory.isDirectory()) {
            throw new IOException(
                ".rof-toolkit exists but is not a directory."
            );
        }

        /*
         * If this is a normal Delver mod that has never been opened
         * by the toolkit before, create its toolkit metadata.
         */
        if (!metadata.isFile()) {
            writeMetadata(project);
        }

        return project;
    }

    /**
     * Writes the small toolkit-only project metadata file.
     */
    private void writeMetadata(ModProject project) throws IOException {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);

        JsonValue formatVersion = new JsonValue(1L);
        formatVersion.name = "formatVersion";
        JsonTree.append(root, formatVersion);

        JsonValue projectName = new JsonValue(project.getName());
        projectName.name = "name";
        JsonTree.append(root, projectName);

        File toolkitDirectory = project.getToolkitDirectory();

        if (!toolkitDirectory.exists()) {
            if (!toolkitDirectory.mkdirs()) {
                throw new IOException(
                    "Could not create toolkit metadata directory."
                );
            }
        }

        if (!toolkitDirectory.isDirectory()) {
            throw new IOException(
                "Toolkit metadata path exists but is not a directory."
            );
        }

        File metadata = new File(
            toolkitDirectory,
            "project.json"
        );

        try (Writer writer = new OutputStreamWriter(
            new FileOutputStream(metadata),
            StandardCharsets.UTF_8)) {

            writer.write(
                root.prettyPrint(
                    JsonWriter.OutputType.json,
                    2
                )
            );
        }
    }
}
