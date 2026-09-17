package com.realmsoffate.toolkit.ui.items;

import com.badlogic.gdx.utils.JsonValue;
import com.realmsoffate.toolkit.data.ItemsDataService;
import com.realmsoffate.toolkit.data.SpriteSheetService;
import com.realmsoffate.toolkit.data.SpellLibraryService;
import com.realmsoffate.toolkit.json.JsonTree;
import com.realmsoffate.toolkit.project.ModProject;
import com.realmsoffate.toolkit.ui.ToolkitColors;
import com.realmsoffate.toolkit.ui.sprites.SpritePickerDialog;
import com.realmsoffate.toolkit.ui.magic.SpellPickerDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

/** Lightweight explicit editor used by the remaining item families. Only fields for the selected type are added. */
public class ItemTypeEditor extends JPanel {
    public interface Listener { void onSaved(); void onCancel(); }
    public enum Type { GUN, WAND, ARMOR, POTION, SCROLL, FOOD, DECORATION, JUNK, UNIQUE }

    private final ModProject project; private final ItemsDataService service; private final ItemsDataService.ItemEntry entry; private final Listener listener; private final Type type; private final JsonValue working;
    private final JTextField name=new JTextField(), tier=new JTextField(), cost=new JTextField(), itemLevel=new JTextField(), atlas=new JTextField(), tex=new JTextField(), heldTex=new JTextField(), inventoryTex=new JTextField(), brokenTex=new JTextField(), equipLoc=new JTextField(), pickupSound=new JTextField(), equipSound=new JTextField(), triggers=new JTextField();
    private final JTextField wandBaseDamage=new JTextField(), wandRandDamage=new JTextField(), wandKnockback=new JTextField(), wandSpeed=new JTextField(), wandChargeSpeed=new JTextField(), wandDurability=new JTextField(), wandAttackAnimation=new JTextField(), wandStrongAttackAnimation=new JTextField(), wandChargeAnimation=new JTextField(), wandDropSound=new JTextField(), wandMinLevel=new JTextField(), wandMaxLevel=new JTextField();
    private final JComboBox<String> wandDamageType=new JComboBox<String>(new String[]{"PHYSICAL","MAGIC","FIRE","ICE","LIGHTNING","POISON"});
    private final JCheckBox wandChargesAttack=new JCheckBox("Weapon attack can charge"), wandTwoHanded=new JCheckBox("Two handed");
    private final JLabel heldPreview=new JLabel("Default",SwingConstants.CENTER), inventoryPreview=new JLabel("Default",SwingConstants.CENTER), brokenPreview=new JLabel("None",SwingConstants.CENTER);
    private final JTextArea description=new JTextArea(3,20); private final JCheckBox identified=new JCheckBox("Identified"), unique=new JCheckBox("Unique"), enchanted=new JCheckBox("Can spawn enchanted"), randomCondition=new JCheckBox("Randomize condition when spawned");
    private final JLabel preview=new JLabel("No sprite",SwingConstants.CENTER);
    private final JLabel selectedSpell=new JLabel("No spell selected");
    private JsonValue embeddedSpell = null;
    private final JTextField a=new JTextField(),b=new JTextField(),c=new JTextField(),d=new JTextField(),e=new JTextField(),f=new JTextField(),g=new JTextField(),h=new JTextField(),i=new JTextField(),j=new JTextField(),k=new JTextField();
    private final JCheckBox flag1=new JCheckBox(),flag2=new JCheckBox(),flag3=new JCheckBox(); private final JComboBox<String> choice=new JComboBox<String>();

