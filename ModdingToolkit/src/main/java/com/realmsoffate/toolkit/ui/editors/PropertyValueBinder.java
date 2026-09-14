package com.realmsoffate.toolkit.ui.editors;

import com.badlogic.gdx.utils.JsonValue;

import com.realmsoffate.toolkit.content.metadata.PropertyDefinition;
import com.realmsoffate.toolkit.content.metadata.PropertyType;
import com.realmsoffate.toolkit.content.weapon.WeaponDefinition;

import javax.swing.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyValueBinder {

    private final Map<String, PropertyDefinition> propertyDefinitions =
        new HashMap<String, PropertyDefinition>();

    private final Map<String, String> initialControlValues =
        new HashMap<String, String>();

    private final Map<String, String> unsupportedEnumValues =
        new HashMap<String, String>();

    public PropertyValueBinder(
        List<PropertyDefinition> properties
    ) {

        for (PropertyDefinition property : properties) {

            propertyDefinitions.put(
                property.getName(),
                property
            );
        }
    }

    public void loadWeapon(
        WeaponDefinition weapon,
        Map<String, JComponent> controls
    ) {

        if (
            weapon == null
                || controls == null
        ) {
            return;
        }

        unsupportedEnumValues.clear();
        initialControlValues.clear();

        for (
            Map.Entry<String, JComponent> entry
            : controls.entrySet()
        ) {

            String propertyName =
                entry.getKey();

            JComponent control =
                entry.getValue();

            PropertyDefinition property =
                propertyDefinitions.get(
                    propertyName
                );

            if (property == null) {
                continue;
            }

            JsonValue value =
                getWeaponValue(
                    weapon,
                    propertyName
                );

            if (value != null) {

                applyValueToControl(
                    property,
                    control,
                    value
                );
            }

            initialControlValues.put(
                propertyName,
                getControlSnapshot(
                    control
                )
            );
        }
    }

    public void saveWeapon(
        WeaponDefinition weapon,
        Map<String, JComponent> controls
    ) {

        if (
            weapon == null
                || controls == null
        ) {
            return;
        }

        for (
            Map.Entry<String, JComponent> entry
            : controls.entrySet()
        ) {

            String propertyName =
                entry.getKey();

            JComponent control =
                entry.getValue();

            PropertyDefinition property =
                propertyDefinitions.get(
                    propertyName
                );

            if (property == null) {
                continue;
            }

            if (isComplexProperty(property)) {
                continue;
            }

            boolean alreadyExisted =
                weapon.wasPropertyPresent(
                    propertyName
                );

            String initialValue =
                initialControlValues.get(
                    propertyName
                );

            String currentValue =
                getControlSnapshot(
                    control
                );

            boolean changed =
                initialValue == null
                    ? currentValue != null
                    : !initialValue.equals(
                    currentValue
                );

            if (
                !alreadyExisted
                    && !changed
            ) {
                continue;
            }

            /*
             * If the original JSON contained a custom enum value
             * and the user has not changed the control, leave the
             * original JSON value completely untouched.
             */
            if (
                property.getPropertyType()
                    == PropertyType.ENUM
                    && unsupportedEnumValues.containsKey(
                    propertyName
                )
                    && !changed
            ) {
                continue;
            }

            JsonValue value =
                readControlValue(
                    property,
                    control
                );

            if (value == null) {
                continue;
            }

            setWeaponValue(
                weapon,
                propertyName,
                value
            );

            unsupportedEnumValues.remove(
                propertyName
            );
        }

        refreshSnapshots(
            controls
        );
    }

    private JsonValue getWeaponValue(
        WeaponDefinition weapon,
        String propertyName
    ) {

        if (
            !weapon.wasPropertyPresent(
                propertyName
            )
        ) {
            return null;
        }

        if (propertyName.equals("class")) {

            return stringValue(
                weapon.getClassName()
            );
        }

        if (propertyName.equals("name")) {

            return stringValue(
                weapon.getName()
            );
        }

        if (propertyName.equals("itemType")) {

            return stringValue(
                weapon.getItemType()
            );
        }

        if (propertyName.equals("tex")) {

            return new JsonValue(
                (long) weapon.getTex()
            );
        }

        if (propertyName.equals("texAtlas")) {

            return stringValue(
                weapon.getTexAtlas()
            );
        }

        return weapon.getExtraProperty(
            propertyName
        );
    }

    private void setWeaponValue(
        WeaponDefinition weapon,
        String propertyName,
        JsonValue value
    ) {

        if (propertyName.equals("class")) {

            weapon.setClassName(
                value.asString()
            );

            return;
        }

        if (propertyName.equals("name")) {

            weapon.setName(
                value.asString()
            );

            return;
        }

        if (propertyName.equals("itemType")) {

            weapon.setItemType(
                value.asString()
            );

            return;
        }

        if (propertyName.equals("tex")) {

            weapon.setTex(
                value.asInt()
            );

            return;
        }

        if (propertyName.equals("texAtlas")) {

            weapon.setTexAtlas(
                value.asString()
            );

            return;
        }

        weapon.putExtraProperty(
            propertyName,
            value
        );
    }

    private void applyValueToControl(
        PropertyDefinition property,
        JComponent control,
        JsonValue value
    ) {

        switch (
            property.getPropertyType()
        ) {

            case STRING:

                if (control instanceof JTextField) {

                    ((JTextField) control).setText(
                        value.asString()
                    );
                }

                break;

            case BOOLEAN:

                if (control instanceof JCheckBox) {

                    ((JCheckBox) control).setSelected(
                        value.asBoolean()
                    );
                }

                break;

            case INTEGER:

                if (control instanceof JSpinner) {

                    ((JSpinner) control).setValue(
                        value.asInt()
                    );
                }

                break;

            case LONG:

                if (control instanceof JSpinner) {

                    ((JSpinner) control).setValue(
                        value.asLong()
                    );
                }

                break;

            case FLOAT:

                if (control instanceof JSpinner) {

                    ((JSpinner) control).setValue(
                        Double.valueOf(
                            value.asFloat()
                        )
                    );
                }

                break;

            case DOUBLE:

                if (control instanceof JSpinner) {

                    ((JSpinner) control).setValue(
                        value.asDouble()
                    );
                }

                break;

            case ENUM:

                boolean matched =
                    setEnumValue(
                        control,
                        value.asString()
                    );

                if (!matched) {

                    addCustomEnumValue(
                        property,
                        control,
                        value.asString()
                    );
                }

                break;

            default:
                break;
        }
    }

    private boolean setEnumValue(
        JComponent control,
        String savedValue
    ) {

        if (
            !(control instanceof JComboBox)
                || savedValue == null
        ) {
            return false;
        }

        JComboBox<?> comboBox =
            (JComboBox<?>) control;

        for (
            int i = 0;
            i < comboBox.getItemCount();
            i++
        ) {

            Object item =
                comboBox.getItemAt(i);

            if (
                item != null
                    && item.toString()
                    .equalsIgnoreCase(
                        savedValue
                    )
            ) {

                comboBox.setSelectedIndex(i);

                return true;
            }
        }

        return false;
    }

    private void addCustomEnumValue(
        PropertyDefinition property,
        JComponent control,
        String savedValue
    ) {

        if (
            !(control instanceof JComboBox)
                || savedValue == null
        ) {
            return;
        }

        @SuppressWarnings("unchecked")
        JComboBox<Object> comboBox =
            (JComboBox<Object>) control;

        PropertyControlFactory.CustomEnumValue customValue =
            new PropertyControlFactory.CustomEnumValue(
                savedValue
            );

        comboBox.insertItemAt(
            customValue,
            0
        );

        comboBox.setSelectedItem(
            customValue
        );

        unsupportedEnumValues.put(
            property.getName(),
            savedValue
        );
    }

    private JsonValue readControlValue(
        PropertyDefinition property,
        JComponent control
    ) {

        switch (
            property.getPropertyType()
        ) {

            case STRING:

                if (control instanceof JTextField) {

                    return new JsonValue(
                        ((JTextField) control)
                            .getText()
                    );
                }

                break;

            case BOOLEAN:

                if (control instanceof JCheckBox) {

                    return new JsonValue(
                        ((JCheckBox) control)
                            .isSelected()
                    );
                }

                break;

            case INTEGER:
            case LONG:

                if (control instanceof JSpinner) {

                    Number number =
                        (Number) ((JSpinner) control)
                            .getValue();

                    return new JsonValue(
                        number.longValue()
                    );
                }

                break;

            case FLOAT:
            case DOUBLE:

                if (control instanceof JSpinner) {

                    Number number =
                        (Number) ((JSpinner) control)
                            .getValue();

                    return new JsonValue(
                        number.doubleValue()
                    );
                }

                break;

            case ENUM:

                if (control instanceof JComboBox) {

                    Object selected =
                        ((JComboBox<?>) control)
                            .getSelectedItem();

                    if (
                        selected
                            instanceof PropertyControlFactory.CustomEnumValue
                    ) {

                        PropertyControlFactory.CustomEnumValue custom =
                            (PropertyControlFactory.CustomEnumValue) selected;

                        return new JsonValue(
                            custom.getValue()
                        );
                    }

                    if (selected != null) {

                        return new JsonValue(
                            selected.toString()
                        );
                    }
                }

                break;

            default:
                break;
        }

        return null;
    }

    private boolean isComplexProperty(
        PropertyDefinition property
    ) {

        PropertyType type =
            property.getPropertyType();

        return type == PropertyType.OBJECT
            || type == PropertyType.ARRAY
            || type == PropertyType.COLLECTION
            || type == PropertyType.MAP
            || type == PropertyType.UNKNOWN;
    }

    private String getControlSnapshot(
        JComponent control
    ) {

        if (control instanceof JTextField) {

            return ((JTextField) control)
                .getText();
        }

        if (control instanceof JCheckBox) {

            return Boolean.toString(
                ((JCheckBox) control)
                    .isSelected()
            );
        }

        if (control instanceof JSpinner) {

            Object value =
                ((JSpinner) control)
                    .getValue();

            return value == null
                ? null
                : value.toString();
        }

        if (control instanceof JComboBox) {

            Object value =
                ((JComboBox<?>) control)
                    .getSelectedItem();

            return value == null
                ? null
                : value.toString();
        }

        return null;
    }

    private void refreshSnapshots(
        Map<String, JComponent> controls
    ) {

        for (
            Map.Entry<String, JComponent> entry
            : controls.entrySet()
        ) {

            initialControlValues.put(
                entry.getKey(),
                getControlSnapshot(
                    entry.getValue()
                )
            );
        }
    }

    private JsonValue stringValue(
        String value
    ) {

        if (value == null) {

            return new JsonValue(
                JsonValue.ValueType.nullValue
            );
        }

        return new JsonValue(
            value
        );
    }
}
