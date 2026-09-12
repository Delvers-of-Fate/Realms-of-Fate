package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.Art;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.GameManager;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.overlays.ModsOverlay;
import com.interrupt.dungeoneer.overlays.OptionsOverlay;
import com.interrupt.dungeoneer.profile.ProfileManager;
import com.interrupt.managers.StringManager;

public class MainMenuScreen extends BaseScreen {

    private Table fullTable;
    private Table menuTable;

    private TextButton playButton;
    private TextButton profileButton;
    private TextButton portalButton;
    private TextButton optionsButton;
    private TextButton modsButton;
    private TextButton quitButton;

    private boolean ignoreEscapeKey = false;

    public MainMenuScreen() {

        if(splashScreenInfo != null) {
            splashLevel = splashScreenInfo.backgroundLevel;
        }

        screenName = "MainMenuScreen";

        ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);

        // This is what moves the menu toward the left side.
        fullTable.align(Align.left);

        menuTable = new Table(skin);
        menuTable.align(Align.left);

        ui.addActor(fullTable);

        Gdx.input.setInputProcessor(ui);
    }

    private void makeContent() {

        gamepadEntries.clear();
        gamepadSelectionIndex = null;

        fullTable.clearChildren();
        menuTable.clearChildren();

        Label title = new Label("REALMS OF FATE", skin);
        title.setFontScale(1.25f);

        Label profileLabel = new Label("Adventurer", skin);
        profileLabel.setColor(Color.LIGHT_GRAY);

        playButton = new TextButton(" PLAY ", skin);
        portalButton = new TextButton(" PORTAL TEST ", skin);
        profileButton = new TextButton(" PROFILE ", skin);
        optionsButton = new TextButton(" OPTIONS ", skin);
        modsButton = new TextButton(" MODS ", skin);
        quitButton = new TextButton(" QUIT ", skin);

        playButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.playSound("/ui/ui_button_click.mp3", 0.3f);
                GameApplication.SetScreen(new SaveSelectScreen());
            }
        });

        portalButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                System.out.println("PORTAL TEST CLICKED");

                Audio.playSound(
                    "/ui/ui_button_click.mp3",
                    0.3f
                );

                GameApplication.SetScreen(
                    new PortalScreen(null)
                );
            }
        });

        profileButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {

                Audio.playSound(
                    "/ui/ui_button_click.mp3",
                    0.3f
                );

                GameApplication.SetScreen(
                    new ProfileScreen()
                );
            }
        });

        optionsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.playSound("/ui/ui_button_click.mp3", 0.3f);

                GameApplication.SetScreen(
                    new OverlayWrapperScreen(
                        new OptionsOverlay(false, true)
                    )
                );
            }
        });

        modsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.playSound("/ui/ui_button_click.mp3", 0.3f);

                GameApplication.SetScreen(
                    new OverlayWrapperScreen(
                        new ModsOverlay()
                    )
                );
            }
        });

        quitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Audio.playSound("/ui/ui_button_click.mp3", 0.3f);
                Gdx.app.exit();
            }
        });

        menuTable.add(title)
            .align(Align.left)
            .padBottom(8f);

        menuTable.row();

        menuTable.add(profileLabel)
            .align(Align.left)
            .padBottom(18f);

        menuTable.row();

        addMenuButton(playButton);
        addMenuButton(portalButton);
        addMenuButton(profileButton);
        addMenuButton(optionsButton);

        if(hasMods()) {
            addMenuButton(modsButton);
        }

        addMenuButton(quitButton);

        /*
         * Left-side positioning.
         *
         * padLeft controls how far from the left edge the menu sits.
         * Increase it to move right.
         * Decrease it to move left.
         */
        fullTable.add(menuTable)
            .align(Align.left)
            .padLeft(70f);

        fullTable.addAction(
            Actions.sequence(
                Actions.fadeOut(0.0001f),
                Actions.fadeIn(0.2f)
            )
        );

        addGamepadEntry(playButton);
        addGamepadEntry(profileButton);
        addGamepadEntry(optionsButton);

        if(hasMods()) {
            addGamepadEntry(modsButton);
        }

        addGamepadEntry(quitButton);
    }

    private void addMenuButton(TextButton button) {

        menuTable.add(button)
            .width(140f)
            .height(24f)
            .align(Align.left)
            .padBottom(6f);

        menuTable.row();
    }

    private void addGamepadEntry(final TextButton button) {

        GamepadEntry entry = new GamepadEntry(
            button,
            new GamepadEntryListener() {
                @Override
                public void onPress() {
                    clickButton(button);
                }
            },
            new GamepadEntryListener() {
                @Override
                public void onPress() {
                    clickButton(button);
                }
            }
        );

        gamepadEntries.add(entry);
    }

    private void clickButton(TextButton button) {

        for(EventListener listener : button.getListeners()) {

            if(listener instanceof ClickListener) {

                ((ClickListener)listener).clicked(
                    new InputEvent(),
                    button.getX(),
                    button.getY()
                );
            }
        }
    }

    @Override
    public void show() {

        super.show();

        if(Game.instance != null) {
            Game.instance.clearMemory();
        }

        ProfileManager.load();

        if(ProfileManager.getStashAmount("Health Potion") == 0) {

            ProfileManager.addToStash(
                "Health Potion",
                3
            );
        }

        makeContent();

        ignoreEscapeKey =
            Gdx.input.isKeyPressed(Input.Keys.ESCAPE);

        if(splashScreenInfo != null
            && splashScreenInfo.music != null) {

            Audio.playMusic(
                splashScreenInfo.music,
                true
            );
        }
    }

    @Override
    public void draw(float delta) {

        super.draw(delta);

        renderer = GameManager.renderer;

        ui.draw();

        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();

        float fontSize =
            Game.getDynamicUiScale() * 140;

        float smallFontSize =
            fontSize * 0.15f;

        renderer.uiBatch.setProjectionMatrix(
            renderer.camera2D.combined
        );

        renderer.uiBatch.begin();

        renderer.drawTextRightJustified(
            Game.VERSION,
            (w / 2) - smallFontSize,
            (-h / 2) + smallFontSize,
            smallFontSize,
            Color.GRAY,
            Color.BLACK
        );

        renderer.uiBatch.end();
    }

    @Override
    public void tick(float delta) {

        super.tick(delta);

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            if(!ignoreEscapeKey) {
                Gdx.app.exit();
            }
        }
        else {
            ignoreEscapeKey = false;
        }

        ui.act(delta);
    }

    private boolean hasMods() {

        if(Game.modManager == null) {
            return false;
        }

        if(Game.modManager.modsFound == null) {
            return false;
        }

        return Game.modManager.hasExtraMods();
    }
}