    public ItemTypeEditor(ModProject project, ItemsDataService service, ItemsDataService.ItemEntry entry, Type type, Listener listener){
        this.project=project;this.service=service;this.entry=entry;this.type=type;this.listener=listener;this.working=service.createEditableCopy(entry.getJson());
        setLayout(new BorderLayout());setOpaque(false);setBorder(BorderFactory.createEmptyBorder(26,34,26,34));
        JLabel title=new JLabel(title()); title.setForeground(ToolkitColors.TEXT_PRIMARY); title.setFont(title.getFont().deriveFont(Font.BOLD,28f));
        JPanel form=new JPanel(new GridBagLayout());form.setOpaque(false);GridBagConstraints gc=new GridBagConstraints();gc.insets=new Insets(5,6,5,6);gc.fill=GridBagConstraints.HORIZONTAL;gc.anchor=GridBagConstraints.WEST;int r=0;
        r=section(form,gc,r,"General");r=field(form,gc,r,"Name",name);r=area(form,gc,r,"Description",description);if(isTiered())r=field(form,gc,r,"Tier",tier);r=field(form,gc,r,"Cost",cost);r=field(form,gc,r,"Item Level",itemLevel);r=check(form,gc,r,identified);r=check(form,gc,r,unique);r=check(form,gc,r,enchanted);r=check(form,gc,r,randomCondition);
        r=typeFields(form,gc,r);
        if(type==Type.WAND) r=wandWeaponFields(form,gc,r);
        r=section(form,gc,r,"Visual");atlas.setEditable(false);tex.setEditable(false);r=field(form,gc,r,"Sprite Atlas",atlas);r=sprite(form,gc,r);heldTex.setEditable(false);inventoryTex.setEditable(false);brokenTex.setEditable(false);r=spriteField(form,gc,r,"Held Sprite",heldPreview,heldTex,true);r=spriteField(form,gc,r,"Inventory Sprite",inventoryPreview,inventoryTex,true);if(type==Type.WAND)r=spriteField(form,gc,r,"Broken Sprite",brokenPreview,brokenTex,true);r=field(form,gc,r,"Equip Location",equipLoc);
        r=section(form,gc,r,"Sounds & Advanced");r=field(form,gc,r,"Equip Sound",equipSound);r=field(form,gc,r,"Pickup Sound",pickupSound);r=field(form,gc,r,"Pickup Trigger",triggers);
        load(); JScrollPane scroll=new JScrollPane(form);scroll.setBorder(null);scroll.setOpaque(false);scroll.getViewport().setOpaque(false);scroll.getVerticalScrollBar().setUnitIncrement(18);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));buttons.setOpaque(false);JButton back=new JButton("Back"),save=new JButton("Save "+title());back.addActionListener(x->listener.onCancel());save.addActionListener(x->save());buttons.add(back);buttons.add(save);
        add(title,BorderLayout.NORTH);add(scroll,BorderLayout.CENTER);add(buttons,BorderLayout.SOUTH);
    }
    private int typeFields(JPanel p,GridBagConstraints gc,int r){r=section(p,gc,r,type==Type.GUN?"Gun":type==Type.WAND?"Magic":type==Type.ARMOR?"Armor":type==Type.POTION?"Potion":type==Type.SCROLL?"Scroll":type==Type.FOOD?"Food":"Item");
        switch(type){
            case GUN: a.setName("baseDamage");b.setName("randDamage");c.setName("cycleTime");d.setName("ammoType");e.setName("ammoPerShot");f.setName("projectileNum");g.setName("projectileSpreadX");h.setName("projectileSpreadY");i.setName("hitParticles");j.setName("fireSound");k.setName("outOfAmmoSound");flag1.setText("Automatic fire");flag1.setName("automatic");r=field(p,gc,r,"Base Damage",a);r=field(p,gc,r,"Random Damage",b);r=field(p,gc,r,"Cycle Time",c);r=field(p,gc,r,"Ammo Type",d);r=field(p,gc,r,"Ammo Per Shot",e);r=field(p,gc,r,"Projectiles Per Shot",f);r=field(p,gc,r,"Horizontal Spread",g);r=field(p,gc,r,"Vertical Spread",h);r=field(p,gc,r,"Hit Particles",i);r=field(p,gc,r,"Fire Sound",j);r=field(p,gc,r,"Out Of Ammo Sound",k);r=check(p,gc,r,flag1);break;
            case WAND: a.setName("charges");b.setName("autoFireTime");c.setName("magicStatBoostMod");flag1.setText("Uses Charges");flag1.setName("usesCharges");flag2.setText("Uses Mana");flag2.setName("usesMana");flag3.setText("Auto Fire");flag3.setName("autoFire");r=field(p,gc,r,"Charges",a);r=check(p,gc,r,flag1);r=check(p,gc,r,flag2);r=check(p,gc,r,flag3);r=field(p,gc,r,"Auto Fire Time",b);r=field(p,gc,r,"Magic Stat Boost",c);r=spellChooser(p,gc,r);break;
            case ARMOR:a.setName("armor");b.setName("durability");c.setName("set");r=field(p,gc,r,"Armor (AC)",a);r=field(p,gc,r,"Durability",b);r=field(p,gc,r,"Armor Set",c);break;
            case POTION:choice.setModel(new DefaultComboBoxModel<String>(new String[]{"health","magic","maxhealth","poison","restore","shield","paralyze"}));choice.setName("potionType");r=combo(p,gc,r,"Potion Type",choice);break;
            case SCROLL:r=spellChooser(p,gc,r);break;
            case FOOD:choice.setModel(new DefaultComboBoxModel<String>(new String[]{"food","booze"}));choice.setName("foodType");a.setName("foodMod");b.setName("consumeSound");c.setName("infoText");r=combo(p,gc,r,"Food Type",choice);r=field(p,gc,r,"Food Modifier",a);r=field(p,gc,r,"Consume Sound",b);r=field(p,gc,r,"Info Text",c);break;
            default:a.setName("itemType");r=field(p,gc,r,"Item Type",a);break;
        }return r;}
    private void load(){name.setText(ItemsDataService.getString(working,"name","New "+title()));description.setText(ItemsDataService.getString(working,"description",""));tier.setText(entry.getTier()==null?"1":entry.getTier());cost.setText(""+ItemsDataService.getInt(working,"cost",20));itemLevel.setText(""+ItemsDataService.getInt(working,"itemLevel",1));identified.setSelected(ItemsDataService.getBoolean(working,"identified",true));unique.setSelected(ItemsDataService.getBoolean(working,"unique",type==Type.UNIQUE));enchanted.setSelected(ItemsDataService.getBoolean(working,"canSpawnEnchanted",true));randomCondition.setSelected(ItemsDataService.getBoolean(working,"randomizeCondition",true));atlas.setText(ItemsDataService.getString(working,"spriteAtlas","item"));tex.setText(""+ItemsDataService.getInt(working,"tex",0));heldTex.setText(ItemsDataService.getOptionalIntText(working,"heldTex"));inventoryTex.setText(ItemsDataService.getOptionalIntText(working,"inventoryTex"));brokenTex.setText(ItemsDataService.getOptionalIntText(working,"brokenTex"));equipLoc.setText(ItemsDataService.getString(working,"equipLoc",""));equipSound.setText(ItemsDataService.getString(working,"equipSound",""));pickupSound.setText(ItemsDataService.getString(working,"pickupSound","pu_gen.mp3"));triggers.setText(ItemsDataService.getString(working,"triggersOnPickup",""));
        for(JTextField x:new JTextField[]{a,b,c,d,e,f,g,h,i,j,k}) if(x.getName()!=null){if(x.getName().equals("spell.class"))x.setText(spellClass());else x.setText(ItemsDataService.getString(working,x.getName(),defaultFor(x.getName())));} if(flag1.getName()!=null)flag1.setSelected(ItemsDataService.getBoolean(working,flag1.getName(),type==Type.GUN));if(flag3.getName()!=null)flag3.setSelected(ItemsDataService.getBoolean(working,flag3.getName(),false));if(flag2.getName()!=null)flag2.setSelected(ItemsDataService.getBoolean(working,flag2.getName(),false));if(choice.getName()!=null)choice.setSelectedItem(ItemsDataService.getString(working,choice.getName(),choice.getItemAt(0)));refreshSelectedSpell();loadWandWeaponFields();refreshPreview();}
    private String defaultFor(String n){if(n.equals("baseDamage")||n.equals("randDamage"))return"2";if(n.equals("cycleTime"))return"6";if(n.equals("ammoType"))return"BULLET";if(n.equals("ammoPerShot")||n.equals("projectileNum"))return"1";if(n.equals("hitParticles"))return"7";if(n.equals("fireSound"))return"explode.mp3";if(n.equals("outOfAmmoSound"))return"button.mp3";if(n.equals("charges"))return"0";if(n.equals("autoFireTime"))return"0.1";if(n.equals("magicStatBoostMod")||n.equals("foodMod"))return"1.0";if(n.equals("armor"))return"1";if(n.equals("durability"))return"25";return"";}
    private String spellClass(){JsonValue s=working.get("spell");return s==null?"":ItemsDataService.getString(s,"class","");}
    private void save(){
        try{
            ensureTypeIdentity();ensureEmbeddedSpell();
            ItemsDataService.putString(working,"name",name.getText().trim());
            optionalStringFromUi("description",description.getText(),"");
            optionalIntFromUi("cost",cost,20);optionalIntFromUi("itemLevel",itemLevel,1);
            optionalBooleanFromUi("identified",identified,true);optionalBooleanFromUi("unique",unique,type==Type.UNIQUE);
            optionalBooleanFromUi("canSpawnEnchanted",enchanted,true);optionalBooleanFromUi("randomizeCondition",randomCondition,true);
            optionalStringFromUi("spriteAtlas",atlas.getText(),"item");optionalIntFromUi("tex",tex,0);
            optionalInt("heldTex",heldTex);optionalInt("inventoryTex",inventoryTex);if(type==Type.WAND)optionalInt("brokenTex",brokenTex);
            optionalStringFromUi("equipLoc",equipLoc.getText(),"");optionalStringFromUi("equipSound",equipSound.getText(),"");
            optionalStringFromUi("pickupSound",pickupSound.getText(),"pu_gen.mp3");optionalStringFromUi("triggersOnPickup",triggers.getText(),"");
            saveType();saveWandWeaponFields();service.saveItem(project,entry,tier.getText(),working,isTiered());listener.onSaved();
        }catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Save "+title(),JOptionPane.ERROR_MESSAGE);}
    }
    private boolean originallyHas(String key){return entry.getJson()!=null&&entry.getJson().get(key)!=null;}
    private void optionalStringFromUi(String key,String value,String fallback){String v=value==null?"":value;if(originallyHas(key)||!v.equals(fallback))ItemsDataService.putString(working,key,v);else ItemsDataService.remove(working,key);}
    private void optionalIntFromUi(String key,JTextField field,int fallback){int v=Integer.parseInt(field.getText().trim());if(originallyHas(key)||v!=fallback)ItemsDataService.putInt(working,key,v);else ItemsDataService.remove(working,key);}
    private void optionalBooleanFromUi(String key,JCheckBox field,boolean fallback){boolean v=field.isSelected();if(originallyHas(key)||v!=fallback)ItemsDataService.putBoolean(working,key,v);else ItemsDataService.remove(working,key);}

    private int wandWeaponFields(JPanel p, GridBagConstraints gc, int r){
        r=section(p,gc,r,"Weapon Properties");
        r=field(p,gc,r,"Base Damage",wandBaseDamage);r=field(p,gc,r,"Random Damage",wandRandDamage);r=combo(p,gc,r,"Damage Type",wandDamageType);r=field(p,gc,r,"Knockback",wandKnockback);r=field(p,gc,r,"Attack Speed",wandSpeed);r=field(p,gc,r,"Charge Speed",wandChargeSpeed);r=field(p,gc,r,"Durability",wandDurability);r=check(p,gc,r,wandChargesAttack);r=check(p,gc,r,wandTwoHanded);
        r=section(p,gc,r,"Animations");r=field(p,gc,r,"Attack Animation",wandAttackAnimation);r=field(p,gc,r,"Strong Attack Animation",wandStrongAttackAnimation);r=field(p,gc,r,"Charge Animation",wandChargeAnimation);
        r=section(p,gc,r,"Wand Item Properties");r=field(p,gc,r,"Min Item Level",wandMinLevel);r=field(p,gc,r,"Max Item Level",wandMaxLevel);r=field(p,gc,r,"Drop Sound",wandDropSound);return r;
    }
    private void loadWandWeaponFields(){if(type!=Type.WAND)return;wandBaseDamage.setText(ItemsDataService.getString(working,"baseDamage","1"));wandRandDamage.setText(ItemsDataService.getString(working,"randDamage","1"));wandDamageType.setSelectedItem(ItemsDataService.getString(working,"damageType","MAGIC"));wandKnockback.setText(ItemsDataService.getString(working,"knockback","0"));wandSpeed.setText(ItemsDataService.getString(working,"speed","1"));wandChargeSpeed.setText(ItemsDataService.getString(working,"chargespeed","1"));wandDurability.setText(ItemsDataService.getString(working,"durability","20"));wandChargesAttack.setSelected(ItemsDataService.getBoolean(working,"chargesAttack",false));wandTwoHanded.setSelected(ItemsDataService.getBoolean(working,"twoHanded",false));wandAttackAnimation.setText(ItemsDataService.getString(working,"attackAnimation",""));wandStrongAttackAnimation.setText(ItemsDataService.getString(working,"attackStrongAnimation",""));wandChargeAnimation.setText(ItemsDataService.getString(working,"chargeAnimation",""));wandMinLevel.setText(ItemsDataService.getOptionalIntText(working,"minItemLevel"));wandMaxLevel.setText(ItemsDataService.getOptionalIntText(working,"maxItemLevel"));wandDropSound.setText(ItemsDataService.getString(working,"dropSound",""));}
    private void saveWandWeaponFields(){if(type!=Type.WAND)return;optionalNumberFromUi("baseDamage",wandBaseDamage,"1");optionalNumberFromUi("randDamage",wandRandDamage,"1");optionalStringFromUi("damageType",String.valueOf(wandDamageType.getSelectedItem()),"MAGIC");optionalNumberFromUi("knockback",wandKnockback,"0");optionalNumberFromUi("speed",wandSpeed,"1");optionalNumberFromUi("chargespeed",wandChargeSpeed,"1");optionalNumberFromUi("durability",wandDurability,"20");optionalBooleanFromUi("chargesAttack",wandChargesAttack,false);optionalBooleanFromUi("twoHanded",wandTwoHanded,false);optionalStringFromUi("attackAnimation",wandAttackAnimation.getText().trim(),"");optionalStringFromUi("attackStrongAnimation",wandStrongAttackAnimation.getText().trim(),"");optionalStringFromUi("chargeAnimation",wandChargeAnimation.getText().trim(),"");optionalInt("minItemLevel",wandMinLevel);optionalInt("maxItemLevel",wandMaxLevel);optionalStringFromUi("dropSound",wandDropSound.getText().trim(),"");}
    private void optionalNumberFromUi(String key,JTextField field,String fallback){String v=field.getText().trim();if(!originallyHas(key)&&v.equals(fallback)){ItemsDataService.remove(working,key);return;}putNumber(key,field);}
    private void putNumber(String key,JTextField x){String v=x.getText().trim();if(v.matches("-?\\d+"))ItemsDataService.putInt(working,key,Integer.parseInt(v));else ItemsDataService.putFloat(working,key,Float.parseFloat(v));}

    private void ensureTypeIdentity(){
        switch(type){
            case GUN: ItemsDataService.putString(working,"class",ItemsDataService.GUN_CLASS); ItemsDataService.remove(working,"itemType"); break;
            case WAND: ItemsDataService.putString(working,"class",ItemsDataService.WAND_CLASS); ItemsDataService.putString(working,"itemType","wand"); break;
            case ARMOR: ItemsDataService.putString(working,"class",ItemsDataService.ARMOR_CLASS); ItemsDataService.putString(working,"itemType","armor"); break;
            case POTION: ItemsDataService.putString(working,"class",ItemsDataService.POTION_CLASS); ItemsDataService.putString(working,"itemType","potion"); break;
            case SCROLL: ItemsDataService.putString(working,"class",ItemsDataService.SCROLL_CLASS); ItemsDataService.putString(working,"itemType","scroll"); break;
            case FOOD: ItemsDataService.putString(working,"class",ItemsDataService.FOOD_CLASS); ItemsDataService.putString(working,"itemType","potion"); break;
            case DECORATION: ItemsDataService.putString(working,"class",ItemsDataService.DECORATION_CLASS); ItemsDataService.putString(working,"itemType","junk"); break;
            case JUNK: ItemsDataService.putString(working,"class",ItemsDataService.DECORATION_CLASS); ItemsDataService.putString(working,"itemType","junk"); break;
            case UNIQUE: if(ItemsDataService.getString(working,"class","").trim().isEmpty()) ItemsDataService.putString(working,"class",ItemsDataService.ITEM_CLASS); ItemsDataService.putBoolean(working,"unique",true); break;
        }
    }
    private void saveType(){for(JTextField x:new JTextField[]{a,b,c,d,e,f,g,h,i,j,k})if(x.getName()!=null&&!x.getName().equals("spell.class")){String v=x.getText().trim();String fallback=defaultFor(x.getName());if(!originallyHas(x.getName())&&v.equals(fallback)){ItemsDataService.remove(working,x.getName());continue;}if(v.matches("-?\\d+"))ItemsDataService.putInt(working,x.getName(),Integer.parseInt(v));else if(v.matches("-?\\d*\\.\\d+"))ItemsDataService.putFloat(working,x.getName(),Float.parseFloat(v));else ItemsDataService.putString(working,x.getName(),v);}if(d.getName()!=null&&d.getName().equals("spell.class"))setSpellClass(d.getText().trim());if(flag1.getName()!=null)optionalBooleanFromUi(flag1.getName(),flag1,type==Type.GUN);if(flag3.getName()!=null)optionalBooleanFromUi(flag3.getName(),flag3,false);if(flag2.getName()!=null)optionalBooleanFromUi(flag2.getName(),flag2,false);if(choice.getName()!=null){String v=String.valueOf(choice.getSelectedItem());String fallback=String.valueOf(choice.getItemAt(0));if(originallyHas(choice.getName())||!v.equals(fallback))ItemsDataService.putString(working,choice.getName(),v);else ItemsDataService.remove(working,choice.getName());}}
    private void setSpellClass(String cls){if(cls.isEmpty())return;JsonValue s=working.get("spell");if(s==null||!s.isObject()){s=new JsonValue(JsonValue.ValueType.object);JsonTree.put(working,"spell",s);}ItemsDataService.putString(s,"class",cls);}
    private int spellChooser(JPanel p, GridBagConstraints gc, int r) {
        JPanel box=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));box.setOpaque(false);selectedSpell.setForeground(ToolkitColors.TEXT_PRIMARY);JButton choose=new JButton("Choose Spell...");choose.addActionListener(e->chooseSpell());box.add(selectedSpell);box.add(choose);gc.gridx=0;gc.gridy=r;p.add(new JLabel("Spell"),gc);gc.gridx=1;p.add(box,gc);return r+1;
    }
    private void chooseSpell(){Window w=SwingUtilities.getWindowAncestor(this);SpellPickerDialog dlg=new SpellPickerDialog(w,project);dlg.setVisible(true);SpellLibraryService.SpellDefinition def=dlg.getSelection();if(def==null)return;JsonValue copy=new SpellLibraryService().copySpell(def);if(copy==null)return;embeddedSpell=SpellLibraryService.deepCopy(copy);ensureEmbeddedSpell();selectedSpell.setText(def.getName()+"  •  "+shortClass(ItemsDataService.getString(copy,"class","")));selectedSpell.setToolTipText("The complete spell definition will be embedded in items.dat when this item is saved.");}
    private void ensureEmbeddedSpell(){if(type!=Type.WAND&&type!=Type.SCROLL)return;if(embeddedSpell==null){JsonValue existing=working.get("spell");if(existing!=null&&existing.isObject())embeddedSpell=SpellLibraryService.deepCopy(existing);}if(embeddedSpell!=null){JsonTree.put(working,"spell",SpellLibraryService.deepCopy(embeddedSpell));}}
    private void refreshSelectedSpell(){JsonValue s=working.get("spell");if(s==null||!s.isObject()){embeddedSpell=null;selectedSpell.setText("No spell selected");return;}embeddedSpell=SpellLibraryService.deepCopy(s);selectedSpell.setText(shortClass(ItemsDataService.getString(s,"class","Unknown Spell"))+" (embedded)");}
    private String shortClass(String c){int i=c.lastIndexOf('.');return i>=0?c.substring(i+1):c;}
    private void optionalInt(String key,JTextField x){String v=x.getText().trim();if(v.isEmpty())ItemsDataService.remove(working,key);else ItemsDataService.putInt(working,key,Integer.parseInt(v));}
    private int sprite(JPanel p,GridBagConstraints gc,int r){return spriteField(p,gc,r,"World Sprite",preview,tex,false);}
    private int spriteField(JPanel p,GridBagConstraints gc,int r,String label,JLabel image,JTextField value,boolean optional){JPanel box=new JPanel(new FlowLayout(FlowLayout.LEFT));box.setOpaque(false);image.setPreferredSize(new Dimension(68,68));image.setBorder(BorderFactory.createLineBorder(ToolkitColors.BORDER));JButton choose=new JButton("Choose Sprite...");choose.addActionListener(x->chooseSprite(value,image));box.add(image);box.add(choose);if(optional){JButton clear=new JButton("Clear");clear.addActionListener(x->{value.setText("");setPreview(image,value,label.equals("Broken Sprite")?"None":"Default");});box.add(clear);}gc.gridx=0;gc.gridy=r;p.add(new JLabel(label),gc);gc.gridx=1;p.add(box,gc);return r+1;}
    private void chooseSprite(JTextField value,JLabel image){try{int current=value.getText().trim().isEmpty()?-1:Integer.parseInt(value.getText().trim());Window w=SwingUtilities.getWindowAncestor(this);SpritePickerDialog dlg=new SpritePickerDialog(w,project,atlas.getText().trim(),current);dlg.setVisible(true);SpritePickerDialog.Selection s=dlg.getSelection();if(s!=null){atlas.setText(s.getAtlas());value.setText(String.valueOf(s.getIndex()));refreshPreview();}}catch(Exception ex){JOptionPane.showMessageDialog(this,ex.getMessage(),"Sprite",JOptionPane.ERROR_MESSAGE);}}
    private void setPreview(JLabel label,JTextField value,String empty){try{String v=value.getText().trim();if(v.isEmpty()){label.setIcon(null);label.setText(empty);return;}SpriteSheetService ss=new SpriteSheetService();SpriteSheetService.SpriteSheet sh=ss.findSheet(project,atlas.getText());BufferedImage im=sh==null?null:sh.getSprite(Integer.parseInt(v));if(im!=null){label.setText("");label.setIcon(new ImageIcon(SpritePickerDialog.scaleNearest(im,58,58)));}else{label.setIcon(null);label.setText("Unavailable");}}catch(Exception ex){label.setIcon(null);label.setText("Unavailable");}}
    private void refreshPreview(){setPreview(preview,tex,"No sprite");setPreview(heldPreview,heldTex,"Default");setPreview(inventoryPreview,inventoryTex,"Default");setPreview(brokenPreview,brokenTex,"None");}
    private boolean isTiered(){return type==Type.GUN||type==Type.ARMOR;}
    private String title(){String s=type.name().toLowerCase().replace('_',' ');return Character.toUpperCase(s.charAt(0))+s.substring(1);}
    private int section(JPanel p,GridBagConstraints gc,int r,String s){JLabel l=new JLabel(s);l.setForeground(ToolkitColors.ACCENT);l.setFont(l.getFont().deriveFont(Font.BOLD,17f));l.setBorder(BorderFactory.createEmptyBorder(r==0?0:14,0,4,0));gc.gridx=0;gc.gridy=r;gc.gridwidth=2;gc.weightx=1;p.add(l,gc);gc.gridwidth=1;return r+1;}
    private int field(JPanel p,GridBagConstraints gc,int r,String s,JTextField x){JLabel l=new JLabel(s);l.setForeground(ToolkitColors.TEXT_PRIMARY);gc.gridx=0;gc.gridy=r;gc.weightx=0;p.add(l,gc);x.setPreferredSize(new Dimension(360,32));gc.gridx=1;gc.weightx=1;p.add(x,gc);return r+1;}
    private int area(JPanel p,GridBagConstraints gc,int r,String s,JTextArea x){gc.gridx=0;gc.gridy=r;p.add(new JLabel(s),gc);x.setLineWrap(true);x.setWrapStyleWord(true);JScrollPane sp=new JScrollPane(x);sp.setPreferredSize(new Dimension(360,72));gc.gridx=1;p.add(sp,gc);return r+1;}
    private int check(JPanel p,GridBagConstraints gc,int r,JCheckBox x){x.setOpaque(false);x.setForeground(ToolkitColors.TEXT_PRIMARY);gc.gridx=1;gc.gridy=r;p.add(x,gc);return r+1;}
    private int combo(JPanel p,GridBagConstraints gc,int r,String s,JComboBox<String>x){gc.gridx=0;gc.gridy=r;p.add(new JLabel(s),gc);gc.gridx=1;p.add(x,gc);return r+1;}
}
