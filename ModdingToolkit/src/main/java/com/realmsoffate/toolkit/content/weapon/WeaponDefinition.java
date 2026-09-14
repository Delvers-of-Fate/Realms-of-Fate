package com.realmsoffate.toolkit.content.weapon;

import com.badlogic.gdx.utils.JsonValue;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class WeaponDefinition {

    private String className;
    private String name;
    private String itemType;

    private int tex = 0;
    private String texAtlas;

    /*
     * Properties that existed in the original JSON,
     * or were deliberately set through the toolkit.
     *
     * This prevents the toolkit from filling saved files
     * with dozens of unused default values.
     */
    private final Set<String> presentProperties =
        new HashSet<String>();

    /*
     * Anything the toolkit does not have a dedicated
     * typed field for is preserved here.
     */
    private final Map<String, JsonValue> extraProperties =
        new LinkedHashMap<String, JsonValue>();

    public WeaponDefinition() {
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(
        String className
    ) {

        this.className =
            className;

        markPropertyPresent(
            "class"
        );
    }

    public String getName() {
        return name;
    }

    public void setName(
        String name
    ) {

        this.name =
            name;

        markPropertyPresent(
            "name"
        );
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(
        String itemType
    ) {

        this.itemType =
            itemType;

        markPropertyPresent(
            "itemType"
        );
    }

    public int getTex() {
        return tex;
    }

    public void setTex(
        int tex
    ) {

        this.tex =
            tex;

        markPropertyPresent(
            "tex"
        );
    }

    public String getTexAtlas() {
        return texAtlas;
    }

    public void setTexAtlas(
        String texAtlas
    ) {

        this.texAtlas =
            texAtlas;

        markPropertyPresent(
            "texAtlas"
        );
    }

    public Map<String, JsonValue> getExtraProperties() {
        return extraProperties;
    }

    public void putExtraProperty(
        String name,
        JsonValue value
    ) {

        if (
            name == null
                || value == null
        ) {
            return;
        }

        extraProperties.put(
            name,
            value
        );

        markPropertyPresent(
            name
        );
    }

    public JsonValue getExtraProperty(
        String name
    ) {

        return extraProperties.get(
            name
        );
    }

    public boolean hasExtraProperty(
        String name
    ) {

        return extraProperties.containsKey(
            name
        );
    }

    public void removeExtraProperty(
        String name
    ) {

        extraProperties.remove(
            name
        );

        presentProperties.remove(
            name
        );
    }

    public void markPropertyPresent(
        String propertyName
    ) {

        if (propertyName == null) {
            return;
        }

        presentProperties.add(
            propertyName
        );
    }

    public boolean wasPropertyPresent(
        String propertyName
    ) {

        return presentProperties.contains(
            propertyName
        );
    }

    public Set<String> getPresentProperties() {
        return presentProperties;
    }
}
