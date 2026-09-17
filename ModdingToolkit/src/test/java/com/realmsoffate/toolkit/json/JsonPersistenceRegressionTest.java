package com.realmsoffate.toolkit.json;

import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.project.ModProject;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Standalone regression suite for the data-loss bug that originally affected
 * toolkit saves. Run with assertions enabled or simply execute main().
 */
public final class JsonPersistenceRegressionTest {
    public static void main(String[] args) throws Exception {
        testCopyThenRemoveDoesNotLoseSiblings();
        testNoEditRoundTripPreservesData();
        testSingleFieldEditPreservesUnknownFields();
        testItemsServiceReplacesWholeObject();
        System.out.println("JsonPersistenceRegressionTest: PASS");
    }

    private static void testCopyThenRemoveDoesNotLoseSiblings() {
        JsonValue original = new JsonReader().parse(
                "{\"class\":\"Wand\",\"name\":\"Test\",\"description\":\"Keep\",\"cost\":5,\"spell\":{\"class\":\"Spell\"}}"
        );
        JsonValue copy = JsonTree.copy(original);
        JsonTree.remove(copy, "cost");

        check(copy.get("class") != null, "removing cost deleted class");
        check(copy.get("name") != null, "removing cost deleted name");
        check(copy.get("description") != null, "removing cost deleted description");
        check(copy.get("spell") != null, "removing cost deleted spell");
        check(copy.get("cost") == null, "cost was not removed");
        JsonTree.assertWellFormed(copy);
    }

    private static void testNoEditRoundTripPreservesData() throws Exception {
        Path source = new File("src/main/resources/templates/test-items.dat").toPath();
        String text = new String(Files.readAllBytes(source), StandardCharsets.UTF_8);
        JsonValue original = new JsonReader().parse(text);
        JsonValue copy = JsonTree.copy(original);

        check(canonical(original).equals(canonical(copy)), "deep copy changed JSON data");
        JsonTree.assertWellFormed(copy);
    }

    private static void testSingleFieldEditPreservesUnknownFields() {
        JsonValue original = new JsonReader().parse(
                "{\"class\":\"Example\",\"name\":\"Old\",\"unknownEngineField\":{\"nested\":[1,2,3]},\"flag\":true}"
        );
        JsonValue edited = JsonTree.copy(original);
        ItemsDataService.putString(edited, "name", "New");

        JsonValue originalWithoutName = JsonTree.copy(original);
        JsonValue editedWithoutName = JsonTree.copy(edited);
        JsonTree.remove(originalWithoutName, "name");
        JsonTree.remove(editedWithoutName, "name");

        check("New".equals(ItemsDataService.getString(edited, "name", "")), "name edit failed");
        check(canonical(originalWithoutName).equals(canonical(editedWithoutName)), "editing name changed unrelated fields");
    }

    private static void testItemsServiceReplacesWholeObject() throws Exception {
        Path temp = Files.createTempDirectory("rof-toolkit-json-test");
        Path data = temp.resolve("data");
        Files.createDirectories(data);
        Files.copy(new File("src/main/resources/templates/test-items.dat").toPath(), data.resolve("items.dat"));

        ModProject project = new ModProject("Regression", temp.toFile());
        ItemsDataService service = new ItemsDataService();
        ItemsDataService.ItemEntry wand = service.listItems(project, "wands", ItemsDataService.WAND_CLASS, false).get(0);
        JsonValue edited = service.createEditableCopy(wand.getJson());

        JsonValue unknown = new JsonValue(JsonValue.ValueType.object);
        ItemsDataService.putString(unknown, "future", "preserve-me");
        JsonTree.put(edited, "futureEngineObject", unknown);
        ItemsDataService.putString(edited, "name", "Regression Wand");
        service.saveItem(project, wand, null, edited, false);

        JsonValue savedRoot = new JsonDocumentService().load(data.resolve("items.dat").toFile());
        JsonValue savedWand = savedRoot.get("wands").child;
        check("Regression Wand".equals(ItemsDataService.getString(savedWand, "name", "")), "saved name did not persist");
        check(savedWand.get("futureEngineObject") != null, "unknown object field was dropped");
        check("preserve-me".equals(ItemsDataService.getString(savedWand.get("futureEngineObject"), "future", "")), "unknown nested field changed");
        check(data.resolve("items.dat.bak").toFile().isFile(), "backup file was not created");
        JsonTree.assertWellFormed(savedRoot);
    }

    private static String canonical(JsonValue value) {
        return value.toJson(JsonWriter.OutputType.json);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
