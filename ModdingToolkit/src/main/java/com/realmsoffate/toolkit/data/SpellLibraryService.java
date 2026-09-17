package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/** Toolkit-only reusable spell definitions. Delver never reads these files directly. */
public class SpellLibraryService {
    public static final String MAGIC_MISSILE_CLASS = "com.interrupt.dungeoneer.entities.spells.MagicMissile";
    private final DelverDataFileService files = new DelverDataFileService();

    public static class SpellDefinition {
        private final File file;
        private final JsonValue root;

        public SpellDefinition(File file, JsonValue root) {
            this.file = file;
            this.root = root;
        }

        public File getFile() { return file; }
        public JsonValue getRoot() { return root; }
        public String getName() { return getString(root, "name", "Unnamed Spell"); }
        public JsonValue getSpell() {
            JsonValue spell = root.get("spell");
            return spell != null && spell.isObject() ? spell : null;
        }
        public String getClassName() {
            JsonValue spell = getSpell();
            return spell == null ? "" : getString(spell, "class", MAGIC_MISSILE_CLASS);
        }
    }

    public File libraryDirectory(ModProject project) {
        return new File(project.getToolkitDirectory(), "spells");
    }

    public List<SpellDefinition> list(ModProject project) throws IOException {
        List<SpellDefinition> out = new ArrayList<SpellDefinition>();
        File dir = libraryDirectory(project);
        File[] json = dir.listFiles((d, n) -> n.toLowerCase().endsWith(".json"));
        if (json == null) return out;
        Arrays.sort(json, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
        for (File file : json) {
            JsonValue root = files.load(file);
            if (root != null && root.isObject() && root.get("spell") != null) out.add(new SpellDefinition(file, root));
        }
        return out;
    }

    public SpellDefinition createDraft() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        putString(root, "name", "New Spell");
        JsonValue spell = new JsonValue(JsonValue.ValueType.object);
        JsonTree.put(root, "spell", spell);
        putString(spell, "class", MAGIC_MISSILE_CLASS);
        putInt(spell, "mpCost", 1);
        return new SpellDefinition(null, root);
    }

    public SpellDefinition editableCopy(SpellDefinition definition) {
        return new SpellDefinition(definition.file, JsonTree.copy(definition.root));
    }

    public SpellDefinition save(ModProject project, SpellDefinition definition, String name, JsonValue spell) throws IOException {
        if (name == null || name.trim().isEmpty()) throw new IOException("Spell name is required.");
        if (spell == null || !spell.isObject()) throw new IOException("Spell data is invalid.");

        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        putString(root, "name", name.trim());
        JsonTree.put(root, "spell", JsonTree.copy(spell));

        File dir = libraryDirectory(project);
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Could not create spell library folder.");
        File target = definition.file != null
                ? definition.file
                : new File(dir, uniqueFileName(dir, slug(name.trim()) + ".json"));
        files.save(target, root);
        return new SpellDefinition(target, root);
    }

    public void delete(SpellDefinition definition) throws IOException {
        if (definition.file != null && definition.file.exists() && !definition.file.delete()) {
            throw new IOException("Could not delete " + definition.file.getName());
        }
    }

    public JsonValue copySpell(SpellDefinition definition) {
        return definition == null || definition.getSpell() == null ? null : JsonTree.copy(definition.getSpell());
    }

    public static JsonValue normalizeMagicSpell(JsonValue spell) {
        if (spell == null || !spell.isObject()) return spell;
        String className = getString(spell, "class", "").trim();
        if (className.isEmpty()) putString(spell, "class", MAGIC_MISSILE_CLASS);
        if (spell.get("mpCost") == null) putInt(spell, "mpCost", 1);
        return spell;
    }

    private String uniqueFileName(File dir, String base) {
        File file = new File(dir, base);
        if (!file.exists()) return base;
        String stem = base.substring(0, base.length() - 5);
        int i = 2;
        while (new File(dir, stem + "_" + i + ".json").exists()) i++;
        return stem + "_" + i + ".json";
    }

    private String slug(String value) {
        String slug = value.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_+|_+$", "");
        return slug.isEmpty() ? "spell" : slug;
    }

    public static String getString(JsonValue object, String key, String fallback) {
        return ItemsDataService.getString(object, key, fallback);
    }

    public static int getInt(JsonValue object, String key, int fallback) {
        return ItemsDataService.getInt(object, key, fallback);
    }

    public static void putString(JsonValue object, String key, String value) {
        ItemsDataService.putString(object, key, value == null ? "" : value);
    }

    public static void putInt(JsonValue object, String key, int value) {
        ItemsDataService.putInt(object, key, value);
    }

    public static void remove(JsonValue object, String key) {
        ItemsDataService.remove(object, key);
    }

    public static JsonValue deepCopy(JsonValue value) {
        return JsonTree.copy(value);
    }
}
