package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Items view over items.dat.
 *
 * The JSON object loaded from disk is authoritative. Editors receive a full
 * copy of an existing item and saving replaces/moves that complete object, so
 * fields unknown to the toolkit survive unchanged.
 */
public class ItemsDataService {
    public static final String SWORD_CLASS = "com.interrupt.dungeoneer.entities.items.Sword";
    public static final String BOW_CLASS = "com.interrupt.dungeoneer.entities.items.Bow";
    public static final String GUN_CLASS = "com.interrupt.dungeoneer.entities.items.Gun";
    public static final String WAND_CLASS = "com.interrupt.dungeoneer.entities.items.Wand";
    public static final String ARMOR_CLASS = "com.interrupt.dungeoneer.entities.items.Armor";
    public static final String POTION_CLASS = "com.interrupt.dungeoneer.entities.items.Potion";
    public static final String SCROLL_CLASS = "com.interrupt.dungeoneer.entities.items.Scroll";
    public static final String FOOD_CLASS = "com.interrupt.dungeoneer.entities.items.Food";
    public static final String DECORATION_CLASS = "com.interrupt.dungeoneer.entities.items.Decoration";
    public static final String ITEM_STACK_CLASS = "com.interrupt.dungeoneer.entities.items.ItemStack";
    public static final String ITEM_CLASS = "com.interrupt.dungeoneer.entities.Item";

    private static final String SCHEMA = "http://delverengine.com/jsonschema/current/managers/ItemManager.schema.json";

    private final DelverDataFileService files = new DelverDataFileService();
    private final ItemTemplateService templates = new ItemTemplateService();

    public static class SwordEntry {
        private final String tier;
        private final int index;
        private final JsonValue json;

        public SwordEntry(String tier, int index, JsonValue json) {
            this.tier = tier;
            this.index = index;
            this.json = json;
        }

        public String getTier() { return tier; }
        public int getIndex() { return index; }
        public JsonValue getJson() { return json; }
        public String getName() { return getString(json, "name", "Unnamed Sword"); }
    }

    public static class BowEntry {
        private final String tier;
        private final int index;
        private final JsonValue json;

        public BowEntry(String tier, int index, JsonValue json) {
            this.tier = tier;
            this.index = index;
            this.json = json;
        }

        public String getTier() { return tier; }
        public int getIndex() { return index; }
        public JsonValue getJson() { return json; }
        public String getName() { return getString(json, "name", "Unnamed Bow"); }
    }

    public static class ItemEntry {
        private final String category;
        private final String tier;
        private final int index;
        private final JsonValue json;

        public ItemEntry(String category, String tier, int index, JsonValue json) {
            this.category = category;
            this.tier = tier;
            this.index = index;
            this.json = json;
        }

        public String getCategory() { return category; }
        public String getTier() { return tier; }
        public int getIndex() { return index; }
        public JsonValue getJson() { return json; }
        public String getName() { return getString(json, "name", "Unnamed Item"); }
        public String getClassName() { return getString(json, "class", ITEM_CLASS); }
    }

    public List<SwordEntry> listSwords(ModProject project) throws IOException {
        List<SwordEntry> result = new ArrayList<SwordEntry>();
        JsonValue bucket = loadOrCreate(project).get("melee");
        if (bucket == null || !bucket.isObject()) return result;

        for (JsonValue tier = bucket.child; tier != null; tier = tier.next) {
            if (!tier.isArray()) continue;
            int index = 0;
            for (JsonValue item = tier.child; item != null; item = item.next, index++) {
                if (item.isObject() && matchesClass(item, SWORD_CLASS)) result.add(new SwordEntry(tier.name, index, item));
            }
        }
        return result;
    }

