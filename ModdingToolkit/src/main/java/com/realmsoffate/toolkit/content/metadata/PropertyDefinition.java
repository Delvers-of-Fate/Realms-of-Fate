package com.realmsoffate.toolkit.content.metadata;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PropertyDefinition {

    private final String name;
    private final String displayName;

    private final PropertyType propertyType;

    private final Class<?> javaType;
    private final Class<?> declaringClass;

    private final Field field;

    private final boolean publicField;
    private final boolean staticField;
    private final boolean finalField;
    private final boolean transientField;

    public PropertyDefinition(
        Field field,
        PropertyType propertyType
    ) {

        this.field = field;

        this.name =
            field.getName();

        this.displayName =
            makeDisplayName(
                field.getName()
            );

        this.propertyType =
            propertyType;

        this.javaType =
            field.getType();

        this.declaringClass =
            field.getDeclaringClass();

        int modifiers =
            field.getModifiers();

        this.publicField =
            Modifier.isPublic(
                modifiers
            );

        this.staticField =
            Modifier.isStatic(
                modifiers
            );

        this.finalField =
            Modifier.isFinal(
                modifiers
            );

        this.transientField =
            Modifier.isTransient(
                modifiers
            );
    }

    private String makeDisplayName(
        String fieldName
    ) {

        if (
            fieldName == null
                || fieldName.isEmpty()
        ) {

            return "";
        }

        StringBuilder builder =
            new StringBuilder();

        char[] characters =
            fieldName.toCharArray();

        for (
            int i = 0;
            i < characters.length;
            i++
        ) {

            char current =
                characters[i];

            if (
                i > 0
                    && Character.isUpperCase(
                    current
                )
            ) {

                builder.append(
                    ' '
                );
            }

            if (i == 0) {

                builder.append(
                    Character.toUpperCase(
                        current
                    )
                );

            }
            else {

                builder.append(
                    current
                );
            }
        }

        return builder.toString();
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PropertyType getPropertyType() {
        return propertyType;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public Class<?> getDeclaringClass() {
        return declaringClass;
    }

    public Field getField() {
        return field;
    }

    public boolean isPublicField() {
        return publicField;
    }

    public boolean isStaticField() {
        return staticField;
    }

    public boolean isFinalField() {
        return finalField;
    }

    public boolean isTransientField() {
        return transientField;
    }

    public boolean isEnum() {
        return javaType.isEnum();
    }

    public Object[] getEnumValues() {

        if (!javaType.isEnum()) {
            return new Object[0];
        }

        Object[] constants =
            javaType.getEnumConstants();

        if (constants == null) {
            return new Object[0];
        }

        return constants;
    }

    public boolean isNormallyEditable() {

        return !staticField
            && !finalField
            && !transientField;
    }

    @Override
    public String toString() {

        return name
            + " : "
            + javaType.getSimpleName();
    }
}
