package com.interrupt.dungeoneer.editor.ui.menu;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

public class Scene2dMenuBar extends Scene2dMenu {

    public static Button playButton = null;

    public Scene2dMenuBar(Skin skin) {
        super(skin);
        playButton = new Button(skin);
        playButton.add(new Image(skin, "menu-arrow")).size(14);
    }

    @Override
    public float getHeight() {
        if(menuTable != null) return menuTable.getHeight();
        return 40f;
    }

    @Override
    public float getWidth() {
        if(menuTable != null) return menuTable.getWidth();
        return 300f;
    }

    @Override
    public void pack() {
        super.pack();
    }

    @Override
    public void close() {
        for(Actor a : items) {
            if(a instanceof MenuItem) {
                MenuItem i = (MenuItem) a;
                i.updateStyle(false);
                if (i.subMenu != null) i.subMenu.close();
            }
        }
    }

    @Override
    protected void refreshDrawables() {
        clearChildren();

        // make the table
        menuTable = new Table();
        menuTable.setZIndex(10);
        menuTable.setOrigin(0f, 50f);
        menuTable.setSkin(skin);
        addActor(menuTable);

        float r = 0;

        // add the rows
        for(Actor a : items) {
            menuTable.add(a)
                .align(Align.left)
                .fill()
                .padLeft(6f)
                .padRight(6f)
                .padTop(3f)
                .padBottom(3f);

            r = a.getRight();
        }

        menuTable.add(playButton)
            .width(38f)
            .height(32f)
            .align(Align.left)
            .padLeft(16f)
            .padTop(4f)
            .padBottom(4f)
            .fill();

        menuTable.setBackground("menu_default_normal");

        // add all of the sub menus to this space
        for(Actor a : items) {
            if(a instanceof MenuItem) {
                MenuItem i = (MenuItem)a;
                i.setParentMenu(this);
                if(i.subMenu != null) {
                    i.showExpandArrow = false;
                    i.refresh();
                    addActor(i.subMenu);
                }
            }
        }

        menuTable.pack();
    }
}
