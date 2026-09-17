package com.realmsoffate.toolkit.data;
import com.badlogic.gdx.utils.JsonValue; import com.realmsoffate.toolkit.project.ModProject;
import java.io.*; import java.util.*;
public class ClassDataService { private final DelverDataFileService files=new DelverDataFileService();
 public static class Entry{private final File file;private final JsonValue json;public Entry(File f,JsonValue j){file=f;json=j;}public File getFile(){return file;}public JsonValue getJson(){return json;}public String getName(){return ItemsDataService.getString(json,"displayName",file.getName());}}
 public List<Entry> list(ModProject p)throws IOException{List<Entry> out=new ArrayList<Entry>();File d=new File(p.getDataDirectory(),"classes");if(!d.isDirectory())return out;File[] fs=d.listFiles((x,n)->n.toLowerCase().endsWith(".json"));if(fs!=null)for(File f:fs){JsonValue j=files.load(f);if(j!=null&&j.isObject())out.add(new Entry(f,j));}return out;}
 public Entry createDraft(ModProject p){JsonValue j=new JsonValue(JsonValue.ValueType.object);ItemsDataService.putString(j,"id","new_class");ItemsDataService.putString(j,"displayName","New Class");ItemsDataService.putInt(j,"hp",100);ItemsDataService.putInt(j,"mp",20);return new Entry(null,j);}
 public void save(ModProject p,Entry e,JsonValue j)throws IOException{String id=ItemsDataService.getString(j,"id","new_class").trim().toLowerCase().replaceAll("[^a-z0-9_-]+","_");if(id.isEmpty())id="new_class";File f=e.file!=null?e.file:new File(new File(p.getDataDirectory(),"classes"),id+".json");files.save(f,j);}
}
