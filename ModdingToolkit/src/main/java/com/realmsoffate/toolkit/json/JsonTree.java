package com.realmsoffate.toolkit.json;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Safe JsonValue tree operations for the LibGDX version used by Delver.
 *
 * Older JsonValue.addChild/remove implementations do not consistently maintain
 * parent/prev/size bookkeeping. All toolkit mutations should flow through this
 * class so an edit cannot silently detach neighboring JSON fields.
 */
public final class JsonTree {
    private JsonTree() { }

    public static JsonValue copy(JsonValue source) {
        if (source == null) return null;
        String json = source.toJson(JsonWriter.OutputType.json);
        JsonValue copy = new JsonReader().parse(json);
        copy.name = source.name;
        repair(copy);
        return copy;
    }

    public static JsonValue append(JsonValue parent, JsonValue child) {
        requireContainer(parent);
        if (child == null) throw new IllegalArgumentException("child cannot be null");
        detach(child);

        child.parent = parent;
        child.prev = null;
        child.next = null;

        if (parent.child == null) {
            parent.child = child;
            parent.size = 1;
            return child;
        }

        JsonValue tail = parent.child;
        int count = 1;
        while (tail.next != null) {
            tail = tail.next;
            count++;
        }
        tail.next = child;
        child.prev = tail;
        parent.size = count + 1;
        return child;
    }

    public static JsonValue put(JsonValue object, String name, JsonValue value) {
        if (object == null || !object.isObject()) throw new IllegalArgumentException("parent must be a JSON object");
        if (name == null || name.isEmpty()) throw new IllegalArgumentException("property name cannot be empty");
        if (value == null) value = new JsonValue(JsonValue.ValueType.nullValue);
        value.name = name;

        JsonValue existing = find(object, name);
        if (existing == null) return append(object, value);
        replaceNode(object, existing, value);
        return value;
    }

    public static JsonValue remove(JsonValue parent, String name) {
        if (parent == null || name == null) return null;
        JsonValue current = parent.child;
        JsonValue previous = null;
        while (current != null) {
            if (name.equals(current.name)) return unlink(parent, previous, current);
            previous = current;
            current = current.next;
        }

        // Match JsonValue#get(String)'s historical case-insensitive behavior as a fallback.
        current = parent.child;
        previous = null;
        while (current != null) {
            if (current.name != null && name.equalsIgnoreCase(current.name)) return unlink(parent, previous, current);
            previous = current;
            current = current.next;
        }
        return null;
    }

    public static JsonValue removeAt(JsonValue parent, int index) {
        if (parent == null || index < 0) return null;
        JsonValue current = parent.child;
        JsonValue previous = null;
        int i = 0;
        while (current != null) {
            if (i == index) return unlink(parent, previous, current);
            previous = current;
            current = current.next;
            i++;
        }
        return null;
    }

    public static JsonValue replaceAt(JsonValue parent, int index, JsonValue replacement) {
        if (parent == null || replacement == null || index < 0) return null;
        JsonValue current = parent.child;
        int i = 0;
        while (current != null) {
            if (i == index) {
                replaceNode(parent, current, replacement);
                return replacement;
            }
            current = current.next;
            i++;
        }
        return null;
    }

    public static JsonValue childAt(JsonValue parent, int index) {
        if (parent == null || index < 0) return null;
        int i = 0;
        for (JsonValue child = parent.child; child != null; child = child.next, i++) {
            if (i == index) return child;
        }
        return null;
    }

    public static JsonValue find(JsonValue parent, String name) {
        if (parent == null || name == null) return null;
        for (JsonValue child = parent.child; child != null; child = child.next) {
            if (name.equals(child.name)) return child;
        }
        for (JsonValue child = parent.child; child != null; child = child.next) {
            if (child.name != null && name.equalsIgnoreCase(child.name)) return child;
        }
        return null;
    }

    public static int childCount(JsonValue parent) {
        int count = 0;
        if (parent != null) for (JsonValue child = parent.child; child != null; child = child.next) count++;
        return count;
    }

    /** Rebuilds linkage metadata recursively without changing JSON content or order. */
    public static void repair(JsonValue root) {
        if (root == null) return;
        repairChildren(root);
    }

    public static void assertWellFormed(JsonValue root) {
        if (root == null) throw new IllegalStateException("JSON root is null");
        assertNode(root, null);
    }

    private static void repairChildren(JsonValue parent) {
        JsonValue previous = null;
        int count = 0;
        for (JsonValue child = parent.child; child != null; child = child.next) {
            child.parent = parent;
            child.prev = previous;
            previous = child;
            count++;
            repairChildren(child);
        }
        parent.size = count;
    }

    private static void assertNode(JsonValue node, JsonValue expectedParent) {
        if (node.parent != expectedParent && expectedParent != null) {
            throw new IllegalStateException("Broken JSON parent link at " + safeName(node));
        }
        int count = 0;
        JsonValue previous = null;
        for (JsonValue child = node.child; child != null; child = child.next) {
            if (child.prev != previous) throw new IllegalStateException("Broken JSON sibling link at " + safeName(child));
            if (child.parent != node) throw new IllegalStateException("Broken JSON parent link at " + safeName(child));
            assertNode(child, node);
            previous = child;
            count++;
        }
        if (node.size != count) throw new IllegalStateException("Broken JSON child count at " + safeName(node));
    }

    private static JsonValue unlink(JsonValue parent, JsonValue previous, JsonValue current) {
        JsonValue next = current.next;
        if (previous == null) parent.child = next;
        else previous.next = next;
        if (next != null) next.prev = previous;

        current.parent = null;
        current.prev = null;
        current.next = null;
        parent.size = childCount(parent);
        return current;
    }

    private static void replaceNode(JsonValue parent, JsonValue existing, JsonValue replacement) {
        if (existing == replacement) return;
        detach(replacement);

        JsonValue previous = null;
        JsonValue current = parent.child;
        while (current != null && current != existing) {
            previous = current;
            current = current.next;
        }
        if (current == null) throw new IllegalArgumentException("existing node is not a child of parent");

        JsonValue next = current.next;
        replacement.parent = parent;
        replacement.prev = previous;
        replacement.next = next;
        if (previous == null) parent.child = replacement;
        else previous.next = replacement;
        if (next != null) next.prev = replacement;

        current.parent = null;
        current.prev = null;
        current.next = null;
        parent.size = childCount(parent);
    }

    private static void detach(JsonValue child) {
        JsonValue parent = child.parent;
        if (parent != null) {
            JsonValue current = parent.child;
            JsonValue previous = null;
            while (current != null) {
                if (current == child) {
                    unlink(parent, previous, current);
                    return;
                }
                previous = current;
                current = current.next;
            }
        }
        child.parent = null;
        child.prev = null;
        child.next = null;
    }

    private static void requireContainer(JsonValue parent) {
        if (parent == null || (!parent.isObject() && !parent.isArray())) {
            throw new IllegalArgumentException("parent must be a JSON object or array");
        }
    }

    private static String safeName(JsonValue value) {
        return value.name == null ? "<array value>" : value.name;
    }
}
