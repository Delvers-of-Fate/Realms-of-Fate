package com.realmsoffate.toolkit.ui.creatures;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.data.MonsterDataService;
import com.realmsoffate.toolkit.data.SpellLibraryService;
import com.realmsoffate.toolkit.data.SpellSchema;
import com.realmsoffate.toolkit.data.SpriteSheetService;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.magic.SpellPickerDialog;
import com.realmsoffate.toolkit.ui.sprites.SpritePickerDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonsterEditor extends JPanel {
    public interface Listener { void onSaved(); void onCancel(); }

    private final ModProject project;
    private final MonsterDataService service;
    private final MonsterDataService.MonsterEntry entry;
    private final JsonValue working;

    private final JTextField group = new JTextField();
    private final JTextField name = new JTextField();
    private final JTextField hp = new JTextField();
    private final JTextField mp = new JTextField();
    private final JTextField atk = new JTextField();
    private final JTextField speed = new JTextField();
    private final JTextField reach = new JTextField();
    private final JTextField minRange = new JTextField();
    private final JTextField maxRange = new JTextField();
    private final JTextField projectileAttackTime = new JTextField();
    private final JTextField projectileOffset = new JTextField();
    private final JTextField alertSound = new JTextField();
    private final JTextField attackSound = new JTextField();
    private final JTextField hurtSound = new JTextField();
    private final JTextField dieSound = new JTextField();
    private final JTextField idleSound = new JTextField();
    private final JTextField walkSound = new JTextField();

    private final AnimationFields walkAnimation = new AnimationFields("walkAnimation");
    private final AnimationFields attackAnimation = new AnimationFields("attackAnimation");
    private final AnimationFields hurtAnimation = new AnimationFields("hurtAnimation");
    private final AnimationFields dieAnimation = new AnimationFields("dieAnimation");
    private final AnimationFields castAnimation = new AnimationFields("castAnimation");

    private class AnimationFields {
        final String property;
        final JTextField speed = new JTextField();
        int start = -1;
        int end = -1;
        int actionFrame = -1;
        final JLabel startPreview = animationPreview();
        final JLabel endPreview = animationPreview();
        final JLabel actionPreview = animationPreview();
        final JButton startButton = new JButton("Choose...");
        final JButton endButton = new JButton("Choose...");
        final JButton actionButton = new JButton("Choose...");

        AnimationFields(String property) { this.property = property; }
    }

    private final JCheckBox hostile = new JCheckBox("Hostile");
    private final JCheckBox chase = new JCheckBox("Chase target");
    private final JCheckBox wander = new JCheckBox("Wanders");
    private final JCheckBox ranged = new JCheckBox("Ranged");
    private final JCheckBox doors = new JCheckBox("Can open doors");
    private final JCheckBox attackAnim = new JCheckBox("Has attack animation");
    private final JCheckBox givesExp = new JCheckBox("Awards experience");

    private final JComboBox<String> damage = new JComboBox<String>(new String[]{
        "PHYSICAL", "MAGIC", "FIRE", "ICE", "POISON", "LIGHTNING"
    });

    private final JLabel preview = new JLabel("No sprite", SwingConstants.CENTER);
    private String atlas;
    private int tex;

    private final DefaultListModel<MonsterSpell> spellModel = new DefaultListModel<MonsterSpell>();
    private final JList<MonsterSpell> spellList = new JList<MonsterSpell>(spellModel);
    private final JComboBox<String> spellPattern = new JComboBox<String>(new String[]{
        "SINGLE", "SPREAD", "RING", "CROSS", "RANDOM", "VOLLEY", "BURST", "ALTERNATING", "SPIRAL"
    });
    private final JTextField spellProjectileCount = new JTextField();
    private final JTextField spellProjectileSpread = new JTextField();
    private final JTextField spellProjectileDelay = new JTextField();
    private final JTextField spellPatternAngleStep = new JTextField();
    private boolean loadingSpellPattern = false;

    private static class MonsterSpell {
        final String displayName;
        final JsonValue json;

        MonsterSpell(String displayName, JsonValue json) {
            this.displayName = displayName;
            this.json = json;
        }

        @Override
        public String toString() {
            String className = ItemsDataService.getString(json, "class", "Spell");
            int dot = className.lastIndexOf('.');
            if(dot >= 0) className = className.substring(dot + 1);
            JsonValue costValue = json.get("mpCost");
            String cost = costValue == null ? "?" : costValue.asString();
            return displayName + "  •  " + className + "  •  " + cost + " MP";
        }
    }

    public MonsterEditor(ModProject project, MonsterDataService service,
                         MonsterDataService.MonsterEntry entry, Listener listener) {
        this.project = project;
        this.service = service;
        this.entry = entry;
        this.working = service.editable(entry.getJson());

        setLayout(new BorderLayout());
        setBackground(ToolkitColors.BACKGROUND);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 28, 30, 28));
        form.setBackground(ToolkitColors.BACKGROUND);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        int y = 0;

        y = section(form, c, y, "Monster Identity");
        group.setText(entry.getGroup());
        name.setText(ItemsDataService.getString(working, "name", ""));
        y = row(form, c, y, "Group", group);
        y = row(form, c, y, "Name", name);

        atlas = ItemsDataService.getString(working, "spriteAtlas", "entities");
        tex = ItemsDataService.getInt(working, "tex", 0);
        JPanel sprite = new JPanel(new FlowLayout(FlowLayout.LEFT));
        sprite.setOpaque(false);
        preview.setPreferredSize(new Dimension(70, 70));
        preview.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        JButton choose = new JButton("Choose Sprite...");
        choose.addActionListener(x -> chooseSprite());
        sprite.add(preview);
        sprite.add(choose);
        y = row(form, c, y, "Sprite", sprite);
        refreshPreview();

        y = section(form, c, y, "Stats & Combat");
        hp.setText(String.valueOf(ItemsDataService.getInt(working, "maxHp", 4)));
        mp.setText(String.valueOf(ItemsDataService.getInt(working, "maxMp", 0)));
        atk.setText(String.valueOf(ItemsDataService.getInt(working, "atk", 1)));
        speed.setText(String.valueOf(ItemsDataService.getFloat(working, "speed", .003f)));
        reach.setText(String.valueOf(ItemsDataService.getFloat(working, "reach", .6f)));
        minRange.setText(String.valueOf(ItemsDataService.getFloat(working, "projectileAttackMinDistance", 0)));
        maxRange.setText(String.valueOf(ItemsDataService.getFloat(working, "projectileAttackMaxDistance", 30)));
        projectileAttackTime.setText(String.valueOf(ItemsDataService.getFloat(working, "projectileAttackTime", 1f)));
        projectileOffset.setText(String.valueOf(ItemsDataService.getFloat(working, "projectileOffset", 0f)));
        damage.setSelectedItem(ItemsDataService.getString(working, "damageType", "PHYSICAL"));

        y = row(form, c, y, "Max HP", hp);
        y = row(form, c, y, "Max MP", mp);
        y = row(form, c, y, "Attack", atk);
        y = row(form, c, y, "Damage Type", damage);
        y = row(form, c, y, "Move Speed", speed);
        y = row(form, c, y, "Melee Reach", reach);
        y = row(form, c, y, "Ranged Min Distance", minRange);
        y = row(form, c, y, "Ranged Max Distance", maxRange);
        y = row(form, c, y, "Projectile / Spell Attack Time", projectileAttackTime);
        y = row(form, c, y, "Projectile / Spell Z Offset", projectileOffset);

        y = section(form, c, y, "Behavior");
        hostile.setSelected(ItemsDataService.getBoolean(working, "hostile", true));
        chase.setSelected(ItemsDataService.getBoolean(working, "chasetarget", true));
        wander.setSelected(ItemsDataService.getBoolean(working, "wanders", true));
        ranged.setSelected(ItemsDataService.getBoolean(working, "ranged", false));
        doors.setSelected(ItemsDataService.getBoolean(working, "canOpenDoors", true));
        attackAnim.setSelected(ItemsDataService.getBoolean(working, "hasAttackAnim", true));
        givesExp.setSelected(ItemsDataService.getBoolean(working, "givesExp", true));
        JPanel flags = new JPanel(new GridLayout(0, 2, 8, 4));
        flags.setOpaque(false);
        for(JCheckBox box : new JCheckBox[]{hostile, chase, wander, ranged, doors, attackAnim, givesExp}) {
            flags.add(box);
        }
        y = row(form, c, y, "AI", flags);

        y = section(form, c, y, "Animations");
        loadAnimation(walkAnimation, null);
        loadAnimation(attackAnimation, "com.interrupt.dungeoneer.gfx.animation.DamageAction");
        loadAnimation(hurtAnimation, null);
        loadAnimation(dieAnimation, null);
        loadAnimation(castAnimation, "com.interrupt.dungeoneer.gfx.animation.SpellCastAction");

        y = row(form, c, y, "Walk Start", animationPicker(walkAnimation, 0));
        y = row(form, c, y, "Walk End", animationPicker(walkAnimation, 1));
        y = row(form, c, y, "Walk Speed", walkAnimation.speed);
        y = row(form, c, y, "Attack Start", animationPicker(attackAnimation, 0));
        y = row(form, c, y, "Attack End", animationPicker(attackAnimation, 1));
        y = row(form, c, y, "Attack Speed", attackAnimation.speed);
        y = row(form, c, y, "Damage Frame", animationPicker(attackAnimation, 2));
        y = row(form, c, y, "Hurt Start", animationPicker(hurtAnimation, 0));
        y = row(form, c, y, "Hurt End", animationPicker(hurtAnimation, 1));
        y = row(form, c, y, "Hurt Speed", hurtAnimation.speed);
        y = row(form, c, y, "Die Start", animationPicker(dieAnimation, 0));
        y = row(form, c, y, "Die End", animationPicker(dieAnimation, 1));
        y = row(form, c, y, "Die Speed", dieAnimation.speed);
        y = row(form, c, y, "Cast Start", animationPicker(castAnimation, 0));
        y = row(form, c, y, "Cast End", animationPicker(castAnimation, 1));
        y = row(form, c, y, "Cast Speed", castAnimation.speed);
        y = row(form, c, y, "Spell Cast Frame", animationPicker(castAnimation, 2));

        JLabel animationHelp = new JLabel("<html>Start, end, damage, and spell-cast frames use the monster sprite atlas. Attack writes <b>DamageAction</b>; Cast writes <b>SpellCastAction</b>.</html>");
        animationHelp.setForeground(ToolkitColors.TEXT_SECONDARY);
        y = row(form, c, y, "", animationHelp);

        y = section(form, c, y, "Magic");
        loadEmbeddedSpells();
        spellList.setVisibleRowCount(5);
        spellList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane spellScroll = new JScrollPane(spellList);
        spellScroll.setPreferredSize(new Dimension(400, 125));
        JPanel spellPanel = new JPanel(new BorderLayout(6, 6));
        spellPanel.setOpaque(false);
        spellPanel.add(spellScroll, BorderLayout.CENTER);
        JPanel spellButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        spellButtons.setOpaque(false);
        JButton addSpell = new JButton("+ Add Spell...");
        JButton addRangedTemplate = new JButton("+ Delver Ranged Template");
        JButton removeSpell = new JButton("Remove Selected");
        addSpell.addActionListener(x -> addSpell());
        addRangedTemplate.addActionListener(x -> addDefaultRangedSpell());
        removeSpell.addActionListener(x -> removeSelectedSpell());
        spellButtons.add(addSpell);
        spellButtons.add(Box.createHorizontalStrut(8));
        spellButtons.add(addRangedTemplate);
        spellButtons.add(Box.createHorizontalStrut(8));
        spellButtons.add(removeSpell);
        spellPanel.add(spellButtons, BorderLayout.SOUTH);
        y = row(form, c, y, "Spells", spellPanel);

        spellList.addListSelectionListener(e -> { if(!e.getValueIsAdjusting()) loadSelectedSpellPattern(); });
        spellPattern.addActionListener(e -> applySelectedSpellPattern());
        spellProjectileCount.addActionListener(e -> applySelectedSpellPattern());
        spellProjectileSpread.addActionListener(e -> applySelectedSpellPattern());
        spellProjectileDelay.addActionListener(e -> applySelectedSpellPattern());
        spellPatternAngleStep.addActionListener(e -> applySelectedSpellPattern());
        y = row(form, c, y, "Fire Pattern", spellPattern);
        y = row(form, c, y, "Projectile Count", spellProjectileCount);
        y = row(form, c, y, "Projectile Spread", spellProjectileSpread);
        y = row(form, c, y, "Projectile Delay", spellProjectileDelay);
        y = row(form, c, y, "Pattern Angle Step", spellPatternAngleStep);
        JButton applyPattern = new JButton("Apply Pattern To Selected Spell");
        applyPattern.addActionListener(e -> applySelectedSpellPattern());
        y = row(form, c, y, "", applyPattern);
        setPatternControlsEnabled(false);
        // loadEmbeddedSpells() selects the first spell before the selection listener is attached.
        // Explicitly hydrate the pattern controls now so reopening a monster shows its saved values.
        loadSelectedSpellPattern();

        JLabel magicHelp = new JLabel("<html>Select a spell to configure the same firing patterns used by the Magic editor: Single, Spread, Ring, Cross, Random, Volley, Burst, Alternating, or Spiral.<br>Pattern settings are written directly into that monster's embedded spell JSON.</html>");
        magicHelp.setForeground(ToolkitColors.TEXT_SECONDARY);
        y = row(form, c, y, "", magicHelp);

        y = section(form, c, y, "Sounds");
        alertSound.setText(ItemsDataService.getString(working, "alertSound", ""));
        attackSound.setText(ItemsDataService.getString(working, "attackSound", ""));
        hurtSound.setText(ItemsDataService.getString(working, "hurtSound", ""));
        dieSound.setText(ItemsDataService.getString(working, "dieSound", ""));
        idleSound.setText(ItemsDataService.getString(working, "idleSound", ""));
        walkSound.setText(ItemsDataService.getString(working, "walkSound", ""));
        y = row(form, c, y, "Alert", alertSound);
        y = row(form, c, y, "Attack", attackSound);
        y = row(form, c, y, "Hurt", hurtSound);
        y = row(form, c, y, "Death", dieSound);
        y = row(form, c, y, "Idle", idleSound);
        y = row(form, c, y, "Walk", walkSound);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("Back");
        JButton save = new JButton("Save Monster");
        back.addActionListener(x -> listener.onCancel());
        save.addActionListener(x -> {
            try {
                save();
                listener.onSaved();
            }
            catch(Exception ex) {
                String message = ex.getMessage();
                if(message == null || message.trim().isEmpty()) message = ex.getClass().getSimpleName();
                JOptionPane.showMessageDialog(this, "Could not save monster: " + message, "Monster Save Failed", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        bottom.add(back);
        bottom.add(save);
        add(bottom, BorderLayout.SOUTH);
    }

    private void loadEmbeddedSpells() {
        spellModel.clear();
        JsonValue spells = working.get("spells");
        if(spells == null || !spells.isArray()) return;
        int number = 1;
        for(JsonValue spell = spells.child; spell != null; spell = spell.next) {
            if(!spell.isObject()) continue;
            // Complete legacy/incomplete embedded entries from the matching reusable spell.
            // Existing embedded values (including monster firing-pattern overrides) always win.
            JsonValue loaded;
            try { loaded = SpellSchema.resolveMonsterSpell(project, spell); }
            catch(IOException ex) { loaded = SpellLibraryService.deepCopy(spell); }
            String className = ItemsDataService.getString(loaded, "class", "Spell");
            int dot = className.lastIndexOf('.');
            String shortName = dot >= 0 ? className.substring(dot + 1) : className;
            String savedName = ItemsDataService.getString(loaded, "name", "").trim();
            String displayName = savedName.isEmpty() ? shortName + " " + number : savedName;
            spellModel.addElement(new MonsterSpell(displayName, loaded));
            number++;
        }
        if(!spellModel.isEmpty()) spellList.setSelectedIndex(0);
    }

    private void addSpell() {
        SpellPickerDialog dialog = new SpellPickerDialog(SwingUtilities.getWindowAncestor(this), project);
        dialog.setVisible(true);
        SpellLibraryService.SpellDefinition selected = dialog.getSelection();
        if(selected == null) return;

        JsonValue copy = new SpellLibraryService().copySpell(selected);
        if(copy != null) {
            // A spell selected from the Magic library is already authoritative. Never merge
            // the Delver ranged template into it; doing so can replace user-authored values.
            // In particular, mpCost must remain exactly what the reusable spell defines.
            ItemsDataService.putString(copy, "name", selected.getName());
        }
        if(copy == null) {
            JOptionPane.showMessageDialog(this, "The selected spell has no valid spell data.", "Monster Magic", JOptionPane.ERROR_MESSAGE);
            return;
        }
        spellModel.addElement(new MonsterSpell(selected.getName(), copy));
        spellList.setSelectedIndex(spellModel.size() - 1);
    }


    private void addDefaultRangedSpell() {
        JsonValue spell = MonsterDataService.createDefaultRangedSpell();
        ItemsDataService.putString(spell, "name", "Delver Magic Missile");
        spellModel.addElement(new MonsterSpell("Delver Magic Missile", spell));
        spellList.setSelectedIndex(spellModel.size() - 1);
        ranged.setSelected(true);
    }

    private void removeSelectedSpell() {
        int index = spellList.getSelectedIndex();
        if(index >= 0) spellModel.remove(index);
        loadSelectedSpellPattern();
    }

    private void setPatternControlsEnabled(boolean enabled) {
        spellPattern.setEnabled(enabled);
        spellProjectileCount.setEnabled(enabled);
        spellProjectileSpread.setEnabled(enabled);
        spellProjectileDelay.setEnabled(enabled);
        spellPatternAngleStep.setEnabled(enabled);
    }

    private void loadSelectedSpellPattern() {
        loadingSpellPattern = true;
        try {
            MonsterSpell selected = spellList.getSelectedValue();
            boolean enabled = selected != null;
            setPatternControlsEnabled(enabled);
            if(!enabled) {
                spellPattern.setSelectedItem("SINGLE");
                spellProjectileCount.setText("");
                spellProjectileSpread.setText("");
                spellProjectileDelay.setText("");
                spellPatternAngleStep.setText("");
                return;
            }
            JsonValue json = selected.json;
            spellPattern.setSelectedItem(ItemsDataService.getString(json, "firePattern", "SINGLE"));
            spellProjectileCount.setText(valueText(json, "projectileCount", "1"));
            spellProjectileSpread.setText(valueText(json, "projectileSpread", "0"));
            spellProjectileDelay.setText(valueText(json, "projectileDelay", "0.1"));
            spellPatternAngleStep.setText(valueText(json, "patternAngleStep", "30"));
        } finally { loadingSpellPattern = false; }
    }

    private String valueText(JsonValue json, String key, String fallback) {
        JsonValue value = json.get(key);
        return value == null ? fallback : value.asString();
    }

    private void applySelectedSpellPattern() {
        if(loadingSpellPattern) return;
        MonsterSpell selected = spellList.getSelectedValue();
        if(selected == null) return;
        try {
            ItemsDataService.putString(selected.json, "firePattern", String.valueOf(spellPattern.getSelectedItem()));
            ItemsDataService.putInt(selected.json, "projectileCount", intValue(spellProjectileCount, "Projectile Count"));
            ItemsDataService.putFloat(selected.json, "projectileSpread", floatValue(spellProjectileSpread, "Projectile Spread"));
            ItemsDataService.putFloat(selected.json, "projectileDelay", floatValue(spellProjectileDelay, "Projectile Delay"));
            ItemsDataService.putFloat(selected.json, "patternAngleStep", floatValue(spellPatternAngleStep, "Pattern Angle Step"));
            spellList.repaint();
        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Monster Fire Pattern", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeSpells() {
        if(working.get("spells") != null) ItemsDataService.remove(working, "spells");
        if(spellModel.isEmpty()) return;

        JsonValue array = new JsonValue(JsonValue.ValueType.array);
        array.name = "spells";
        for(int i = 0; i < spellModel.size(); i++) {
            // Lossless round-trip: write the complete embedded spell exactly as held by
            // the editor. Do not hydrate, normalize, rebuild, whitelist, or template it.
            JsonValue copy = SpellLibraryService.deepCopy(spellModel.get(i).json);
            if(copy == null || !copy.isObject()) continue;
            String error = SpellSchema.validationError(copy);
            if(error != null) {
                throw new IllegalStateException("Spell '" + spellModel.get(i).displayName + "' is " + error + ". Re-add it from Magic so the complete spell definition is embedded.");
            }
            copy.name = null;
            JsonTree.append(array, copy);
        }
        JsonTree.put(working, "spells", array);
    }

    private int section(JPanel form, GridBagConstraints c, int y, String title) {
        c.gridx = 0; c.gridy = y; c.gridwidth = 2;
        JLabel label = new JLabel(title);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 17f));
        label.setForeground(ToolkitColors.ACCENT);
        c.insets = new Insets(16, 6, 8, 6);
        form.add(label, c);
        c.gridwidth = 1;
        c.insets = new Insets(5, 6, 5, 6);
        return y + 1;
    }

    private int row(JPanel form, GridBagConstraints c, int y, String labelText, Component component) {
        c.gridy = y; c.gridx = 0; c.weightx = .25;
        JLabel label = new JLabel(labelText);
        label.setForeground(ToolkitColors.TEXT_PRIMARY);
        form.add(label, c);
        c.gridx = 1; c.weightx = .75;
        form.add(component, c);
        return y + 1;
    }

    private static JLabel animationPreview() {
        JLabel label = new JLabel("None", SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(64, 64));
        label.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        return label;
    }

    private JPanel animationPicker(final AnimationFields animation, final int kind) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        panel.setOpaque(false);
        JLabel previewLabel = kind == 0 ? animation.startPreview : kind == 1 ? animation.endPreview : animation.actionPreview;
        JButton button = kind == 0 ? animation.startButton : kind == 1 ? animation.endButton : animation.actionButton;
        button.addActionListener(e -> chooseAnimationSprite(animation, kind));
        panel.add(previewLabel);
        panel.add(button);
        refreshAnimationPreview(animation, kind);
        return panel;
    }

    private void loadAnimation(AnimationFields animation, String actionClass) {
        JsonValue json = working.get(animation.property);
        if(json == null || !json.isObject()) {
            animation.speed.setText("");
            return;
        }
        animation.start = ItemsDataService.getInt(json, "start", -1);
        animation.end = ItemsDataService.getInt(json, "end", -1);
        JsonValue speedValue = json.get("speed");
        animation.speed.setText(speedValue == null ? "" : String.valueOf(speedValue.asFloat()));
        if(actionClass != null) animation.actionFrame = findActionFrame(json, actionClass);
    }

    private int findActionFrame(JsonValue animation, String actionClass) {
        JsonValue actions = animation.get("actions");
        if(actions == null || !actions.isObject()) return -1;
        for(JsonValue frame = actions.child; frame != null; frame = frame.next) {
            if(!frame.isArray()) continue;
            for(JsonValue action = frame.child; action != null; action = action.next) {
                if(action.isObject() && actionClass.equals(ItemsDataService.getString(action, "class", ""))) {
                    try { return Integer.parseInt(frame.name); } catch(Exception ignored) { return -1; }
                }
            }
        }
        return -1;
    }

    private void chooseAnimationSprite(AnimationFields animation, int kind) {
        int current = kind == 0 ? animation.start : kind == 1 ? animation.end : animation.actionFrame;
        SpritePickerDialog dialog = new SpritePickerDialog(SwingUtilities.getWindowAncestor(this), project, atlas, current);
        dialog.setVisible(true);
        SpritePickerDialog.Selection selection = dialog.getSelection();
        if(selection == null) return;
        if(!selection.getAtlas().equals(atlas)) {
            int result = JOptionPane.showConfirmDialog(this,
                    "Animation frames share the monster spriteAtlas. Change this monster's atlas to '" + selection.getAtlas() + "'?",
                    "Change Monster Sprite Atlas", JOptionPane.YES_NO_OPTION);
            if(result != JOptionPane.YES_OPTION) return;
            atlas = selection.getAtlas();
            refreshPreview();
            refreshAllAnimationPreviews();
        }
        if(kind == 0) animation.start = selection.getIndex();
        else if(kind == 1) animation.end = selection.getIndex();
        else animation.actionFrame = selection.getIndex();
        refreshAnimationPreview(animation, kind);
    }

    private void refreshAllAnimationPreviews() {
        for(AnimationFields a : new AnimationFields[]{walkAnimation, attackAnimation, hurtAnimation, dieAnimation, castAnimation}) {
            refreshAnimationPreview(a, 0); refreshAnimationPreview(a, 1); refreshAnimationPreview(a, 2);
        }
    }

    private void refreshAnimationPreview(AnimationFields animation, int kind) {
        int index = kind == 0 ? animation.start : kind == 1 ? animation.end : animation.actionFrame;
        JLabel label = kind == 0 ? animation.startPreview : kind == 1 ? animation.endPreview : animation.actionPreview;
        JButton button = kind == 0 ? animation.startButton : kind == 1 ? animation.endButton : animation.actionButton;
        button.setText(index < 0 ? "Choose..." : "Sprite " + index);
        if(index < 0) { label.setIcon(null); label.setText("None"); return; }
        try {
            SpriteSheetService.SpriteSheet sheet = new SpriteSheetService().findSheet(project, atlas);
            BufferedImage image = sheet == null ? null : sheet.getSprite(index);
            if(image == null) { label.setIcon(null); label.setText(String.valueOf(index)); return; }
            label.setText("");
            label.setIcon(new ImageIcon(SpritePickerDialog.scaleNearest(image, 54, 54)));
        }
        catch(Exception ex) { label.setIcon(null); label.setText(String.valueOf(index)); }
    }

    private void saveAnimation(AnimationFields animation, String actionClass) {
        boolean hasFrames = animation.start >= 0 || animation.end >= 0 || !animation.speed.getText().trim().isEmpty();
        boolean hasAction = actionClass != null && animation.actionFrame >= 0;
        if(!hasFrames && !hasAction) return; // Preserve absent animations as absent.
        if(animation.start < 0 || animation.end < 0 || animation.speed.getText().trim().isEmpty())
            throw new IllegalArgumentException(animation.property + " requires Start, End, and Speed when used.");

        JsonValue existing = working.get(animation.property);
        JsonValue json = existing != null && existing.isObject() ? SpellLibraryService.deepCopy(existing) : new JsonValue(JsonValue.ValueType.object);
        json.name = animation.property;
        ItemsDataService.putInt(json, "start", animation.start);
        ItemsDataService.putInt(json, "end", animation.end);
        ItemsDataService.putFloat(json, "speed", floatValue(animation.speed, animation.property + " Speed"));

        if(actionClass != null) {
            removeActionClass(json, actionClass);
            if(animation.actionFrame >= 0) addAnimationAction(json, animation.actionFrame, actionClass);
        }
        JsonTree.put(working, animation.property, json);
    }

    private void removeActionClass(JsonValue animation, String actionClass) {
        JsonValue oldActions = animation.get("actions");
        if(oldActions == null || !oldActions.isObject()) return;

        JsonValue rebuilt = new JsonValue(JsonValue.ValueType.object);
        rebuilt.name = "actions";
        for(JsonValue frame = oldActions.child; frame != null; frame = frame.next) {
            if(!frame.isArray()) {
                JsonValue copy = SpellLibraryService.deepCopy(frame);
                copy.name = frame.name;
                JsonTree.append(rebuilt, copy);
                continue;
            }
            JsonValue newFrame = new JsonValue(JsonValue.ValueType.array);
            newFrame.name = frame.name;
            for(JsonValue action = frame.child; action != null; action = action.next) {
                if(action.isObject() && actionClass.equals(ItemsDataService.getString(action, "class", ""))) continue;
                JsonValue copy = SpellLibraryService.deepCopy(action);
                copy.name = null;
                JsonTree.append(newFrame, copy);
            }
            if(newFrame.child != null) JsonTree.append(rebuilt, newFrame);
        }
        if(rebuilt.child != null) JsonTree.put(animation, "actions", rebuilt);
        else JsonTree.remove(animation, "actions");
    }

    private void addAnimationAction(JsonValue animation, int frameIndex, String actionClass) {
        JsonValue actions = animation.get("actions");
        if(actions == null || !actions.isObject()) {
            actions = new JsonValue(JsonValue.ValueType.object); JsonTree.put(animation, "actions", actions);
        }
        String frameName = String.valueOf(frameIndex);
        JsonValue frame = actions.get(frameName);
        if(frame == null || !frame.isArray()) {
            frame = new JsonValue(JsonValue.ValueType.array); JsonTree.put(actions, frameName, frame);
        }
        JsonValue action = new JsonValue(JsonValue.ValueType.object);
        ItemsDataService.putString(action, "class", actionClass);
        action.name = null;
        JsonTree.append(frame, action);
    }

    private void chooseSprite() {
        SpritePickerDialog dialog = new SpritePickerDialog(SwingUtilities.getWindowAncestor(this), project, atlas, tex);
        dialog.setVisible(true);
        SpritePickerDialog.Selection selection = dialog.getSelection();
        if(selection != null) {
            atlas = selection.getAtlas();
            tex = selection.getIndex();
            setImage(selection.getImage());
        }
    }

    private void setImage(BufferedImage image) {
        preview.setText("");
        preview.setIcon(image == null ? null : new ImageIcon(SpritePickerDialog.scaleNearest(image, 58, 58)));
    }

    private void refreshPreview() {
        try {
            SpriteSheetService.SpriteSheet sheet = new SpriteSheetService().findSheet(project, atlas);
            if(sheet != null) setImage(sheet.getSprite(tex));
        }
        catch(Exception ignored) { }
    }

    private int intValue(JTextField field, String name) {
        try { return Integer.parseInt(field.getText().trim()); }
        catch(Exception ex) { throw new IllegalArgumentException(name + " must be a whole number."); }
    }

    private float floatValue(JTextField field, String name) {
        try { return Float.parseFloat(field.getText().trim()); }
        catch(Exception ex) { throw new IllegalArgumentException(name + " must be a number."); }
    }

    private void saveString(String property, JTextField field) {
        String value = field.getText().trim();
        if(value.isEmpty()) ItemsDataService.remove(working, property);
        else ItemsDataService.putString(working, property, value);
    }

    private void save() throws IOException {
        if(name.getText().trim().isEmpty()) throw new IllegalArgumentException("Enter a monster name.");

        ItemsDataService.putString(working, "class", MonsterDataService.MONSTER_CLASS);
        ItemsDataService.putString(working, "name", name.getText().trim());
        ItemsDataService.putString(working, "spriteAtlas", atlas);
        ItemsDataService.putInt(working, "tex", tex);

        int maxHp = intValue(hp, "Max HP");
        int maxMp = intValue(mp, "Max MP");
        ItemsDataService.putInt(working, "maxHp", maxHp);
        ItemsDataService.putInt(working, "hp", maxHp);
        ItemsDataService.putInt(working, "maxMp", maxMp);
        ItemsDataService.putInt(working, "mp", maxMp);
        ItemsDataService.putInt(working, "atk", intValue(atk, "Attack"));
        ItemsDataService.putFloat(working, "speed", floatValue(speed, "Move Speed"));
        ItemsDataService.putFloat(working, "reach", floatValue(reach, "Melee Reach"));
        ItemsDataService.putFloat(working, "projectileAttackMinDistance", floatValue(minRange, "Ranged Min Distance"));
        ItemsDataService.putFloat(working, "projectileAttackMaxDistance", floatValue(maxRange, "Ranged Max Distance"));
        ItemsDataService.putFloat(working, "projectileAttackTime", floatValue(projectileAttackTime, "Projectile / Spell Attack Time"));
        ItemsDataService.putFloat(working, "projectileOffset", floatValue(projectileOffset, "Projectile / Spell Z Offset"));
        ItemsDataService.putString(working, "damageType", String.valueOf(damage.getSelectedItem()));

        ItemsDataService.putBoolean(working, "hostile", hostile.isSelected());
        ItemsDataService.putBoolean(working, "chasetarget", chase.isSelected());
        ItemsDataService.putBoolean(working, "wanders", wander.isSelected());
        ItemsDataService.putBoolean(working, "ranged", ranged.isSelected());
        ItemsDataService.putBoolean(working, "canOpenDoors", doors.isSelected());
        ItemsDataService.putBoolean(working, "hasAttackAnim", attackAnim.isSelected());
        ItemsDataService.putBoolean(working, "givesExp", givesExp.isSelected());

        saveAnimation(walkAnimation, null);
        saveAnimation(attackAnimation, "com.interrupt.dungeoneer.gfx.animation.DamageAction");
        saveAnimation(hurtAnimation, null);
        saveAnimation(dieAnimation, null);
        saveAnimation(castAnimation, "com.interrupt.dungeoneer.gfx.animation.SpellCastAction");

        applySelectedSpellPattern();
        // The embedded monster spell is the authoritative snapshot selected by the user.
        // Never refresh it from the reusable Magic library during Save Monster: doing so can
        // replace edited values (for example mpCost) with stale/default library values.
        writeSpells();

        saveString("alertSound", alertSound);
        saveString("attackSound", attackSound);
        saveString("hurtSound", hurtSound);
        saveString("dieSound", dieSound);
        saveString("idleSound", idleSound);
        saveString("walkSound", walkSound);

        service.save(project, entry, group.getText(), working);
    }
}
