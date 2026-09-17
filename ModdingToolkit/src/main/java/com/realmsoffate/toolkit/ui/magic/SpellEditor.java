package com.realmsoffate.toolkit.ui.magic;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.data.SpellLibraryService;
import com.realmsoffate.toolkit.data.SpriteSheetService;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.sprites.SpritePickerDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit editor for the Realms of Fate MagicMissile spell data. Unknown JSON is preserved. */
public class SpellEditor extends JPanel {
    public interface Listener { void onSaved(); void onCancel(); }

    private static final String PROJECTILE_CLASS = "com.interrupt.dungeoneer.entities.projectiles.MagicMissileProjectile";

    private final ModProject project;
    private final SpellLibraryService service;
    private final SpellLibraryService.SpellDefinition original;
    private final Listener listener;
    private final JsonValue spell;

    private final JTextField libraryName = new JTextField();
    private final JTextField className = new JTextField();
    private final Map<String,JTextField> fields = new LinkedHashMap<String,JTextField>();
    private final Map<String,JCheckBox> checks = new LinkedHashMap<String,JCheckBox>();
    private final Map<String,JComboBox<String>> combos = new LinkedHashMap<String,JComboBox<String>>();
    private final Map<String,JTextField> projectileFields = new LinkedHashMap<String,JTextField>();
    private final Map<String,JCheckBox> projectileChecks = new LinkedHashMap<String,JCheckBox>();
    private final Map<String,JComboBox<String>> projectileCombos = new LinkedHashMap<String,JComboBox<String>>();

    // Hidden backing values for visual sprite pickers. Modders never need to type atlas/index values.
    private final JTextField projectileAtlas = new JTextField();
    private final JTextField projectileTex = new JTextField();
    private final JTextField projectileEndTex = new JTextField();
    private final JTextField trailParticleTex = new JTextField();
    private final JLabel projectilePreview = new JLabel("No sprite", SwingConstants.CENTER);
    private final JLabel projectileEndPreview = new JLabel("None", SwingConstants.CENTER);
    private final JLabel trailParticlePreview = new JLabel("No sprite", SwingConstants.CENTER);

