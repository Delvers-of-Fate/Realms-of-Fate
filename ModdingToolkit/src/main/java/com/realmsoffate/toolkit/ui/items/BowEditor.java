package com.realmsoffate.toolkit.ui.items;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.data.SpriteSheetService;
import com.realmsoffate.toolkit.ui.sprites.SpritePickerDialog;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class BowEditor extends JPanel {
    public interface Listener { void onSaved(); void onCancel(); }

    private static final String[] DAMAGE_TYPES = {
            "PHYSICAL", "MAGIC", "FIRE", "ICE", "LIGHTNING", "POISON", "HEALING", "PARALYZE", "VAMPIRE"
    };
    private static final String[] CONDITIONS = { "broken", "worn", "normal", "fine", "excellent" };

    private final ModProject project;
    private final ItemsDataService service;
    private final ItemsDataService.BowEntry entry;
    private final Listener listener;
    private final JsonValue working;

    private final JTextField name = new JTextField();
    private final JTextArea description = new JTextArea(3, 20);
    private final JTextField tier = new JTextField();
    private final JTextField cost = new JTextField();
    private final JTextField itemLevel = new JTextField();
    private final JTextField minItemLevel = new JTextField();
    private final JTextField maxItemLevel = new JTextField();
    private final JComboBox<String> condition = new JComboBox<String>(CONDITIONS);
    private final JCheckBox identified = new JCheckBox("Identified");
    private final JCheckBox unique = new JCheckBox("Unique");
    private final JCheckBox canSpawnEnchanted = new JCheckBox("Can spawn enchanted");
    private final JCheckBox randomizeCondition = new JCheckBox("Randomize condition when spawned");

    private final JTextField baseDamage = new JTextField();
    private final JTextField randDamage = new JTextField();
    private final JComboBox<String> damageType = new JComboBox<String>(DAMAGE_TYPES);
    private final JTextField knockback = new JTextField();
    private final JTextField speed = new JTextField();
    private final JTextField chargeSpeed = new JTextField();
    private final JTextField durability = new JTextField();
    private final JCheckBox chargesAttack = new JCheckBox("Allows charged attacks");
    private final JCheckBox twoHanded = new JCheckBox("Two handed");

    private final JTextField attackAnimation = new JTextField();
    private final JTextField attackStrongAnimation = new JTextField();
    private final JTextField chargeAnimation = new JTextField();

    private final JTextField atlas = new JTextField();
    private final JTextField tex = new JTextField();
    private final JTextField heldTex = new JTextField();
    private final JTextField inventoryTex = new JTextField();
    private final JTextField brokenTex = new JTextField();
    private final JLabel worldPreview = new JLabel("No sprite", JLabel.CENTER);
    private final JLabel heldPreview = new JLabel("Default", JLabel.CENTER);
    private final JLabel inventoryPreview = new JLabel("Default", JLabel.CENTER);
    private final JLabel brokenPreview = new JLabel("None", JLabel.CENTER);
    private final JTextField equipLoc = new JTextField();

    private final JTextField range = new JTextField();
    private final JTextField fireSound = new JTextField();
    private final JTextField equipSound = new JTextField();
    private final JTextField pickupSound = new JTextField();
    private final JTextField dropSound = new JTextField();

    private final JTextField triggersOnPickup = new JTextField();
    private final JTextField meshFile = new JTextField();
    private final JTextField viewMeshFile = new JTextField();
    private final JTextField textureFile = new JTextField();

    public BowEditor(ModProject project, ItemsDataService service, ItemsDataService.BowEntry entry, Listener listener) {
        this.project = project;
        this.service = service;
        this.entry = entry;
        this.listener = listener;
        this.working = service.createEditableCopy(entry.getJson());

        setLayout(new BorderLayout());
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(26, 34, 26, 34));

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Bow");
        title.setForeground(ToolkitColors.TEXT_PRIMARY);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitle = new JLabel("Practical Delver Bow, Weapon and Item properties");
        subtitle.setForeground(ToolkitColors.TEXT_SECONDARY);
        titles.add(title);
        titles.add(Box.createVerticalStrut(4));
        titles.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(18, 0, 22, 0));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        int row = 0;
        row = addSection(form, c, row, "General");
        row = addField(form, c, row, "Name", name, "Display name shown in game.");
        row = addArea(form, c, row, "Description", description, "Item description.");
        row = addField(form, c, row, "Tier", tier, "The ranged tier inside items.dat.");
        row = addField(form, c, row, "Cost", cost, "Amount of gold the item is worth.");
        row = addField(form, c, row, "Item Level", itemLevel, "Base item level used by Delver scaling.");
        row = addField(form, c, row, "Min Item Level", minItemLevel, "Optional minimum spawn level. Leave blank for none.");
        row = addField(form, c, row, "Max Item Level", maxItemLevel, "Optional maximum spawn level. Leave blank for none.");
        row = addCombo(form, c, row, "Condition", condition, "Starting item condition.");
        row = addCheck(form, c, row, identified);
        row = addCheck(form, c, row, unique);
        row = addCheck(form, c, row, canSpawnEnchanted);
        row = addCheck(form, c, row, randomizeCondition);

        row = addSection(form, c, row, "Combat");
        row = addField(form, c, row, "Base Damage", baseDamage, "Base amount of damage dealt.");
        row = addField(form, c, row, "Random Damage", randDamage, "Additional random damage range.");
        row = addCombo(form, c, row, "Damage Type", damageType, "Damage element/type used by the weapon.");
        row = addField(form, c, row, "Range", range, "Distance a fully charged bow shot will travel.");
        row = addField(form, c, row, "Knockback", knockback, "Strength of knockback on hit.");
        row = addField(form, c, row, "Attack Speed", speed, "Length/speed factor for the attack animation.");
        row = addField(form, c, row, "Charge Speed", chargeSpeed, "Charge speed used for charged attacks.");
        row = addField(form, c, row, "Durability", durability, "Uses before condition degradation is checked.");
        row = addCheck(form, c, row, chargesAttack);
        row = addCheck(form, c, row, twoHanded);

        row = addSection(form, c, row, "Animations");
        row = addField(form, c, row, "Attack Animation", attackAnimation, "Animation used for a standard attack.");
        row = addField(form, c, row, "Strong Attack Animation", attackStrongAnimation, "Animation used for a charged/strong attack.");
        row = addField(form, c, row, "Charge Animation", chargeAnimation, "Animation used while charging.");

        row = addSection(form, c, row, "Visual");
        atlas.setEditable(false);
        tex.setEditable(false);
        heldTex.setEditable(false);
        inventoryTex.setEditable(false);
        brokenTex.setEditable(false);
        row = addField(form, c, row, "Sprite Atlas", atlas, "Selected automatically by the sprite picker.");
        row = addSpriteField(form, c, row, "World Sprite", worldPreview, tex, false, "Main world sprite (tex).");
        row = addSpriteField(form, c, row, "Held Sprite", heldPreview, heldTex, true, "Optional held sprite. Clear uses Delver's default.");
        row = addSpriteField(form, c, row, "Inventory Sprite", inventoryPreview, inventoryTex, true, "Optional inventory sprite. Clear uses Delver's default.");
        row = addSpriteField(form, c, row, "Broken Sprite", brokenPreview, brokenTex, true, "Optional broken sprite. Clear writes -1.");
        row = addField(form, c, row, "Equip Location", equipLoc, "Equipment slot/location string used by Delver.");

        row = addSection(form, c, row, "Sounds");
        row = addField(form, c, row, "Fire Sound", fireSound, "Sound played when the bow fires. Comma-separated sound choices are supported by Delver.");
        row = addField(form, c, row, "Equip Sound", equipSound, "Sound played when equipped.");
        row = addField(form, c, row, "Pickup Sound", pickupSound, "Sound played when picked up.");
        row = addField(form, c, row, "Drop Sound", dropSound, "Sound played when the item hits the floor.");

        row = addSection(form, c, row, "Advanced");
        row = addField(form, c, row, "Pickup Trigger", triggersOnPickup, "Optional entity id to trigger when picked up.");
        row = addField(form, c, row, "World Mesh", meshFile, "Optional mesh file used for the world item.");
        row = addField(form, c, row, "View Mesh", viewMeshFile, "Optional first-person/view mesh.");
        addField(form, c, row, "Mesh Texture", textureFile, "Optional texture used by item meshes.");

        loadValues();

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        JButton cancel = new JButton("Back");
        JButton save = new JButton("Save Bow");
        cancel.addActionListener(e -> listener.onCancel());
        save.addActionListener(e -> save());
        buttons.add(cancel);
        buttons.add(save);

        add(titles, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
    }

    private int addSection(JPanel form, GridBagConstraints c, int row, String text) {
        JLabel label = new JLabel(text);
        label.setForeground(ToolkitColors.ACCENT);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
        label.setBorder(BorderFactory.createEmptyBorder(row == 0 ? 0 : 14, 0, 4, 0));
        c.gridx = 0; c.gridy = row; c.gridwidth = 2; c.weightx = 1;
        form.add(label, c);
        c.gridwidth = 1;
        return row + 1;
    }

    private int addField(JPanel form, GridBagConstraints c, int row, String label, JTextField field, String help) {
        JLabel l = new JLabel(label);
        l.setForeground(ToolkitColors.TEXT_PRIMARY);
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(l, c);
        field.setPreferredSize(new Dimension(360, 32));
        field.setToolTipText(help);
        c.gridx = 1; c.weightx = 1;
        form.add(field, c);
        return row + 1;
    }

    private int addArea(JPanel form, GridBagConstraints c, int row, String label, JTextArea area, String help) {
        JLabel l = new JLabel(label);
        l.setForeground(ToolkitColors.TEXT_PRIMARY);
        c.gridx = 0; c.gridy = row; c.weightx = 0; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(l, c);
        area.setLineWrap(true); area.setWrapStyleWord(true); area.setToolTipText(help);
        JScrollPane pane = new JScrollPane(area); pane.setPreferredSize(new Dimension(360, 72));
        c.gridx = 1; c.weightx = 1;
        form.add(pane, c);
        c.anchor = GridBagConstraints.WEST;
        return row + 1;
    }

    private int addCombo(JPanel form, GridBagConstraints c, int row, String label, JComboBox<String> combo, String help) {
        JLabel l = new JLabel(label);
        l.setForeground(ToolkitColors.TEXT_PRIMARY);
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(l, c);
        combo.setToolTipText(help);
        c.gridx = 1; c.weightx = 1;
        form.add(combo, c);
        return row + 1;
    }

    private int addCheck(JPanel form, GridBagConstraints c, int row, JCheckBox check) {
        check.setOpaque(false);
        check.setForeground(ToolkitColors.TEXT_PRIMARY);
        c.gridx = 1; c.gridy = row; c.weightx = 1;
        form.add(check, c);
        return row + 1;
    }

    private void loadValues() {
        name.setText(ItemsDataService.getString(working, "name", "New Bow"));
        description.setText(ItemsDataService.getString(working, "description", ""));
        tier.setText(entry.getTier());
        cost.setText(String.valueOf(ItemsDataService.getInt(working, "cost", 20)));
        itemLevel.setText(String.valueOf(ItemsDataService.getInt(working, "itemLevel", 1)));
        minItemLevel.setText(ItemsDataService.getOptionalIntText(working, "minItemLevel"));
        maxItemLevel.setText(ItemsDataService.getOptionalIntText(working, "maxItemLevel"));
        condition.setSelectedItem(ItemsDataService.getString(working, "itemCondition", "normal"));
        identified.setSelected(ItemsDataService.getBoolean(working, "identified", true));
        unique.setSelected(ItemsDataService.getBoolean(working, "unique", false));
        canSpawnEnchanted.setSelected(ItemsDataService.getBoolean(working, "canSpawnEnchanted", true));
        randomizeCondition.setSelected(ItemsDataService.getBoolean(working, "randomizeCondition", true));

        baseDamage.setText(String.valueOf(ItemsDataService.getInt(working, "baseDamage", 2)));
        randDamage.setText(String.valueOf(ItemsDataService.getInt(working, "randDamage", 2)));
        damageType.setSelectedItem(ItemsDataService.getString(working, "damageType", "PHYSICAL"));
        range.setText(String.valueOf(ItemsDataService.getInt(working, "range", 4)));
        knockback.setText(String.valueOf(ItemsDataService.getFloat(working, "knockback", 0.8f)));
        speed.setText(String.valueOf(ItemsDataService.getFloat(working, "speed", 0.5f)));
        chargeSpeed.setText(String.valueOf(ItemsDataService.getFloat(working, "chargespeed", 1f)));
        durability.setText(String.valueOf(ItemsDataService.getInt(working, "durability", 25)));
        chargesAttack.setSelected(ItemsDataService.getBoolean(working, "chargesAttack", true));
        twoHanded.setSelected(ItemsDataService.getBoolean(working, "twoHanded", false));

        attackAnimation.setText(ItemsDataService.getString(working, "attackAnimation", "bowAttack"));
        attackStrongAnimation.setText(ItemsDataService.getString(working, "attackStrongAnimation", ""));
        chargeAnimation.setText(ItemsDataService.getString(working, "chargeAnimation", "bowCharge"));

        atlas.setText(ItemsDataService.getString(working, "spriteAtlas", "item"));
        tex.setText(String.valueOf(ItemsDataService.getInt(working, "tex", 0)));
        heldTex.setText(ItemsDataService.getOptionalIntText(working, "heldTex"));
        inventoryTex.setText(ItemsDataService.getOptionalIntText(working, "inventoryTex"));
        brokenTex.setText(String.valueOf(ItemsDataService.getInt(working, "brokenTex", -1)));
        equipLoc.setText(ItemsDataService.getString(working, "equipLoc", ""));

        fireSound.setText(ItemsDataService.getString(working, "fireSound", "bow.mp3,bow_02.mp3,bow_03.mp3,bow_04.mp3"));
        equipSound.setText(ItemsDataService.getString(working, "equipSound", "/ui/ui_equip_item.mp3"));
        pickupSound.setText(ItemsDataService.getString(working, "pickupSound", "pu_gen.mp3"));
        dropSound.setText(ItemsDataService.getString(working, "dropSound", "drops/drop_soft.mp3"));

        triggersOnPickup.setText(ItemsDataService.getString(working, "triggersOnPickup", ""));
        meshFile.setText(ItemsDataService.getString(working, "meshFile", ""));
        viewMeshFile.setText(ItemsDataService.getString(working, "viewMeshFile", ""));
        textureFile.setText(ItemsDataService.getString(working, "textureFile", ""));
        refreshSpritePreviews();
    }

    private int addSpriteField(JPanel form, GridBagConstraints c, int row, String label, JLabel preview, JTextField indexField, boolean allowClear, String help) {
        JLabel l = new JLabel(label);
        l.setForeground(ToolkitColors.TEXT_PRIMARY);
        c.gridx = 0; c.gridy = row; c.weightx = 0;
        form.add(l, c);

        preview.setPreferredSize(new Dimension(72, 72));
        preview.setMinimumSize(new Dimension(72, 72));
        preview.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        preview.setForeground(ToolkitColors.TEXT_SECONDARY);

        indexField.setPreferredSize(new Dimension(70, 30));
        indexField.setToolTipText(help);

        JButton choose = new JButton("Choose Sprite...");
        choose.addActionListener(e -> chooseSprite(indexField));

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        controls.add(preview);
        controls.add(new JLabel("#"));
        controls.add(indexField);
        controls.add(choose);

        if (allowClear) {
            JButton clear = new JButton("Clear");
            clear.addActionListener(e -> {
                if (indexField == brokenTex) indexField.setText("-1");
                else indexField.setText("");
                refreshSpritePreviews();
            });
            controls.add(clear);
        }

        c.gridx = 1; c.weightx = 1;
        form.add(controls, c);
        return row + 1;
    }

    private void chooseSprite(JTextField target) {
        int current = parseOptionalIndex(target, -1);
        Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        SpritePickerDialog dialog = new SpritePickerDialog(owner, project, atlas.getText().trim(), current);
        dialog.setVisible(true);
        SpritePickerDialog.Selection selected = dialog.getSelection();
        if (selected == null) return;
        atlas.setText(selected.getAtlas());
        target.setText(String.valueOf(selected.getIndex()));
        refreshSpritePreviews();
    }

    private int parseOptionalIndex(JTextField field, int fallback) {
        try {
            String value = field.getText() == null ? "" : field.getText().trim();
            return value.isEmpty() ? fallback : Integer.parseInt(value);
        }
        catch (Exception ignored) { return fallback; }
    }

    private void refreshSpritePreviews() {
        String atlasName = atlas.getText() == null ? "" : atlas.getText().trim();
        try {
            SpriteSheetService.SpriteSheet sheet = new SpriteSheetService().findSheet(project, atlasName);
            setPreview(worldPreview, sheet, parseOptionalIndex(tex, -1), "No sprite");
            setPreview(heldPreview, sheet, parseOptionalIndex(heldTex, -1), "Default");
            setPreview(inventoryPreview, sheet, parseOptionalIndex(inventoryTex, -1), "Default");
            setPreview(brokenPreview, sheet, parseOptionalIndex(brokenTex, -1), "None");
        }
        catch (IOException ex) {
            setPreviewUnavailable(worldPreview);
            setPreviewUnavailable(heldPreview);
            setPreviewUnavailable(inventoryPreview);
            setPreviewUnavailable(brokenPreview);
        }
    }

    private void setPreview(JLabel label, SpriteSheetService.SpriteSheet sheet, int index, String emptyText) throws IOException {
        label.setIcon(null);
        label.setText(emptyText);
        if (sheet == null || index < 0 || index >= sheet.getSpriteCount()) return;
        BufferedImage image = sheet.getSprite(index);
        Image scaled = SpritePickerDialog.scaleNearest(image, 58, 58);
        label.setText("");
        label.setIcon(new ImageIcon(scaled));
        label.setToolTipText(sheet.getName() + " #" + index);
    }

    private void setPreviewUnavailable(JLabel label) {
        label.setIcon(null);
        label.setText("Unavailable");
        label.setToolTipText(null);
    }

    private void save() {
        try {
            String itemName = name.getText().trim();
            if (itemName.isEmpty()) throw new IllegalArgumentException("Enter a bow name.");
            String tierValue = tier.getText().trim();

            ItemsDataService.putString(working, "class", ItemsDataService.BOW_CLASS);
            ItemsDataService.putString(working, "itemType", "bow");
            ItemsDataService.putString(working, "name", itemName);
            ItemsDataService.putString(working, "description", description.getText());
            ItemsDataService.putInt(working, "cost", parseInt(cost, "Cost"));
            ItemsDataService.putInt(working, "itemLevel", parseInt(itemLevel, "Item Level"));
            putOptionalInt("minItemLevel", minItemLevel, "Min Item Level");
            putOptionalInt("maxItemLevel", maxItemLevel, "Max Item Level");
            ItemsDataService.putString(working, "itemCondition", String.valueOf(condition.getSelectedItem()));
            ItemsDataService.putBoolean(working, "identified", identified.isSelected());
            ItemsDataService.putBoolean(working, "unique", unique.isSelected());
            ItemsDataService.putBoolean(working, "canSpawnEnchanted", canSpawnEnchanted.isSelected());
            ItemsDataService.putBoolean(working, "randomizeCondition", randomizeCondition.isSelected());

            ItemsDataService.putInt(working, "baseDamage", parseInt(baseDamage, "Base Damage"));
            ItemsDataService.putInt(working, "randDamage", parseInt(randDamage, "Random Damage"));
            ItemsDataService.putString(working, "damageType", String.valueOf(damageType.getSelectedItem()));
            ItemsDataService.putInt(working, "range", parseInt(range, "Range"));
            ItemsDataService.putFloat(working, "knockback", parseFloat(knockback, "Knockback"));
            ItemsDataService.putFloat(working, "speed", parseFloat(speed, "Attack Speed"));
            ItemsDataService.putFloat(working, "chargespeed", parseFloat(chargeSpeed, "Charge Speed"));
            ItemsDataService.putInt(working, "durability", parseInt(durability, "Durability"));
            ItemsDataService.putBoolean(working, "chargesAttack", chargesAttack.isSelected());
            ItemsDataService.putBoolean(working, "twoHanded", twoHanded.isSelected());

            putOptionalString("attackAnimation", attackAnimation);
            putOptionalString("attackStrongAnimation", attackStrongAnimation);
            putOptionalString("chargeAnimation", chargeAnimation);

            putOptionalString("spriteAtlas", atlas);
            ItemsDataService.putInt(working, "tex", parseInt(tex, "World Sprite"));
            putOptionalInt("heldTex", heldTex, "Held Sprite");
            putOptionalInt("inventoryTex", inventoryTex, "Inventory Sprite");
            ItemsDataService.putInt(working, "brokenTex", parseInt(brokenTex, "Broken Sprite"));
            putOptionalString("equipLoc", equipLoc);

            putOptionalString("fireSound", fireSound);
            putOptionalString("equipSound", equipSound);
            putOptionalString("pickupSound", pickupSound);
            putOptionalString("dropSound", dropSound);

            putOptionalString("triggersOnPickup", triggersOnPickup);
            putOptionalString("meshFile", meshFile);
            putOptionalString("viewMeshFile", viewMeshFile);
            putOptionalString("textureFile", textureFile);

            service.saveBow(project, entry, tierValue, working);
            listener.onSaved();
        }
        catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Bow", JOptionPane.WARNING_MESSAGE);
        }
        catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Could Not Save Bow", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void putOptionalString(String property, JTextField field) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) ItemsDataService.remove(working, property);
        else ItemsDataService.putString(working, property, value);
    }

    private void putOptionalInt(String property, JTextField field, String label) {
        String value = field.getText() == null ? "" : field.getText().trim();
        if (value.isEmpty()) ItemsDataService.remove(working, property);
        else ItemsDataService.putInt(working, property, parseInt(field, label));
    }

    private int parseInt(JTextField field, String label) {
        try { return Integer.parseInt(field.getText().trim()); }
        catch (Exception ex) { throw new IllegalArgumentException(label + " must be a whole number."); }
    }

    private float parseFloat(JTextField field, String label) {
        try { return Float.parseFloat(field.getText().trim()); }
        catch (Exception ex) { throw new IllegalArgumentException(label + " must be a number."); }
    }
}
