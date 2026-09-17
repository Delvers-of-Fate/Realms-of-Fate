package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** Monster view over monsters.dat. Unknown fields are preserved on every edit. */
public class MonsterDataService {
    public static final String MONSTER_CLASS = "com.interrupt.dungeoneer.entities.Monster";
    private static final String SCHEMA = "http://delverengine.com/jsonschema/current/managers/MonsterManager.schema.json";

    private final DelverDataFileService files = new DelverDataFileService();

    public static class MonsterEntry {
        private final String group;
        private final int index;
        private final JsonValue json;

        public MonsterEntry(String group, int index, JsonValue json) {
            this.group = group;
            this.index = index;
            this.json = json;
        }

        public String getGroup() { return group; }
        public int getIndex() { return index; }
        public JsonValue getJson() { return json; }
        public String getName() { return ItemsDataService.getString(json, "name", "Unnamed Monster"); }
    }

    public List<MonsterEntry> list(ModProject project) throws IOException {
        JsonValue root = loadOrCreate(project);
        List<MonsterEntry> result = new ArrayList<MonsterEntry>();
        JsonValue groups = root.get("monsters");
        if (groups == null || !groups.isObject()) return result;

        for (JsonValue group = groups.child; group != null; group = group.next) {
            if (!group.isArray()) continue;
            int index = 0;
            for (JsonValue monster = group.child; monster != null; monster = monster.next, index++) {
                if (monster.isObject()) result.add(new MonsterEntry(group.name, index, monster));
            }
        }
        return result;
    }

    public MonsterEntry createDraft() {
        JsonValue template = loadTestMonsterTemplate();
        if (template == null) throw new IllegalStateException("Known-good test monster template is missing.");
        ItemsDataService.putString(template, "name", "New Monster");
        return new MonsterEntry("CUSTOM", -1, template);
    }

    private JsonValue loadTestMonsterTemplate() {
        try (InputStream in = MonsterDataService.class.getResourceAsStream("/templates/test-monsters.dat")) {
            if (in == null) return null;
            JsonValue root = new JsonReader().parse(new InputStreamReader(in, "UTF-8"));
            JsonValue monsters = root.get("monsters");
            if (monsters == null || !monsters.isObject()) return null;
            JsonValue test = monsters.get("TEST");
            if (test == null || !test.isArray() || test.child == null) return null;
            return JsonTree.copy(test.child);
        }
        catch (Exception ex) {
            throw new IllegalStateException("Could not load known-good test monster template.", ex);
        }
    }

    public static JsonValue createDefaultRangedSpell() {
        String json = "{\"class\":\"com.interrupt.dungeoneer.entities.spells.MagicMissile\",\"mpCost\":8,\"baseDamage\":3,\"randDamage\":3,\"speed\":0.1,\"spellColor\":{\"r\":0.6172,\"g\":0.0937,\"b\":0.7695},\"castSound\":\"magic/mg_fire_shoot_01.mp3,magic/mg_fire_shoot_02.mp3,magic/mg_fire_shoot_03.mp3,magic/mg_fire_shoot_04.mp3\",\"hitSound\":\"magic/mg_fire_impact_01.mp3,magic/mg_fire_impact_02.mp3,magic/mg_fire_impact_03.mp3,magic/mg_fire_impact_04.mp3\",\"magicMissileProjectile\":{\"class\":\"com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile\",\"tex\":88,\"endAnimTex\":95,\"scale\":0.75,\"animSpeed\":15,\"spriteAtlas\":\"particle\",\"splashForce\":0.2,\"splashRadius\":1.75,\"fullbrite\":true,\"knockback\":0.5,\"floating\":true}}";
        JsonValue spell = new JsonReader().parse(json);
        JsonTree.repair(spell);
        return spell;
    }

    public JsonValue editable(JsonValue source) {
        return JsonTree.copy(source);
    }

    public void save(ModProject project, MonsterEntry entry, String targetGroup, JsonValue edited) throws IOException {
        if (entry == null) throw new IOException("No monster is being edited.");
        if (edited == null || !edited.isObject()) throw new IOException("Monster data is invalid.");

        String destinationName = targetGroup == null || targetGroup.trim().isEmpty() ? "CUSTOM" : targetGroup.trim();
        JsonValue root = loadOrCreate(project);
        JsonValue groups = ensureObject(root, "monsters");
        JsonValue destination = ensureArray(groups, destinationName);
        JsonValue replacement = JsonTree.copy(edited);

        if (entry.index < 0) {
            JsonTree.append(destination, replacement);
        }
        else {
            JsonValue source = ensureArray(groups, entry.group);
            if (JsonTree.childAt(source, entry.index) == null) {
                throw new IOException("Monster no longer exists. Refresh and try again.");
            }
            if (source == destination) JsonTree.replaceAt(source, entry.index, replacement);
            else {
                JsonTree.removeAt(source, entry.index);
                JsonTree.append(destination, replacement);
            }
        }

        files.save(file(project), root);
        verifySavedMonster(project, destinationName, edited);
    }

    private void verifySavedMonster(ModProject project, String groupName, JsonValue edited) throws IOException {
        JsonValue root = loadOrCreate(project);
        JsonValue groups = root.get("monsters");
        JsonValue group = groups == null ? null : groups.get(groupName);
        String expectedName = ItemsDataService.getString(edited, "name", "");
        if (group != null && group.isArray()) {
            for (JsonValue value = group.child; value != null; value = value.next) {
                if (value.isObject() && expectedName.equals(ItemsDataService.getString(value, "name", ""))) return;
            }
        }
        throw new IOException("The monster file was written, but the saved monster could not be verified on disk.");
    }

    private JsonValue loadOrCreate(ModProject project) throws IOException {
        JsonValue root = files.load(file(project));
        if (root != null) {
            if (!root.isObject()) throw new IOException("monsters.dat must contain a JSON object.");
            return root;
        }

        root = new JsonValue(JsonValue.ValueType.object);
        ItemsDataService.putString(root, "$schema", SCHEMA);
        JsonTree.put(root, "monsters", new JsonValue(JsonValue.ValueType.object));
        return root;
    }

    private File file(ModProject project) {
        return new File(project.getDataDirectory(), "monsters.dat");
    }

    private JsonValue ensureObject(JsonValue root, String name) {
        JsonValue value = root.get(name);
        if (value != null && value.isObject()) return value;
        JsonValue replacement = new JsonValue(JsonValue.ValueType.object);
        JsonTree.put(root, name, replacement);
        return replacement;
    }

    private JsonValue ensureArray(JsonValue root, String name) {
        JsonValue value = root.get(name);
        if (value != null && value.isArray()) return value;
        JsonValue replacement = new JsonValue(JsonValue.ValueType.array);
        JsonTree.put(root, name, replacement);
        return replacement;
    }
}