    public SpellEditor(ModProject p, SpellLibraryService s, SpellLibraryService.SpellDefinition d, Listener l) {
        project=p; service=s; original=d; listener=l;
        SpellLibraryService.SpellDefinition copy=s.editableCopy(d); spell=copy.getSpell();

        setLayout(new BorderLayout()); setOpaque(false); setBorder(BorderFactory.createEmptyBorder(24,32,24,32));
        JLabel title=new JLabel(d.getFile()==null?"Create Spell":"Edit Spell");
        title.setForeground(ToolkitColors.TEXT_PRIMARY); title.setFont(title.getFont().deriveFont(Font.BOLD,28f));

        JPanel form=new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints gc=new GridBagConstraints(); gc.insets=new Insets(5,6,5,6); gc.fill=GridBagConstraints.HORIZONTAL; gc.anchor=GridBagConstraints.WEST;
        int r=0;
        r=section(form,gc,r,"Definition");
        r=field(form,gc,r,"Library Name",libraryName);
        r=field(form,gc,r,"Spell Class",className);

        r=section(form,gc,r,"Base Spell");
        r=num(form,gc,r,"Mana Cost","mpCost");
        r=num(form,gc,r,"Base Damage","baseDamage");
        r=num(form,gc,r,"Random Damage","randDamage");
        r=combo(form,gc,r,"Damage Type","damageType",new String[]{"MAGIC","PHYSICAL","FIRE","ICE","POISON","LIGHTNING"});
        r=num(form,gc,r,"Minimum Target Distance","minDistanceToTarget");
        r=num(form,gc,r,"Maximum Target Distance","maxDistanceToTarget");
        r=text(form,gc,r,"Cast Sound","castSound");
        r=num(form,gc,r,"Cast Sound Volume","castSoundVolume");
        r=bool(form,gc,r,"Create Cast VFX","doCastVfx");

        r=section(form,gc,r,"Charging");
        r=bool(form,gc,r,"Can Charge","canCharge");
        r=num(form,gc,r,"Max Charge Time","maxChargeTime");
        r=num(form,gc,r,"Charged Mana Cost (-1 = normal)","chargedMpCost");
        r=num(form,gc,r,"Charged Damage Multiplier","chargedDamageMultiplier");

        r=section(form,gc,r,"Magic Missile");
        r=text(form,gc,r,"Hit Sound","hitSound");
        r=num(form,gc,r,"Projectile Speed","speed");
        r=num(form,gc,r,"Trail Interval","trailInterval");
        r=num(form,gc,r,"Splash Force","splashForce");
        r=num(form,gc,r,"Splash Radius","splashRadius");
        r=bool(form,gc,r,"Splash Damage","splashDamage");
        r=bool(form,gc,r,"Floating","floating");
        r=num(form,gc,r,"Shot Accuracy","shotAccuracy");

        r=section(form,gc,r,"Fire Pattern");
        String[] patterns={"SINGLE","SPREAD","RING","CROSS","RANDOM","VOLLEY","BURST","ALTERNATING","SPIRAL"};
        r=combo(form,gc,r,"Normal Pattern","firePattern",patterns);
        r=comboWithBlank(form,gc,r,"Charged Pattern (blank = normal)","chargedFirePattern",patterns);
        r=num(form,gc,r,"Projectile Count","projectileCount");
        r=num(form,gc,r,"Charged Projectile Count","chargedProjectileCount");
        r=num(form,gc,r,"Projectile Spread","projectileSpread");
        r=num(form,gc,r,"Charged Projectile Spread","chargedProjectileSpread");
        r=num(form,gc,r,"Projectile Delay","projectileDelay");
        r=num(form,gc,r,"Charged Projectile Delay (-1 = normal)","chargedProjectileDelay");
        r=num(form,gc,r,"Pattern Angle Step","patternAngleStep");
        r=num(form,gc,r,"Charged Angle Step (-1 = normal)","chargedPatternAngleStep");

        r=section(form,gc,r,"Projectile");
        r=pCombo(form,gc,r,"Movement","movement",new String[]{"STRAIGHT","HOMING","WAVE","ACCELERATE","DECELERATE"});
        r=pNum(form,gc,r,"Homing Range","homingRange");
        r=pNum(form,gc,r,"Homing Strength","homingStrength");
        r=pBool(form,gc,r,"Homing Can Retarget","homingCanRetarget");
        r=pNum(form,gc,r,"Wave Strength","waveStrength");
        r=pNum(form,gc,r,"Wave Speed","waveSpeed");
        r=pNum(form,gc,r,"Movement Acceleration","movementAcceleration");
        r=pNum(form,gc,r,"Minimum Movement Speed","minimumMovementSpeed");
        r=pNum(form,gc,r,"Maximum Movement Speed","maximumMovementSpeed");

        r=section(form,gc,r,"Projectile Visual & Trail");
        projectileFields.put("spriteAtlas", projectileAtlas);
        projectileFields.put("tex", projectileTex);
        projectileFields.put("endAnimTex", projectileEndTex);
        projectileFields.put("trailParticleTex", trailParticleTex);
        r=projectileSpriteField(form,gc,r,"Projectile Sprite",projectilePreview,projectileTex,SpriteKind.PROJECTILE,false);
        r=projectileSpriteField(form,gc,r,"End Animation Sprite",projectileEndPreview,projectileEndTex,SpriteKind.END_ANIMATION,true);
        r=pNum(form,gc,r,"Animation Speed","animSpeed");
        r=pBool(form,gc,r,"Leave Trail","leaveTrail");
        r=projectileSpriteField(form,gc,r,"Trail Particle Sprite",trailParticlePreview,trailParticleTex,SpriteKind.TRAIL,false);
        r=pNum(form,gc,r,"Trail Particle Lifetime","trailParticleLifetime");
        r=pNum(form,gc,r,"Trail Random Lifetime","trailParticleRandomLifetime");
        r=pNum(form,gc,r,"Trail Start Scale","trailParticleStartScale");
        r=pNum(form,gc,r,"Trail End Scale","trailParticleEndScale");
        r=pNum(form,gc,r,"Projectile Splash Force","splashForce");
        r=pNum(form,gc,r,"Projectile Splash Radius","splashRadius");
        r=pBool(form,gc,r,"Projectile Splash Damage","splashDamage");
        r=pNum(form,gc,r,"Particle Amount Modifier","particleAmoundMod");
        r=pNum(form,gc,r,"Projectile Trail Interval","trailInterval");
        r=pNum(form,gc,r,"Light Modifier","lightMod");
        r=pCombo(form,gc,r,"Halo Mode","haloMode",new String[]{"BOTH","CORONA_ONLY","NONE"});
        r=pBool(form,gc,r,"Floating Projectile","floating");
        r=pBool(form,gc,r,"Fullbrite Projectile","fullbrite");
        r=pNum(form,gc,r,"Knockback","knockback");
        r=pNum(form,gc,r,"Scale","scale");

        r=section(form,gc,r,"Spell Color");
        r=colorField(form,gc,r,"Red","r"); r=colorField(form,gc,r,"Green","g"); r=colorField(form,gc,r,"Blue","b");

        JTextArea note=new JTextArea("Explosion, attached entities, castVfx and status-effect objects are preserved if already present in the spell JSON. Dedicated visual editors for those nested entity types can be added without changing this spell-library format.");
        note.setEditable(false); note.setLineWrap(true); note.setWrapStyleWord(true); note.setOpaque(false); note.setForeground(ToolkitColors.TEXT_SECONDARY);
        gc.gridx=0;gc.gridy=r;gc.gridwidth=2;form.add(note,gc);gc.gridwidth=1;

        load();
        JScrollPane scroll=new JScrollPane(form); scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false); scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT)); buttons.setOpaque(false);
        if(d.getFile()!=null){JButton del=new JButton("Delete");del.addActionListener(e->delete());buttons.add(del);}
        JButton back=new JButton("Back"), save=new JButton("Save Spell"); back.addActionListener(e->listener.onCancel()); save.addActionListener(e->save()); buttons.add(back);buttons.add(save);
        add(title,BorderLayout.NORTH); add(scroll,BorderLayout.CENTER); add(buttons,BorderLayout.SOUTH);
    }

    private void load(){
        libraryName.setText(original.getName()); className.setText(ItemsDataService.getString(spell,"class",SpellLibraryService.MAGIC_MISSILE_CLASS));
        for(Map.Entry<String,JTextField> e:fields.entrySet()) if(!e.getKey().startsWith("spellColor.")) e.getValue().setText(valueText(spell,e.getKey(),defaultValue(e.getKey())));
        for(Map.Entry<String,JCheckBox> e:checks.entrySet()) e.getValue().setSelected(ItemsDataService.getBoolean(spell,e.getKey(),defaultBool(e.getKey())));
        for(Map.Entry<String,JComboBox<String>> e:combos.entrySet()) {String v=ItemsDataService.getString(spell,e.getKey(),defaultValue(e.getKey())); e.getValue().setSelectedItem(v);}
        JsonValue p=spell.get("magicMissileProjectile");
        if(p!=null&&p.isObject()){
            for(Map.Entry<String,JTextField> e:projectileFields.entrySet()) e.getValue().setText(valueText(p,e.getKey(),projectileDefault(e.getKey())));
            for(Map.Entry<String,JCheckBox> e:projectileChecks.entrySet()) e.getValue().setSelected(ItemsDataService.getBoolean(p,e.getKey(),projectileDefaultBool(e.getKey())));
            for(Map.Entry<String,JComboBox<String>> e:projectileCombos.entrySet()) e.getValue().setSelectedItem(ItemsDataService.getString(p,e.getKey(),projectileDefault(e.getKey())));
        } else {
            for(Map.Entry<String,JTextField> e:projectileFields.entrySet()) e.getValue().setText(projectileDefault(e.getKey()));
            for(Map.Entry<String,JCheckBox> e:projectileChecks.entrySet()) e.getValue().setSelected(projectileDefaultBool(e.getKey()));
            for(Map.Entry<String,JComboBox<String>> e:projectileCombos.entrySet()) e.getValue().setSelectedItem(projectileDefault(e.getKey()));
        }
        JsonValue color=spell.get("spellColor");
        if(color!=null&&color.isObject()) { setColor("r",color);setColor("g",color);setColor("b",color); }
        refreshProjectileSpritePreviews();
    }

    private void save(){
        try{
            String cls=className.getText().trim(); if(cls.isEmpty())throw new Exception("Spell Class is required.");
            ItemsDataService.putString(spell,"class",cls);
            for(Map.Entry<String,JTextField> e:fields.entrySet()) if(!e.getKey().startsWith("spellColor.")) saveValue(spell,e.getKey(),e.getValue().getText());
            for(Map.Entry<String,JCheckBox> e:checks.entrySet()) ItemsDataService.putBoolean(spell,e.getKey(),e.getValue().isSelected());
            for(Map.Entry<String,JComboBox<String>> e:combos.entrySet()) {Object v=e.getValue().getSelectedItem(); if(v==null||String.valueOf(v).trim().isEmpty())ItemsDataService.remove(spell,e.getKey());else ItemsDataService.putString(spell,e.getKey(),String.valueOf(v));}

            JsonValue p=spell.get("magicMissileProjectile");
            if(p==null||!p.isObject()){p=new JsonValue(JsonValue.ValueType.object);JsonTree.put(spell,"magicMissileProjectile",p);}
            ItemsDataService.putString(p,"class",PROJECTILE_CLASS);
            for(Map.Entry<String,JTextField> e:projectileFields.entrySet()) saveValue(p,e.getKey(),e.getValue().getText());
            for(Map.Entry<String,JCheckBox> e:projectileChecks.entrySet()) ItemsDataService.putBoolean(p,e.getKey(),e.getValue().isSelected());
            for(Map.Entry<String,JComboBox<String>> e:projectileCombos.entrySet()) ItemsDataService.putString(p,e.getKey(),String.valueOf(e.getValue().getSelectedItem()));

            JsonValue color=spell.get("spellColor"); if(color==null||!color.isObject()){color=new JsonValue(JsonValue.ValueType.object);JsonTree.put(spell,"spellColor",color);}
            saveColor(color,"r");saveColor(color,"g");saveColor(color,"b");

            service.save(project,original,libraryName.getText(),spell); listener.onSaved();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Save Spell",JOptionPane.ERROR_MESSAGE);}
    }

    private void saveValue(JsonValue root,String key,String raw){String v=raw.trim();if(v.isEmpty()){ItemsDataService.remove(root,key);return;}if(v.matches("-?\\d+"))ItemsDataService.putInt(root,key,Integer.parseInt(v));else if(v.matches("-?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][+-]?\\d+)?"))ItemsDataService.putFloat(root,key,Float.parseFloat(v));else ItemsDataService.putString(root,key,v);}
    private void saveColor(JsonValue color,String key){JTextField f=fields.get("spellColor."+key);if(f!=null)ItemsDataService.putFloat(color,key,Float.parseFloat(f.getText().trim()));}
    private void setColor(String key,JsonValue color){JTextField f=fields.get("spellColor."+key);if(f!=null)f.setText(String.valueOf(ItemsDataService.getFloat(color,key,1f)));}
    private String valueText(JsonValue root,String key,String fallback){JsonValue v=root==null?null:root.get(key);return v==null||v.isNull()?fallback:v.asString();}

    private String defaultValue(String k){
        if(k.equals("mpCost")||k.equals("baseDamage")||k.equals("randDamage")||k.equals("projectileCount")||k.equals("chargedProjectileCount"))return"1";
        if(k.equals("damageType"))return"MAGIC"; if(k.equals("castSoundVolume"))return"0.5"; if(k.equals("minDistanceToTarget"))return"0"; if(k.equals("maxDistanceToTarget"))return"30";
        if(k.equals("maxChargeTime"))return"1.5";if(k.equals("chargedMpCost")||k.equals("chargedProjectileDelay")||k.equals("chargedPatternAngleStep"))return"-1";if(k.equals("chargedDamageMultiplier"))return"1";
        if(k.equals("speed"))return"0.17";if(k.equals("trailInterval"))return"1";if(k.equals("splashForce"))return"0.1";if(k.equals("splashRadius"))return"3";if(k.equals("shotAccuracy"))return"1";
        if(k.equals("firePattern"))return"SINGLE";if(k.equals("projectileSpread")||k.equals("chargedProjectileSpread"))return"0";if(k.equals("projectileDelay"))return"0.1";if(k.equals("patternAngleStep"))return"30";return"";
    }
    private boolean defaultBool(String k){return k.equals("doCastVfx")||k.equals("floating");}
    private String projectileDefault(String k){if(k.equals("movement"))return"STRAIGHT";if(k.equals("homingRange"))return"8";if(k.equals("homingStrength"))return"0.05";if(k.equals("waveStrength"))return"0.04";if(k.equals("waveSpeed"))return"0.15";if(k.equals("movementAcceleration"))return"0.002";if(k.equals("minimumMovementSpeed"))return"0.02";if(k.equals("maximumMovementSpeed"))return"1.0";if(k.equals("trailParticleLifetime")||k.equals("trailParticleRandomLifetime"))return"60";if(k.equals("trailParticleStartScale")||k.equals("particleAmoundMod")||k.equals("lightMod")||k.equals("scale"))return"1";if(k.equals("trailParticleEndScale"))return"0";if(k.equals("trailInterval"))return"0.1";if(k.equals("splashForce"))return"0.1";if(k.equals("splashRadius"))return"3";if(k.equals("animSpeed"))return"30";if(k.equals("haloMode"))return"BOTH";if(k.equals("spriteAtlas"))return"particle";return"";}
    private boolean projectileDefaultBool(String k){return k.equals("homingCanRetarget")||k.equals("leaveTrail")||k.equals("floating")||k.equals("fullbrite");}

    private enum SpriteKind { PROJECTILE, END_ANIMATION, TRAIL }

    private int projectileSpriteField(JPanel p, GridBagConstraints gc, int r, String label, JLabel preview, JTextField value, SpriteKind kind, boolean optional) {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        box.setOpaque(false);
        preview.setPreferredSize(new Dimension(68, 68));
        preview.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));
        JButton choose = new JButton("Choose Sprite...");
        choose.addActionListener(e -> chooseProjectileSprite(value, kind));
        box.add(preview);
        box.add(choose);
        if(optional) {
            JButton clear = new JButton("Clear");
            clear.addActionListener(e -> { value.setText(""); refreshProjectileSpritePreviews(); });
            box.add(clear);
        }
        gc.gridx=0; gc.gridy=r; gc.weightx=0; p.add(new JLabel(label),gc);
        gc.gridx=1; gc.weightx=1; p.add(box,gc);
        return r+1;
    }

    private void chooseProjectileSprite(JTextField value, SpriteKind kind) {
        try {
            String atlasName = kind == SpriteKind.TRAIL ? "particle" : projectileAtlas.getText().trim();
            if(atlasName.isEmpty()) atlasName = "particle";
            int current = value.getText().trim().isEmpty() ? -1 : Integer.parseInt(value.getText().trim());
            Window owner = SwingUtilities.getWindowAncestor(this);
            SpritePickerDialog dialog = new SpritePickerDialog(owner, project, atlasName, current);
            dialog.setVisible(true);
            SpritePickerDialog.Selection selection = dialog.getSelection();
            if(selection == null) return;

            if(kind == SpriteKind.PROJECTILE) {
                // tex and endAnimTex share one spriteAtlas in Delver. Changing the projectile atlas
                // therefore clears the optional end animation if it came from another sheet.
                String oldAtlas = projectileAtlas.getText().trim();
                if(!oldAtlas.isEmpty() && !oldAtlas.equals(selection.getAtlas())) projectileEndTex.setText("");
                projectileAtlas.setText(selection.getAtlas());
                projectileTex.setText(String.valueOf(selection.getIndex()));
            }
            else if(kind == SpriteKind.END_ANIMATION) {
                String projectileAtlasName = projectileAtlas.getText().trim();
                if(projectileAtlasName.isEmpty()) projectileAtlasName = "particle";
                if(!projectileAtlasName.equals(selection.getAtlas())) {
                    JOptionPane.showMessageDialog(this,
                        "End Animation Sprite must use the same atlas as the Projectile Sprite (" + projectileAtlasName + ").",
                        "Sprite Atlas", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                projectileEndTex.setText(String.valueOf(selection.getIndex()));
            }
            else {
                if(!"particle".equals(selection.getAtlas())) {
                    JOptionPane.showMessageDialog(this,
                        "Trail particles use Delver's particle atlas. Choose a sprite from the particle sheet.",
                        "Trail Particle Sprite", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                trailParticleTex.setText(String.valueOf(selection.getIndex()));
            }
            refreshProjectileSpritePreviews();
        }
        catch(Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Sprite", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshProjectileSpritePreviews() {
        setProjectilePreview(projectilePreview, projectileAtlas.getText().trim(), projectileTex.getText().trim(), "No sprite");
        setProjectilePreview(projectileEndPreview, projectileAtlas.getText().trim(), projectileEndTex.getText().trim(), "None");
        setProjectilePreview(trailParticlePreview, "particle", trailParticleTex.getText().trim(), "No sprite");
    }

    private void setProjectilePreview(JLabel label, String atlasName, String indexText, String emptyText) {
        try {
            if(indexText == null || indexText.trim().isEmpty()) {
                label.setIcon(null); label.setText(emptyText); label.setToolTipText(null); return;
            }
            if(atlasName == null || atlasName.trim().isEmpty()) atlasName = "particle";
            SpriteSheetService.SpriteSheet sheet = new SpriteSheetService().findSheet(project, atlasName);
            BufferedImage image = sheet == null ? null : sheet.getSprite(Integer.parseInt(indexText.trim()));
            if(image == null) {
                label.setIcon(null); label.setText("Unavailable"); label.setToolTipText(null); return;
            }
            label.setText("");
            label.setIcon(new ImageIcon(SpritePickerDialog.scaleNearest(image,58,58)));
            label.setToolTipText(atlasName + "  #" + indexText.trim());
        }
        catch(Exception ex) {
            label.setIcon(null); label.setText("Unavailable"); label.setToolTipText(null);
        }
    }

    private void delete(){int ok=JOptionPane.showConfirmDialog(this,"Delete this reusable spell? Existing Wands keep their embedded copy.","Delete Spell",JOptionPane.YES_NO_OPTION);if(ok!=JOptionPane.YES_OPTION)return;try{service.delete(original);listener.onSaved();}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Delete Spell",JOptionPane.ERROR_MESSAGE);}}

    private int section(JPanel p,GridBagConstraints gc,int r,String s){JLabel l=new JLabel(s);l.setForeground(ToolkitColors.ACCENT);l.setFont(l.getFont().deriveFont(Font.BOLD,17f));l.setBorder(BorderFactory.createEmptyBorder(r==0?0:14,0,4,0));gc.gridx=0;gc.gridy=r;gc.gridwidth=2;gc.weightx=1;p.add(l,gc);gc.gridwidth=1;return r+1;}
    private int field(JPanel p,GridBagConstraints gc,int r,String label,JTextField x){gc.gridx=0;gc.gridy=r;gc.weightx=0;p.add(new JLabel(label),gc);x.setPreferredSize(new Dimension(360,30));gc.gridx=1;gc.weightx=1;p.add(x,gc);return r+1;}
    private int text(JPanel p,GridBagConstraints gc,int r,String label,String key){JTextField x=new JTextField();fields.put(key,x);return field(p,gc,r,label,x);}
    private int num(JPanel p,GridBagConstraints gc,int r,String label,String key){return text(p,gc,r,label,key);}
    private int bool(JPanel p,GridBagConstraints gc,int r,String label,String key){JCheckBox x=new JCheckBox(label);x.setOpaque(false);checks.put(key,x);gc.gridx=1;gc.gridy=r;p.add(x,gc);return r+1;}
    private int combo(JPanel p,GridBagConstraints gc,int r,String label,String key,String[] values){JComboBox<String>x=new JComboBox<String>(values);combos.put(key,x);gc.gridx=0;gc.gridy=r;p.add(new JLabel(label),gc);gc.gridx=1;p.add(x,gc);return r+1;}
    private int comboWithBlank(JPanel p,GridBagConstraints gc,int r,String label,String key,String[] values){String[] all=new String[values.length+1];all[0]="";System.arraycopy(values,0,all,1,values.length);return combo(p,gc,r,label,key,all);}
    private int pText(JPanel p,GridBagConstraints gc,int r,String label,String key){JTextField x=new JTextField();projectileFields.put(key,x);return field(p,gc,r,label,x);}
    private int pNum(JPanel p,GridBagConstraints gc,int r,String label,String key){return pText(p,gc,r,label,key);}
    private int pBool(JPanel p,GridBagConstraints gc,int r,String label,String key){JCheckBox x=new JCheckBox(label);x.setOpaque(false);projectileChecks.put(key,x);gc.gridx=1;gc.gridy=r;p.add(x,gc);return r+1;}
    private int pCombo(JPanel p,GridBagConstraints gc,int r,String label,String key,String[] values){JComboBox<String>x=new JComboBox<String>(values);projectileCombos.put(key,x);gc.gridx=0;gc.gridy=r;p.add(new JLabel(label),gc);gc.gridx=1;p.add(x,gc);return r+1;}
    private int colorField(JPanel p,GridBagConstraints gc,int r,String label,String key){JTextField x=new JTextField("1");fields.put("spellColor."+key,x);return field(p,gc,r,label,x);}
}
