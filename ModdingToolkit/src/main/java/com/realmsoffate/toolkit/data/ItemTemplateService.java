package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.json.JsonTree;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Supplies known-good item objects copied from the working Realms of Fate
 * test items.dat. New toolkit items begin as one of these complete objects
 * instead of being assembled from an empty generic JSON object.
 */
public class ItemTemplateService {
    private static final String RESOURCE = "/templates/test-items.dat";
    private JsonValue root;

    /**
     * Returns a deep copy of a known-good object from the bundled working
     * test items.dat. This is the preferred path for every toolkit item type.
     */
    public JsonValue createTemplate(String category, String className, String displayName) {
        JsonValue source = findTemplate(category, className);
        if (source == null) return null;
        JsonValue copy = JsonTree.copy(source);
        if (displayName != null && !displayName.trim().isEmpty()) {
            JsonValue name = copy.get("name");
            if (name != null) name.set(displayName);
            else ItemsDataService.putString(copy, "name", displayName);
        }
        return copy;
    }

    /**
     * Same as createTemplate, but deliberately fails instead of allowing the
     * caller to fall back to a hand-built / partial JSON object. Supported
     * toolkit item types use this so a missing template can never silently
     * produce an unsafe items.dat entry.
     */
    public JsonValue requireTemplate(String category, String className, String displayName) {
        JsonValue value = createTemplate(category, className, displayName);
        if (value == null) {
            throw new IllegalStateException(
                    "No known-good test items.dat template for category '" + category
                            + "' and class '" + className + "'.");
        }
        return value;
    }

    private JsonValue findTemplate(String category, String className) {
        JsonValue data = load();
        JsonValue bucket = data.get(category);
        if (bucket == null) return null;

        if (bucket.isArray()) return findInArray(bucket, className);
        if (bucket.isObject()) {
            for (JsonValue tier = bucket.child; tier != null; tier = tier.next) {
                if (!tier.isArray()) continue;
                JsonValue match = findInArray(tier, className);
                if (match != null) return match;
            }
        }
        return null;
    }

    private JsonValue findInArray(JsonValue array, String className) {
        for (JsonValue item = array.child; item != null; item = item.next) {
            if (!item.isObject()) continue;
            if (className == null || sameClass(className, ItemsDataService.getString(item, "class", ""))) return item;
        }
        return null;
    }

    private boolean sameClass(String expected, String actual) {
        if (expected.equals(actual)) return true;
        int a = expected.lastIndexOf('.');
        int b = actual.lastIndexOf('.');
        return expected.substring(a + 1).equals(actual.substring(b + 1));
    }

    private synchronized JsonValue load() {
        if (root != null) return root;
        InputStream in = ItemTemplateService.class.getResourceAsStream(RESOURCE);
        if (in == null) throw new IllegalStateException("Missing toolkit item template resource: " + RESOURCE);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) out.write(buffer, 0, read);
            root = new JsonReader().parse(new String(out.toByteArray(), "UTF-8"));
            return root;
        }
        catch (IOException ex) {
            throw new IllegalStateException("Could not load toolkit item templates.", ex);
        }
        finally {
            try { in.close(); } catch (IOException ignored) { }
        }
    }


}
