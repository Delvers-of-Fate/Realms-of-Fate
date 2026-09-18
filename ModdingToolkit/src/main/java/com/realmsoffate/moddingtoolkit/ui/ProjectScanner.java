package com.realmsoffate.moddingtoolkit.ui;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Finds Delver JSON/DAT content without making serialization assumptions. */
public final class ProjectScanner {
    public enum Kind { ITEMS, MONSTERS, SPELLS, LEVELS, OTHER }
    private ProjectScanner() {}

    public static Map<Kind,List<Path>> scan(Path root) throws IOException {
        final Map<Kind,List<Path>> out = new EnumMap<Kind,List<Path>>(Kind.class);
        for (Kind k : Kind.values()) out.put(k, new ArrayList<Path>());
        Files.walk(root).filter(Files::isRegularFile).forEach(p -> {
            String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
            if (!(n.endsWith(".dat") || n.endsWith(".json"))) return;
            out.get(classify(p)).add(p);
        });
        for (List<Path> l : out.values()) Collections.sort(l);
        return out;
    }

    private static Kind classify(Path p) {
        String s = p.toString().replace('\\','/').toLowerCase(Locale.ROOT);
        String n = p.getFileName().toString().toLowerCase(Locale.ROOT);
        if (n.contains("item") || s.contains("/items/")) return Kind.ITEMS;
        if (n.contains("monster") || n.contains("enemy") || n.contains("mob") || s.contains("/monsters/") || s.contains("/enemies/")) return Kind.MONSTERS;
        if (n.contains("spell") || n.contains("magic") || s.contains("/spells/")) return Kind.SPELLS;
        if (n.contains("level") || n.contains("dungeon") || s.contains("/levels/")) return Kind.LEVELS;
        return Kind.OTHER;
    }
}
