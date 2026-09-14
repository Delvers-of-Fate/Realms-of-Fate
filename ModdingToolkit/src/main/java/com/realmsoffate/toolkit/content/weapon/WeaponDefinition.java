package com.realmsoffate.toolkit.content.weapon;

import com.badlogic.gdx.utils.JsonValue;

import java.util.LinkedHashMap;
import java.util.Map;

public class WeaponDefinition {

    /*
     * Friendly fields understood directly by the toolkit.
     */
    private String className;
    private String name;

    private String itemType;

    private int tex = 0;
    private String texAtlas;

    /*
     * IMPORTANT:
     *
     * Any JSON fields that the toolkit does not understand yet
     * are kept here.
     *
     * This prevents the toolkit from destroying mod data when
     * loading and saving files containing custom or future fields.
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
        this.className = className;
    }

    public String getName() {
        return name;
    }

    public void setName(
        String name
    ) {
        this.name = name;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(
        String itemType
    ) {
        this.itemType = itemType;
    }

    public int getTex() {
        return tex;
    }

    public void setTex(
        int tex
    ) {
        this.tex = tex;
    }

    public String getTexAtlas() {
        return texAtlas;
    }

    public void setTexAtlas(
        String texAtlas
    ) {
        this.texAtlas = texAtlas;
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
    }
}