    public List<BowEntry> listBows(ModProject project) throws IOException {
        List<BowEntry> result = new ArrayList<BowEntry>();
        JsonValue bucket = loadOrCreate(project).get("ranged");
        if (bucket == null || !bucket.isObject()) return result;

        for (JsonValue tier = bucket.child; tier != null; tier = tier.next) {
            if (!tier.isArray()) continue;
            int index = 0;
            for (JsonValue item = tier.child; item != null; item = item.next, index++) {
                if (item.isObject() && matchesClass(item, BOW_CLASS)) result.add(new BowEntry(tier.name, index, item));
            }
        }
        return result;
    }

    public List<ItemEntry> listItems(ModProject project, String category, String className, boolean tiered) throws IOException {
        JsonValue root = loadOrCreate(project);
        List<ItemEntry> result = new ArrayList<ItemEntry>();
        JsonValue bucket = root.get(category);
        if (bucket == null) return result;

        if (tiered && bucket.isObject()) {
            for (JsonValue tier = bucket.child; tier != null; tier = tier.next) {
                if (tier.isArray()) scanArray(result, category, tier.name, tier, className);
            }
        }
        else if (bucket.isArray()) {
            scanArray(result, category, null, bucket, className);
        }
        return result;
    }

    private void scanArray(List<ItemEntry> out, String category, String tier, JsonValue array, String className) {
        int index = 0;
        for (JsonValue value = array.child; value != null; value = value.next, index++) {
            if (value.isObject() && matchesClass(value, className)) out.add(new ItemEntry(category, tier, index, value));
        }
    }

    private boolean matchesClass(JsonValue value, String className) {
        if (className == null) return true;
        String actual = getString(value, "class", "").trim();
        if (className.equals(actual)) return true;
        return simpleName(className).equals(simpleName(actual));
    }

