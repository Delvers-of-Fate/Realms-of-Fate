package com.realmsoffate.toolkit.data;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.project.ModProject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SpriteSheetService {
    public static class SpriteSheet {
        private final String name;
        private final String filename;
        private final int columns;
        private final File imageFile;
        private BufferedImage image;

        public SpriteSheet(String name, String filename, int columns, File imageFile) {
            this.name = name;
            this.filename = filename;
            this.columns = columns;
            this.imageFile = imageFile;
        }

        public String getName() { return name; }
        public String getFilename() { return filename; }
        public int getColumns() { return columns; }
        public File getImageFile() { return imageFile; }

        public BufferedImage getImage() throws IOException {
            if (image == null) image = ImageIO.read(imageFile);
            if (image == null) throw new IOException("Could not read sprite sheet: " + imageFile.getAbsolutePath());
            return image;
        }

        public int getTileWidth() throws IOException {
            return Math.max(1, getImage().getWidth() / Math.max(1, columns));
        }

        public int getTileHeight() throws IOException {
            return getTileWidth();
        }

        public int getRows() throws IOException {
            return Math.max(1, getImage().getHeight() / getTileHeight());
        }

        public int getSpriteCount() throws IOException {
            return columns * getRows();
        }

        public BufferedImage getSprite(int index) throws IOException {
            if (index < 0 || index >= getSpriteCount()) return null;
            int tileWidth = getTileWidth();
            int tileHeight = getTileHeight();
            int x = (index % columns) * tileWidth;
            int y = (index / columns) * tileHeight;
            if (x + tileWidth > getImage().getWidth() || y + tileHeight > getImage().getHeight()) return null;
            return getImage().getSubimage(x, y, tileWidth, tileHeight);
        }
    }

    public List<SpriteSheet> loadAvailableSheets(ModProject project) throws IOException {
        Map<String, SpriteSheet> sheets = new LinkedHashMap<String, SpriteSheet>();
        loadEngineSheets(sheets);
        loadModSheets(project, sheets);
        return new ArrayList<SpriteSheet>(sheets.values());
    }

    public SpriteSheet findSheet(ModProject project, String name) throws IOException {
        if (name == null) return null;
        for (SpriteSheet sheet : loadAvailableSheets(project)) {
            if (name.equals(sheet.getName())) return sheet;
        }
        return null;
    }

    private void loadEngineSheets(Map<String, SpriteSheet> sheets) throws IOException {
        File dataFile = findEngineSpritesheetsFile();
        if (dataFile == null) return;
        File assetsDirectory = dataFile.getParentFile().getParentFile();
        loadFile(dataFile, assetsDirectory, sheets);
    }

    private void loadModSheets(ModProject project, Map<String, SpriteSheet> sheets) throws IOException {
        if (project == null) return;
        File dataFile = new File(project.getDataDirectory(), "spritesheets.dat");
        if (!dataFile.isFile()) return;
        loadFile(dataFile, project.getRootDirectory(), sheets);
    }

    private void loadFile(File dataFile, File assetRoot, Map<String, SpriteSheet> sheets) throws IOException {
        String text = new String(Files.readAllBytes(dataFile.toPath()), StandardCharsets.UTF_8);
        JsonValue root;
        try { root = new JsonReader().parse(text); }
        catch (Exception ex) { throw new IOException("Could not parse " + dataFile.getAbsolutePath(), ex); }
        if (root == null || !root.isArray()) return;

        JsonValue item = root.child;
        while (item != null) {
            if (item.isObject()) {
                String name = getString(item, "name", "");
                String filename = getString(item, "filename", "");
                int columns = getInt(item, "columns", 1);
                if (!name.isEmpty() && !filename.isEmpty() && columns > 0) {
                    File image = new File(assetRoot, filename.replace('/', File.separatorChar));
                    if (image.isFile()) sheets.put(name, new SpriteSheet(name, filename, columns, image));
                }
            }
            item = item.next;
        }
    }

    private File findEngineSpritesheetsFile() {
        String userDir = System.getProperty("user.dir", ".");
        File[] candidates = new File[] {
                new File(userDir, "Dungeoneer/assets/data/spritesheets.dat"),
                new File(userDir, "../Dungeoneer/assets/data/spritesheets.dat"),
                new File(userDir, "../../Dungeoneer/assets/data/spritesheets.dat")
        };
        for (File candidate : candidates) if (candidate.isFile()) return candidate.getAbsoluteFile();
        return null;
    }

    private static String getString(JsonValue root, String name, String fallback) {
        JsonValue value = root.get(name);
        return value == null || value.isNull() ? fallback : value.asString();
    }

    private static int getInt(JsonValue root, String name, int fallback) {
        JsonValue value = root.get(name);
        try { return value == null ? fallback : value.asInt(); }
        catch (Exception ignored) { return fallback; }
    }
}
