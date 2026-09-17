package com.realmsoffate.toolkit.project;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public final class RecentProjects {
    private static final int MAX = 8;
    private static final Preferences PREFS = Preferences.userNodeForPackage(RecentProjects.class);
    private RecentProjects() { }

    public static void remember(File root) {
        if (root == null) return;
        String path = root.getAbsoluteFile().getAbsolutePath();
        List<File> files = load();
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        for (File file : files) {
            String other = file.getAbsolutePath();
            if (!other.equalsIgnoreCase(path) && paths.size() < MAX) paths.add(other);
        }
        PREFS.putInt("count", paths.size());
        for (int i = 0; i < MAX; i++) {
            if (i < paths.size()) PREFS.put("path." + i, paths.get(i));
            else PREFS.remove("path." + i);
        }
    }

    public static List<File> load() {
        List<File> result = new ArrayList<File>();
        int count = Math.min(MAX, Math.max(0, PREFS.getInt("count", 0)));
        for (int i = 0; i < count; i++) {
            String path = PREFS.get("path." + i, "").trim();
            if (!path.isEmpty()) result.add(new File(path));
        }
        return result;
    }
}