    private String simpleName(String className) {
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    public SwordEntry createSwordDraft() {
        return new SwordEntry("1", -1, templates.requireTemplate("melee", SWORD_CLASS, "New Sword"));
    }

    public BowEntry createBowDraft() {
        return new BowEntry("1", -1, templates.requireTemplate("ranged", BOW_CLASS, "New Bow"));
    }

    public ItemEntry createItemDraft(String category, String tier, String className, String itemType, String displayName) {
        JsonValue value = templates.createTemplate(category, className, displayName);
        if (value == null) {
            value = new JsonValue(JsonValue.ValueType.object);
            putString(value, "class", className);
            if (itemType != null && !itemType.trim().isEmpty()) putString(value, "itemType", itemType);
            putString(value, "name", displayName);
        }
        return new ItemEntry(category, tier, -1, value);
    }

    private ItemEntry createKnownGoodDraft(String category, String tier, String className, String displayName) {
        return new ItemEntry(category, tier, -1, templates.requireTemplate(category, className, displayName));
    }

    public ItemEntry createGunDraft() { return createKnownGoodDraft("ranged", "1", GUN_CLASS, "New Gun"); }
    public ItemEntry createWandDraft() { return createKnownGoodDraft("wands", null, WAND_CLASS, "New Wand"); }
    public ItemEntry createArmorDraft() { return createKnownGoodDraft("armor", "1", ARMOR_CLASS, "New Armor"); }
    public ItemEntry createPotionDraft() { return createKnownGoodDraft("potions", null, POTION_CLASS, "New Potion"); }
    public ItemEntry createScrollDraft() { return createKnownGoodDraft("scrolls", null, SCROLL_CLASS, "New Scroll"); }
    public ItemEntry createFoodDraft() { return createKnownGoodDraft("food", null, FOOD_CLASS, "New Food"); }
    public ItemEntry createDecorationDraft() { return createKnownGoodDraft("decorations", null, DECORATION_CLASS, "New Decoration"); }
    public ItemEntry createJunkDraft() { return createKnownGoodDraft("junk", null, DECORATION_CLASS, "New Junk Item"); }
    public ItemEntry createUniqueDraft() { return createKnownGoodDraft("unique", null, SWORD_CLASS, "New Unique Item"); }

    public void saveSword(ModProject project, SwordEntry entry, String targetTier, JsonValue editedJson) throws IOException {
        if (entry == null) throw new IOException("No sword is being edited.");
        saveTiered(project, "melee", entry.getTier(), entry.getIndex(), targetTier, editedJson, "sword");
    }

    public void saveBow(ModProject project, BowEntry entry, String targetTier, JsonValue editedJson) throws IOException {
        if (entry == null) throw new IOException("No bow is being edited.");
        saveTiered(project, "ranged", entry.getTier(), entry.getIndex(), targetTier, editedJson, "bow");
    }

    private void saveTiered(ModProject project, String category, String oldTier, int index,
                            String targetTier, JsonValue edited, String label) throws IOException {
        requireObject(edited, label);
        JsonValue root = loadOrCreate(project);
        String newTier = normalizeTier(targetTier);
        JsonValue replacement = JsonTree.copy(edited);

        if (index < 0) {
            JsonTree.append(ensureTierArray(root, category, newTier), replacement);
            files.save(itemsFile(project), root);
            return;
        }

        String sourceTier = normalizeTier(oldTier);
        JsonValue source = ensureTierArray(root, category, sourceTier);
        if (JsonTree.childAt(source, index) == null) {
            throw new IOException("The " + label + " could not be found in items.dat. Refresh Items and try again.");
        }

        if (sourceTier.equals(newTier)) {
            JsonTree.replaceAt(source, index, replacement);
        }
        else {
            JsonTree.removeAt(source, index);
            JsonTree.append(ensureTierArray(root, category, newTier), replacement);
        }
        files.save(itemsFile(project), root);
    }

    public void saveItem(ModProject project, ItemEntry entry, String targetTier, JsonValue edited, boolean tiered) throws IOException {
        if (entry == null) throw new IOException("No item is being edited.");
        requireObject(edited, "item");

        JsonValue root = loadOrCreate(project);
        JsonValue replacement = JsonTree.copy(edited);
        JsonValue source = tiered
                ? ensureTierArray(root, entry.category, normalizeTier(entry.tier))
                : ensureArray(root, entry.category);
        JsonValue target = tiered
                ? ensureTierArray(root, entry.category, normalizeTier(targetTier))
                : ensureArray(root, entry.category);

        if (entry.index < 0) {
            JsonTree.append(target, replacement);
        }
        else {
            if (JsonTree.childAt(source, entry.index) == null) {
                throw new IOException("The item could not be found in items.dat. Refresh Items and try again.");
            }
            if (source == target) JsonTree.replaceAt(source, entry.index, replacement);
            else {
                JsonTree.removeAt(source, entry.index);
                JsonTree.append(target, replacement);
            }
        }
        files.save(itemsFile(project), root);
    }

    public JsonValue loadOrCreate(ModProject project) throws IOException {
        JsonValue root = files.load(itemsFile(project));
        if (root == null) return createBlankRoot();
        if (!root.isObject()) throw new IOException("items.dat must contain a JSON object.");
        return root;
    }

    public JsonValue createEditableCopy(JsonValue source) {
        return JsonTree.copy(source);
    }

    private JsonValue createBlankRoot() {
        JsonValue root = new JsonValue(JsonValue.ValueType.object);
        putString(root, "$schema", SCHEMA);
        putArray(root, "unique");
        putTierContainer(root, "melee");
        putTierContainer(root, "ranged");
        putTierContainer(root, "armor");
        putArray(root, "wands");
        putArray(root, "potions");
        putArray(root, "scrolls");
        putArray(root, "decorations");
        putArray(root, "junk");
        putArray(root, "food");
        putArray(root, "weaponEnchantments");
        putArray(root, "weaponPrefixEnchantments");
        putArray(root, "armorEnchantments");
        putArray(root, "armorPrefixEnchantments");
        return root;
    }

    private JsonValue ensureArray(JsonValue root, String name) {
        JsonValue value = root.get(name);
        if (value != null && value.isArray()) return value;
        JsonValue replacement = new JsonValue(JsonValue.ValueType.array);
        JsonTree.put(root, name, replacement);
        return replacement;
    }

    private JsonValue ensureTierArray(JsonValue root, String tierName) {
        return ensureTierArray(root, "melee", tierName);
    }

    private JsonValue ensureTierArray(JsonValue root, String category, String tierName) {
        JsonValue bucket = root.get(category);
        if (bucket == null || !bucket.isObject()) {
            bucket = new JsonValue(JsonValue.ValueType.object);
            JsonTree.put(root, category, bucket);
        }
        JsonValue tier = bucket.get(tierName);
        if (tier == null || !tier.isArray()) {
            tier = new JsonValue(JsonValue.ValueType.array);
            JsonTree.put(bucket, tierName, tier);
        }
        return tier;
    }

    private File itemsFile(ModProject project) {
        return new File(project.getDataDirectory(), "items.dat");
    }

    private static String normalizeTier(String tier) {
        if (tier == null || tier.trim().isEmpty()) return "1";
        try {
            return String.valueOf(Math.max(1, Integer.parseInt(tier.trim())));
        }
        catch (Exception ignored) {
            return "1";
        }
    }

    private static void putTierContainer(JsonValue root, String name) {
        JsonValue object = new JsonValue(JsonValue.ValueType.object);
        JsonValue tier = new JsonValue(JsonValue.ValueType.array);
        JsonTree.put(object, "1", tier);
        JsonTree.put(root, name, object);
    }

    private static void putArray(JsonValue root, String name) {
        JsonTree.put(root, name, new JsonValue(JsonValue.ValueType.array));
    }

    private static void requireObject(JsonValue value, String label) throws IOException {
        if (value == null || !value.isObject()) throw new IOException("The " + label + " data is invalid.");
    }

    public static String getString(JsonValue root, String name, String fallback) {
        JsonValue value = root == null ? null : root.get(name);
        return value == null || value.isNull() ? fallback : value.asString();
    }

    public static int getInt(JsonValue root, String name, int fallback) {
        JsonValue value = root == null ? null : root.get(name);
        try { return value == null ? fallback : value.asInt(); }
        catch (Exception ignored) { return fallback; }
    }

    public static float getFloat(JsonValue root, String name, float fallback) {
        JsonValue value = root == null ? null : root.get(name);
        try { return value == null ? fallback : value.asFloat(); }
        catch (Exception ignored) { return fallback; }
    }

    public static boolean getBoolean(JsonValue root, String name, boolean fallback) {
        JsonValue value = root == null ? null : root.get(name);
        try { return value == null ? fallback : value.asBoolean(); }
        catch (Exception ignored) { return fallback; }
    }

    public static String getOptionalIntText(JsonValue root, String name) {
        JsonValue value = root == null ? null : root.get(name);
        if (value == null || value.isNull()) return "";
        try { return String.valueOf(value.asInt()); }
        catch (Exception ignored) { return ""; }
    }

    public static void putString(JsonValue root, String name, String value) {
        JsonValue existing = root == null ? null : root.get(name);
        if (existing != null && value != null) {
            existing.set(value);
            return;
        }
        JsonValue child = value == null ? new JsonValue(JsonValue.ValueType.nullValue) : new JsonValue(value);
        JsonTree.put(root, name, child);
    }

    public static void putInt(JsonValue root, String name, int value) {
        JsonValue existing = root == null ? null : root.get(name);
        if (existing != null) {
            existing.set((long) value, null);
            return;
        }
        JsonTree.put(root, name, new JsonValue((long) value));
    }

    public static void putFloat(JsonValue root, String name, float value) {
        JsonValue existing = root == null ? null : root.get(name);
        if (existing != null) {
            existing.set((double) value, null);
            return;
        }
        JsonTree.put(root, name, new JsonValue((double) value));
    }

    public static void putBoolean(JsonValue root, String name, boolean value) {
        JsonValue existing = root == null ? null : root.get(name);
        if (existing != null) {
            existing.set(value);
            return;
        }
        JsonTree.put(root, name, new JsonValue(value));
    }

    public static void remove(JsonValue root, String name) {
        JsonTree.remove(root, name);
    }
}
