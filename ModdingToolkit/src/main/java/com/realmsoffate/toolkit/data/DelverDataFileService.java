package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.json.JsonDocumentService;

import java.io.File;
import java.io.IOException;

/**
 * Compatibility facade for the existing editors. All persistence now flows
 * through the universal JsonDocumentService.
 */
public class DelverDataFileService {
    private final JsonDocumentService documents = new JsonDocumentService();

    public JsonValue load(File file) throws IOException {
        return documents.load(file);
    }

    public void save(File file, JsonValue root) throws IOException {
        documents.save(file, root);
    }
}
