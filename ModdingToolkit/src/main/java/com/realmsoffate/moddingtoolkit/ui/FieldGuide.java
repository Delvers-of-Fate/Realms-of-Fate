package com.realmsoffate.moddingtoolkit.ui;
import java.util.*;
final class FieldGuide {
 private static final Map<String,String> LABELS=new HashMap<String,String>(); private static final Map<String,String> HELP=new HashMap<String,String>();
 static{
  label("name","Name","The name shown for this entry in the game or editor.");label("description","Description","Player-facing description text.");label("itemType","Item Type","The Delver item category, such as wand, sword, bow, potion, or armor.");label("class","Engine Class","Java class used by Delver when this object is created.");
  label("health","Health","How much health this entity has.");label("hp","Health","Health value used by this entry.");label("mp","Mana","Mana value used by this entry.");label("damage","Damage","Base damage dealt by this value or attack.");label("speed","Speed","Movement, attack, or projectile speed depending on this section.");
  label("usesMana","Uses Mana","When enabled, this item or action consumes mana.");label("manaCost","Mana Cost","Amount of mana consumed per use or cast.");label("usesCharges","Uses Charges","When enabled, this item tracks and consumes charges.");label("spell","Spell","Nested spell configuration used by this entry.");label("spellType","Spell Type","The spell behavior/type identifier used by the engine.");
  label("tex","Sprite / Texture","Numeric sprite index. Use Pick Sprite to choose it visually from a sprite sheet.");label("texture","Texture","Texture or sprite identifier used for this object.");label("artType","Art Type","How Delver renders the object's art.");label("equipLoc","Equip Location","Equipment slot where this item can be worn or held.");label("value","Value","Numeric or textual value for this property.");label("weight","Weight","Item weight used by the game.");label("rarity","Rarity","Rarity/category value for this entry.");label("color","Color","Color settings for this object or effect.");
 }
 private static void label(String key,String label,String help){LABELS.put(key,label);HELP.put(key,help);} private FieldGuide(){}
 static String labelFor(String key){String s=LABELS.get(key);return s==null?humanize(key):s;} static String helpFor(String key){String s=HELP.get(key);return s==null?"Custom/engine property: \""+key+"\". It is preserved exactly when saved.":s;}
 static boolean known(String key){return LABELS.containsKey(key);} static boolean advanced(String key){return "class".equals(key)||key.startsWith("_");}
 private static String humanize(String s){if(s==null||s.length()==0)return "Property";StringBuilder b=new StringBuilder();for(int i=0;i<s.length();i++){char c=s.charAt(i);if(i>0&&Character.isUpperCase(c))b.append(' ');b.append(i==0?Character.toUpperCase(c):c);}return b.toString();}
}
