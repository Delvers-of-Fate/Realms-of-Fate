package com.interrupt.dungeoneer.entities.items;

import com.interrupt.dungeoneer.entities.Item;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Development utility for auditing fields used by Delver / Realms of Fate
 * item classes.
 *
 * This intentionally reflects ONLY the item classes listed in ITEM_CLASSES.
 *
 * It walks the inheritance chain for every item until it reaches Item.class,
 * so a Wand report, for example, contains:
 *
 *     Wand
 *     Weapon
 *     Item
 *
 * Runtime/static/transient fields are still shown, but clearly marked so
 * they are not accidentally assumed to belong in items.dat.
 */
public class ItemReflectionDump {

    // ---------------------------------------------------------------------
    // ITEM CLASSES WE WANT TO AUDIT
    // ---------------------------------------------------------------------

    private static final Class<?>[] ITEM_CLASSES = new Class<?>[] {
        Armor.class,
        BagUpgrade.class,
        Bow.class,
        Debug.class,
        Decoration.class,
        Elixer.class,
        Food.class,
        FusedBomb.class,
        Gold.class,
        Gun.class,
        ItemStack.class,
        Key.class,
        Note.class,
        Potion.class,
        QuestItem.class,
        Scroll.class,
        Sword.class,
        Wand.class,
        Weapon.class
    };

    // ---------------------------------------------------------------------
    // OUTPUT
    // ---------------------------------------------------------------------

    private static final String OUTPUT_FILE = "item-reflection-report.txt";

    public static void main(String[] args) {

        System.out.println("==============================================");
        System.out.println(" Delver / Realms of Fate Item Reflection Dump");
        System.out.println("==============================================");
        System.out.println();

        File output = new File(OUTPUT_FILE);

        try (PrintWriter writer = new PrintWriter(new FileWriter(output))) {

            writeHeader(writer);

            for (Class<?> itemClass : ITEM_CLASSES) {
                inspectItemClass(itemClass, writer);
            }

            writer.flush();

            System.out.println();
            System.out.println("==============================================");
            System.out.println(" Reflection complete!");
            System.out.println("==============================================");
            System.out.println();
            System.out.println("Report written to:");
            System.out.println(output.getAbsolutePath());

        }
        catch (Exception ex) {
            System.err.println("ERROR while generating reflection report:");
            ex.printStackTrace();
        }
    }

    // ---------------------------------------------------------------------
    // REPORT HEADER
    // ---------------------------------------------------------------------

    private static void writeHeader(PrintWriter writer) {

        writer.println("============================================================");
        writer.println("DELVER / REALMS OF FATE");
        writer.println("ITEM CLASS REFLECTION REPORT");
        writer.println("============================================================");
        writer.println();

        writer.println("Purpose:");
        writer.println("Audit Java fields available to items.dat item classes.");
        writer.println();

        writer.println("Flags:");
        writer.println("  STATIC     = class-level field");
        writer.println("  TRANSIENT  = runtime field; normally should not serialize");
        writer.println("  FINAL      = constant / non-editable field");
        writer.println("  PRIVATE    = private Java field");
        writer.println("  PROTECTED  = protected Java field");
        writer.println("  PUBLIC     = public Java field");
        writer.println();

        writer.println("IMPORTANT:");
        writer.println("The existence of a Java field does NOT automatically mean");
        writer.println("that the field should appear in items.dat.");
        writer.println();

        writer.println("============================================================");
        writer.println();
    }

    // ---------------------------------------------------------------------
    // INSPECT ONE ITEM TYPE
    // ---------------------------------------------------------------------

    private static void inspectItemClass(
        Class<?> itemClass,
        PrintWriter writer) {

        System.out.println("Reflecting: " + itemClass.getSimpleName());

        writer.println();
        writer.println("############################################################");
        writer.println("# ITEM TYPE: " + itemClass.getSimpleName());
        writer.println("############################################################");
        writer.println();

        writer.println("Full Class:");
        writer.println(itemClass.getName());
        writer.println();

        writer.println("Inheritance:");
        writer.println(buildInheritanceString(itemClass));
        writer.println();

        List<Class<?>> hierarchy = getItemHierarchy(itemClass);

        for (Class<?> currentClass : hierarchy) {
            writeDeclaredFields(currentClass, writer);
        }

        writer.println();
        writer.println("############################################################");
        writer.println("# END " + itemClass.getSimpleName());
        writer.println("############################################################");
        writer.println();
    }

    // ---------------------------------------------------------------------
    // BUILD INHERITANCE CHAIN
    // ---------------------------------------------------------------------

    private static String buildInheritanceString(Class<?> itemClass) {

        StringBuilder builder = new StringBuilder();

        Class<?> current = itemClass;

        while (current != null) {

            if (builder.length() > 0) {
                builder.append(" -> ");
            }

            builder.append(current.getSimpleName());

            if (current == Item.class) {
                break;
            }

            current = current.getSuperclass();
        }

        return builder.toString();
    }

