package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.json.JsonTree;
import java.io.IOException;

/** Canonical spell structure helpers. Existing JSON wins; library data only fills missing fields. */
public final class SpellSchema {
    private SpellSchema() {}

    public static boolean isComplete(JsonValue spell) {
        return spell != null && spell.isObject()
                && hasText(spell, "class")
                && spell.get("mpCost") != null
                && spell.get("baseDamage") != null
                && spell.get("randDamage") != null
                && spell.get("speed") != null
                && spell.get("magicMissileProjectile") != null;
    }

    /**
     * Resolve an embedded monster spell to a complete canonical spell.
     * If it is already complete, it is returned as an exact deep copy.
     * For legacy/incomplete monster entries, the matching reusable spell is used as
     * the base and the embedded monster fields are overlaid on top. No hardcoded
     * class, mana cost, damage, speed, or template values are introduced here.
     */
    public static JsonValue resolveMonsterSpell(ModProject project, JsonValue embedded) throws IOException {
        JsonValue exact = SpellLibraryService.deepCopy(embedded);
        if (isComplete(exact)) return exact;

        String name = SpellLibraryService.getString(exact, "name", "").trim();
        if (name.isEmpty()) return exact;

        SpellLibraryService library = new SpellLibraryService();
        for (SpellLibraryService.SpellDefinition def : library.list(project)) {
            if (!name.equalsIgnoreCase(def.getName())) continue;
            JsonValue source = library.copySpell(def);
            if (source == null || !source.isObject()) return exact;
            // This is a legacy/incomplete embedded entry. The reusable Magic spell is
            // authoritative for spell structure/values; only Monster firing controls
            // are allowed to override it. This also removes old hardcoded repair values.
            overlayKey(source, exact, "firePattern");
            overlayKey(source, exact, "chargedFirePattern");
            overlayKey(source, exact, "projectileCount");
            overlayKey(source, exact, "chargedProjectileCount");
            overlayKey(source, exact, "projectileSpread");
            overlayKey(source, exact, "chargedProjectileSpread");
            overlayKey(source, exact, "projectileDelay");
            overlayKey(source, exact, "chargedProjectileDelay");
            overlayKey(source, exact, "patternAngleStep");
            overlayKey(source, exact, "chargedPatternAngleStep");
            setStringNoRemove(source, "name", def.getName());
            return source;
        }
        return exact;
    }

    private static void overlayKey(JsonValue target, JsonValue overrides, String key) {
        JsonValue value = overrides.get(key);
        if (value == null) return;
        JsonValue existing = target.get(key);
        if (existing == null) {
            JsonTree.put(target, key, SpellLibraryService.deepCopy(value)); return;
        }
        if (value.isString()) existing.set(value.asString());
        else if (value.isBoolean()) existing.set(value.asBoolean());
        else if (value.isLong()) existing.set(value.asLong(), null);
        else if (value.isDouble()) existing.set(value.asDouble(), null);
    }

    private static void setStringNoRemove(JsonValue target, String key, String value) {
        JsonValue existing = target.get(key);
        if (existing != null) existing.set(value);
        else JsonTree.put(target, key, new JsonValue(value));
    }

    /** Overlay all fields from overrides onto target, preserving fields not mentioned. */
    public static void overlay(JsonValue target, JsonValue overrides) {
        if (target == null || overrides == null || !target.isObject() || !overrides.isObject()) return;
        for (JsonValue child = overrides.child; child != null; child = child.next) {
            JsonTree.put(target, child.name, SpellLibraryService.deepCopy(child));
        }
    }

    public static String validationError(JsonValue spell) {
        if (spell == null || !spell.isObject()) return "spell JSON is not an object";
        if (!hasText(spell, "class")) return "missing required field 'class'";
        if (spell.get("mpCost") == null) return "missing required field 'mpCost'";
        return null;
    }

    private static boolean hasText(JsonValue object, String key) {
        JsonValue value = object == null ? null : object.get(key);
        return value != null && !value.asString().trim().isEmpty();
    }
}
