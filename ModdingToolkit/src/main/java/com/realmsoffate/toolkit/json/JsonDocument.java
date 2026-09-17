package com.realmsoffate.toolkit.json;

import com.badlogic.gdx.utils.JsonValue;

import java.io.File;

/** A loaded JSON document. The JsonValue tree is the single source of truth. */
public final class JsonDocument {
    private final File file;
    private final JsonValue root;

    public JsonDocument(File file, JsonValue root) {
        if (root == null) throw new IllegalArgumentException("root cannot be null");
        this.file = file;
        this.root = root;
    }

    public File getFile() { return file; }
    public JsonValue getRoot() { return root; }
    public JsonValue editableCopy() { return JsonTree.copy(root); }
}
