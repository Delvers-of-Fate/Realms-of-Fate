package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.profile.PlayerProfile;
import com.interrupt.dungeoneer.profile.ProfileManager;

public class ProfileScreen extends BaseScreen {

    private Table fullTable;
    private Table profileTable;

    private TextButton stashButton;
    private TextButton backButton;

    private boolean ignoreEscapeKey = false;

    public ProfileScreen() {

        if(splashScreenInfo != null) {
            splashLevel = splashScreenInfo.backgroundLevel;
        }

        screenName = "ProfileScreen";

        ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);
        fullTable.align(Align.left);

        profileTable = new Table(skin);
        profileTable.align(Align.left);

        ui.addActor(fullTable);

        Gdx.input.setInputProcessor(ui);
    }

    private void makeContent() {

        gamepadEntries.clear();
        gamepadSelectionIndex = null;

        fullTable.clearChildren();
        profileTable.clearChildren();

        PlayerProfile profile = ProfileManager.getProfile();

        Label title = new Label("PROFILE", skin);
        title.setFontScale(1.25f);

        Label username = new Label(
            profile.username,
            skin
        );

        username.setColor(Color.WHITE);

        int stashAmount = 0;

        if(profile.stash != null) {
            for(int i = 0; i < profile.stash.size; i++) {
                stashAmount += profile.stash.get(i).amount;
            }
        }

        Label stashLabel = new Label(
            "Stash Items: " + stashAmount,
            skin
        );

        stashLabel.setColor(Color.LIGHT_GRAY);

        stashButton = new TextButton(
            " STASH ",
            skin
        );

        backButton = new TextButton(
            " BACK ",
            skin
        );

        stashButton.addListener(new ClickListener() {
            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y) {

                Audio.playSound(
                    "/ui/ui_button_click.mp3",
                    0.3f
                );

                GameApplication.SetScreen(
                    new StashScreen()
                );
            }
        });

        backButton.addListener(new ClickListener() {
            @Override
            public void clicked(
                InputEvent event,
                float x,
                float y) {

                Audio.playSound(
                    "/ui/ui_button_click.mp3",
                    0.3f
                );

                goBack();
            }
        });

        profileTable.add(title)
            .align(Align.left)
            .padBottom(10f);

        profileTable.row();

        profileTable.add(username)
            .align(Align.left)
            .padBottom(6f);

        profileTable.row();

        profileTable.add(stashLabel)
            .align(Align.left)
            .padBottom(18f);

        profileTable.row();

        addMenuButton(stashButton);
        addMenuButton(backButton);

        // Same left-side position as the new main menu.
        fullTable.add(profileTable)
            .align(Align.left)
            .padLeft(70f);

        fullTable.addAction(
            Actions.sequence(
                Actions.fadeOut(0.0001f),
                Actions.fadeIn(0.2f)
            )
        );

        addGamepadEntry(stashButton);
        addGamepadEntry(backButton);
    }

    private void addMenuButton(TextButton button) {

        profileTable.add(button)
            .width(140f)
            .height(24f)
            .align(Align.left)
            .padBottom(6f);

        profileTable.row();
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

    private void goBack() {
        GameApplication.SetScreen(
            new MainMenuScreen()
        );
    }

    @Override
    public void show() {

        super.show();

        ProfileManager.load();

        makeContent();

        ignoreEscapeKey =
            Gdx.input.isKeyPressed(
                Input.Keys.ESCAPE
            );
    }

    @Override
    public void draw(float delta) {

        super.draw(delta);

        ui.draw();
    }

    @Override
    public void tick(float delta) {

        super.tick(delta);

        if(Gdx.input.isKeyJustPressed(
            Input.Keys.ESCAPE)) {

            if(!ignoreEscapeKey) {
                goBack();
            }
        }
        else {
            ignoreEscapeKey = false;
        }

        ui.act(delta);
    }
}
