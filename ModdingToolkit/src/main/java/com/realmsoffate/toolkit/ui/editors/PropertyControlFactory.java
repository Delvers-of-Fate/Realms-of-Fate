package com.realmsoffate.toolkit.ui.editors;

import com.realmsoffate.toolkit.content.metadata.PropertyDefinition;
import com.realmsoffate.toolkit.content.metadata.PropertyType;

import javax.swing.*;
import java.awt.*;

public final class PropertyControlFactory {

    private PropertyControlFactory() {
    }

    public static JComponent createControl(
        PropertyDefinition property
    ) {

        if (property == null) {

            throw new IllegalArgumentException(
                "Property cannot be null."
            );
        }

        PropertyType type =
            property.getPropertyType();

        switch (type) {

            case STRING:
                return createStringControl();

            case BOOLEAN:
                return createBooleanControl();

            case INTEGER:
                return createIntegerControl();

            case LONG:
                return createLongControl();

            case FLOAT:
                return createFloatControl();

            case DOUBLE:
                return createDoubleControl();

            case ENUM:
                return createEnumControl(
                    property
                );

            case ARRAY:
                return createComplexControl(
                    "Edit Array..."
                );

            case COLLECTION:
                return createComplexControl(
                    "Edit Collection..."
                );

            case MAP:
                return createComplexControl(
                    "Edit Map..."
                );

            case OBJECT:
                return createComplexControl(
                    "Edit Object..."
                );

            default:
                return createComplexControl(
                    "Edit..."
                );
        }
    }

    private static JTextField createStringControl() {

        JTextField field =
            new JTextField();

        field.setPreferredSize(
            new Dimension(
                220,
                30
            )
        );

        return field;
    }

    private static JCheckBox createBooleanControl() {

        JCheckBox checkBox =
            new JCheckBox();

        checkBox.setOpaque(false);

        return checkBox;
    }

    private static JSpinner createIntegerControl() {

        SpinnerNumberModel model =
            new SpinnerNumberModel(
                0,
                Integer.MIN_VALUE,
                Integer.MAX_VALUE,
                1
            );

        return new JSpinner(
            model
        );
    }

    private static JSpinner createLongControl() {

        SpinnerNumberModel model =
            new SpinnerNumberModel(
                Long.valueOf(0L),
                Long.valueOf(Long.MIN_VALUE),
                Long.valueOf(Long.MAX_VALUE),
                Long.valueOf(1L)
            );

        return new JSpinner(
            model
        );
    }

    private static JSpinner createFloatControl() {

        SpinnerNumberModel model =
            new SpinnerNumberModel(
                Double.valueOf(0.0),
                Double.valueOf(-1000000.0),
                Double.valueOf(1000000.0),
                Double.valueOf(0.1)
            );

        return new JSpinner(
            model
        );
    }

    private static JSpinner createDoubleControl() {

        SpinnerNumberModel model =
            new SpinnerNumberModel(
                Double.valueOf(0.0),
                Double.valueOf(-1000000.0),
                Double.valueOf(1000000.0),
                Double.valueOf(0.1)
            );

        return new JSpinner(
            model
        );
    }

    private static JComboBox<Object> createEnumControl(
        PropertyDefinition property
    ) {

        DefaultComboBoxModel<Object> model =
            new DefaultComboBoxModel<Object>();

        Object[] values =
            property.getEnumValues();

        for (Object value : values) {

            model.addElement(
                value
            );
        }

        JComboBox<Object> comboBox =
            new JComboBox<Object>(
                model
            );

        comboBox.setRenderer(
            new DefaultListCellRenderer() {

                @Override
                public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
                ) {

                    JLabel label =
                        (JLabel) super.getListCellRendererComponent(
                            list,
                            value,
                            index,
                            isSelected,
                            cellHasFocus
                        );

                    if (
                        value instanceof CustomEnumValue
                    ) {

                        CustomEnumValue customValue =
                            (CustomEnumValue) value;

                        label.setText(
                            customValue.getValue()
                                + " (custom)"
                        );
                    }

                    return label;
                }
            }
        );

        return comboBox;
    }

    private static JButton createComplexControl(
        String text
    ) {

        JButton button =
            new JButton(
                text
            );

        button.setEnabled(false);

        button.setToolTipText(
            "A specialized editor for this property type will be added later."
        );

        return button;
    }

    public static class CustomEnumValue {

        private final String value;

        public CustomEnumValue(
            String value
        ) {

            this.value =
                value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