    // ---------------------------------------------------------------------
    // GET ITEM HIERARCHY
    //
    // Example:
    //
    // Wand
    // Weapon
    // Item
    //
    // We deliberately STOP at Item.
    // ---------------------------------------------------------------------

    private static List<Class<?>> getItemHierarchy(Class<?> itemClass) {

        List<Class<?>> hierarchy = new ArrayList<Class<?>>();

        Class<?> current = itemClass;

        while (current != null) {

            hierarchy.add(current);

            if (current == Item.class) {
                break;
            }

            current = current.getSuperclass();
        }

        return hierarchy;
    }

    // ---------------------------------------------------------------------
    // WRITE FIELDS DECLARED BY ONE CLASS
    // ---------------------------------------------------------------------

    private static void writeDeclaredFields(
        Class<?> declaringClass,
        PrintWriter writer) {

        writer.println();
        writer.println("============================================================");
        writer.println("FIELDS DECLARED BY: " + declaringClass.getSimpleName());
        writer.println("============================================================");
        writer.println();

        Field[] fields = declaringClass.getDeclaredFields();

        List<Field> sortedFields = new ArrayList<Field>();

        Collections.addAll(sortedFields, fields);

        Collections.sort(sortedFields, new Comparator<Field>() {
            @Override
            public int compare(Field a, Field b) {
                return a.getName().compareToIgnoreCase(b.getName());
            }
        });

        if (sortedFields.isEmpty()) {
            writer.println("(No fields declared)");
            return;
        }

        for (Field field : sortedFields) {
            writeField(field, writer);
        }
    }

    // ---------------------------------------------------------------------
    // WRITE ONE FIELD
    // ---------------------------------------------------------------------

    private static void writeField(
        Field field,
        PrintWriter writer) {

        int modifiers = field.getModifiers();

        String fieldName = field.getName();
        String fieldType = getReadableType(field.getType());

        writer.println("Field: " + fieldName);
        writer.println("  Type:       " + fieldType);
        writer.println("  DeclaredBy: " + field.getDeclaringClass().getSimpleName());
        writer.println("  Access:     " + getAccessModifier(modifiers));

        writer.println("  Static:     " + Modifier.isStatic(modifiers));
        writer.println("  Final:      " + Modifier.isFinal(modifiers));
        writer.println("  Transient:  " + Modifier.isTransient(modifiers));
        writer.println("  Volatile:   " + Modifier.isVolatile(modifiers));

        // -------------------------------------------------------------
        // Serialization recommendation
        // -------------------------------------------------------------

        writer.println(
            "  CandidateForItemsDat: "
                + isPossibleItemsDatField(field)
        );

        // -------------------------------------------------------------
        // Extra type information
        // -------------------------------------------------------------

        Class<?> type = field.getType();

        if (type.isEnum()) {

            writer.println("  Enum:       true");

            Object[] constants = type.getEnumConstants();

            if (constants != null) {

                StringBuilder enumValues = new StringBuilder();

                for (Object constant : constants) {

                    if (enumValues.length() > 0) {
                        enumValues.append(", ");
                    }

                    enumValues.append(constant.toString());
                }

                writer.println("  Values:     " + enumValues);
            }
        }

        if (type.isArray()) {
            writer.println(
                "  ArrayType:   "
                    + getReadableType(type.getComponentType())
            );
        }

        writer.println();
    }

    // ---------------------------------------------------------------------
    // BASIC ITEMS.DAT CANDIDATE FILTER
    //
    // This DOES NOT claim the field belongs in items.dat.
    //
    // It only identifies fields that aren't obviously runtime/constants.
    //
    // We'll compare these against the real items.dat afterward.
    // ---------------------------------------------------------------------

    private static boolean isPossibleItemsDatField(Field field) {

        int modifiers = field.getModifiers();

        if (Modifier.isStatic(modifiers)) {
            return false;
        }

        if (Modifier.isTransient(modifiers)) {
            return false;
        }

        if (Modifier.isFinal(modifiers)) {
            return false;
        }

        return true;
    }

    // ---------------------------------------------------------------------
    // ACCESS MODIFIER
    // ---------------------------------------------------------------------

    private static String getAccessModifier(int modifiers) {

        if (Modifier.isPublic(modifiers)) {
            return "public";
        }

        if (Modifier.isProtected(modifiers)) {
            return "protected";
        }

        if (Modifier.isPrivate(modifiers)) {
            return "private";
        }

        return "package-private";
    }

    // ---------------------------------------------------------------------
    // FRIENDLIER TYPE NAMES
    // ---------------------------------------------------------------------

    private static String getReadableType(Class<?> type) {

        if (type.isArray()) {
            return getReadableType(type.getComponentType()) + "[]";
        }

        if (type.isPrimitive()) {
            return type.getSimpleName();
        }

        return type.getName();
    }
}
