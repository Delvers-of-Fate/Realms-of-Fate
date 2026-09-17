package com.realmsoffate.toolkit.json;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Universal JSON/.dat persistence. It has no knowledge of items, monsters,
 * spells, or schemas; it only guarantees a valid, structurally intact document.
 */
public final class JsonDocumentService {
    public JsonDocument loadDocument(File file) throws IOException {
        JsonValue root = load(file);
        return root == null ? null : new JsonDocument(file, root);
    }

    public JsonValue load(File file) throws IOException {
        if (file == null || !file.isFile()) return null;
        String text = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        try {
            JsonValue root = new JsonReader().parse(text);
            JsonTree.repair(root);
            JsonTree.assertWellFormed(root);
            return root;
        }
        catch (Exception ex) {
            throw new IOException("Could not parse " + file.getName() + ": " + ex.getMessage(), ex);
        }
    }

    public void save(File file, JsonValue root) throws IOException {
        if (file == null) throw new IOException("No data file was selected.");
        if (root == null) throw new IOException("Cannot save empty data.");

        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Could not create " + parent.getAbsolutePath());
        }

        try {
            JsonTree.repair(root);
            JsonTree.assertWellFormed(root);
        }
        catch (RuntimeException ex) {
            throw new IOException("JSON tree is structurally invalid: " + ex.getMessage(), ex);
        }

        String text = root.prettyPrint(JsonWriter.OutputType.json, 1);
        JsonValue reparsed;
        try {
            reparsed = new JsonReader().parse(text);
            JsonTree.repair(reparsed);
            JsonTree.assertWellFormed(reparsed);
        }
        catch (Exception ex) {
            throw new IOException("Generated JSON failed validation: " + ex.getMessage(), ex);
        }

        File temp = new File(file.getAbsolutePath() + ".tmp");
        Files.write(temp.toPath(), text.getBytes(StandardCharsets.UTF_8));

        // Validate the exact bytes that reached disk before replacing the real file.
        try {
            JsonValue disk = new JsonReader().parse(new String(Files.readAllBytes(temp.toPath()), StandardCharsets.UTF_8));
            JsonTree.repair(disk);
            JsonTree.assertWellFormed(disk);
        }
        catch (Exception ex) {
            Files.deleteIfExists(temp.toPath());
            throw new IOException("Temporary JSON failed verification: " + ex.getMessage(), ex);
        }

        if (file.exists()) {
            File backup = new File(file.getAbsolutePath() + ".bak");
            Files.copy(file.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }

        try {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        }
        catch (Exception ignored) {
            Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
