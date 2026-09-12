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
import com.interrupt.dungeoneer.profile.ProfileStashEntry;

public class StashScreen extends BaseScreen {

    private Table fullTable;
    private Table stashTable;

    private TextButton backButton;

    private boolean ignoreEscapeKey = false;

    public StashScreen() {

        if(splashScreenInfo != null) {
            splashLevel = splashScreenInfo.backgroundLevel;
        }

        screenName = "StashScreen";

        ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);
        fullTable.align(Align.left);

        stashTable = new Table(skin);
        stashTable.align(Align.left);

        ui.addActor(fullTable);

        Gdx.input.setInputProcessor(ui);
    }


    private void makeContent() {

        gamepadEntries.clear();
        gamepadSelectionIndex = null;

        fullTable.clearChildren();
        stashTable.clearChildren();

        PlayerProfile profile =
            ProfileManager.getProfile();

        Label title = new Label(
            "STASH",
            skin
        );

        title.setFontScale(1.25f);

        Label description = new Label(
            "Permanent Profile Items",
            skin
        );

        description.setColor(Color.LIGHT_GRAY);

        stashTable.add(title)
            .align(Align.left)
            .padBottom(8f);

        stashTable.row();

        stashTable.add(description)
            .align(Align.left)
            .padBottom(18f);

        stashTable.row();


        // ----------------------------
        // STASH CONTENTS
        // ----------------------------

        if(profile.stash == null
            || profile.stash.size == 0) {

            Label emptyLabel = new Label(
                "Your stash is empty.",
                skin
            );

            emptyLabel.setColor(Color.GRAY);

            stashTable.add(emptyLabel)
                .align(Align.left)
                .padBottom(20f);

            stashTable.row();
        }
        else {

            for(ProfileStashEntry entry
                : profile.stash) {

                if(entry == null) {
                    continue;
                }

                Table itemRow = new Table(skin);

                Label itemName = new Label(
                    entry.itemName,
                    skin
                );

                Label amount = new Label(
                    "x" + entry.amount,
                    skin
                );

                amount.setColor(Color.LIGHT_GRAY);

                itemRow.add(itemName)
                    .width(180f)
                    .align(Align.left);

                itemRow.add(amount)
                    .width(50f)
                    .align(Align.right);

                stashTable.add(itemRow)
                    .width(230f)
                    .align(Align.left)
                    .padBottom(6f);

                stashTable.row();
            }

            stashTable.add()
                .height(12f);

            stashTable.row();
        }

        // ----------------------------
// PROFILE STASH
// ----------------------------

        Label stashHeader = new Label(
            "PROFILE STASH",
            skin
        );

        stashHeader.setColor(Color.WHITE);

        stashTable.add(stashHeader)
            .align(Align.left)
            .padBottom(8f);

        stashTable.row();


        if(profile.stash == null
            || profile.stash.size == 0) {

            Label emptyLabel = new Label(
                "Your stash is empty.",
                skin
            );

            emptyLabel.setColor(Color.GRAY);

            stashTable.add(emptyLabel)
                .align(Align.left)
                .padBottom(12f);

            stashTable.row();
        }
        else {

            for(final ProfileStashEntry entry
                : profile.stash) {

                if(entry == null) {
                    continue;
                }

                final String itemName =
                    entry.itemName;

                final int itemAmount =
                    entry.amount;

                Table itemRow =
                    new Table(skin);

                Label nameLabel =
                    new Label(
                        itemName,
                        skin
                    );

                Label amountLabel =
                    new Label(
                        "x" + itemAmount,
                        skin
                    );

                amountLabel.setColor(
                    Color.LIGHT_GRAY
                );


                TextButton sendOneButton =
                    new TextButton(
                        " SEND 1 ",
                        skin
                    );


                TextButton sendAllButton =
                    new TextButton(
                        " SEND ALL ",
                        skin
                    );


                sendOneButton.addListener(
                    new ClickListener() {

                        @Override
                        public void clicked(
                            InputEvent event,
                            float x,
                            float y) {

                            Audio.playSound(
                                "/ui/ui_button_click.mp3",
                                0.3f
                            );

                            if(ProfileManager.sendToCamp(
                                itemName,
                                1)) {

                                makeContent();
                            }
                        }
                    }
                );


                sendAllButton.addListener(
                    new ClickListener() {

                        @Override
                        public void clicked(
                            InputEvent event,
                            float x,
                            float y) {

                            Audio.playSound(
                                "/ui/ui_button_click.mp3",
                                0.3f
                            );

                            if(ProfileManager.sendToCamp(
                                itemName,
                                itemAmount)) {

                                makeContent();
                            }
                        }
                    }
                );


                itemRow.add(nameLabel)
                    .width(130f)
                    .align(Align.left);

                itemRow.add(amountLabel)
                    .width(35f)
                    .align(Align.left);

                itemRow.add(sendOneButton)
                    .width(80f)
                    .height(24f)
                    .padLeft(5f);

                itemRow.add(sendAllButton)
                    .width(90f)
                    .height(24f)
                    .padLeft(5f);


                stashTable.add(itemRow)
                    .align(Align.left)
                    .padBottom(6f);

                stashTable.row();


                addGamepadEntry(
                    sendOneButton
                );

                addGamepadEntry(
                    sendAllButton
                );
            }
        }


// Spacer between storage areas.

        stashTable.add()
            .height(18f);

        stashTable.row();


// ----------------------------
// CAMP CHEST
// ----------------------------

        Label campHeader =
            new Label(
                "CAMP CHEST",
                skin
            );

        campHeader.setColor(Color.WHITE);

        stashTable.add(campHeader)
            .align(Align.left)
            .padBottom(8f);

        stashTable.row();


        if(profile.campChest == null
            || profile.campChest.size == 0) {

            Label emptyCampLabel =
                new Label(
                    "Nothing prepared for camp.",
                    skin
                );

            emptyCampLabel.setColor(
                Color.GRAY
            );

            stashTable.add(emptyCampLabel)
                .align(Align.left)
                .padBottom(12f);

            stashTable.row();
        }
        else {

            for(final ProfileStashEntry entry
                : profile.campChest) {

                if(entry == null) {
                    continue;
                }

                final String itemName =
                    entry.itemName;

                final int itemAmount =
                    entry.amount;


                Table itemRow =
                    new Table(skin);


                Label nameLabel =
                    new Label(
                        itemName,
                        skin
                    );

                Label amountLabel =
                    new Label(
                        "x" + itemAmount,
                        skin
                    );

                amountLabel.setColor(
                    Color.LIGHT_GRAY
                );


                TextButton returnOneButton =
                    new TextButton(
                        " RETURN 1 ",
                        skin
                    );


                TextButton returnAllButton =
                    new TextButton(
                        " RETURN ALL ",
                        skin
                    );


                returnOneButton.addListener(
                    new ClickListener() {

                        @Override
                        public void clicked(
                            InputEvent event,
                            float x,
                            float y) {

                            Audio.playSound(
                                "/ui/ui_button_click.mp3",
                                0.3f
                            );

                            if(ProfileManager.returnFromCamp(
                                itemName,
                                1)) {

                                makeContent();
                            }
                        }
                    }
                );


                returnAllButton.addListener(
                    new ClickListener() {

                        @Override
                        public void clicked(
                            InputEvent event,
                            float x,
                            float y) {

                            Audio.playSound(
                                "/ui/ui_button_click.mp3",
                                0.3f
                            );

                            if(ProfileManager.returnFromCamp(
                                itemName,
                                itemAmount)) {

                                makeContent();
                            }
                        }
                    }
                );


                itemRow.add(nameLabel)
                    .width(130f)
                    .align(Align.left);

                itemRow.add(amountLabel)
                    .width(35f)
                    .align(Align.left);

                itemRow.add(returnOneButton)
                    .width(90f)
                    .height(24f)
                    .padLeft(5f);

                itemRow.add(returnAllButton)
                    .width(100f)
                    .height(24f)
                    .padLeft(5f);


                stashTable.add(itemRow)
                    .align(Align.left)
                    .padBottom(6f);

                stashTable.row();


                addGamepadEntry(
                    returnOneButton
                );

                addGamepadEntry(
                    returnAllButton
                );
            }
        }


        stashTable.add()
            .height(18f);

        stashTable.row();


        // ----------------------------
        // BACK BUTTON
        // ----------------------------

        backButton = new TextButton(
            " BACK ",
            skin
        );

        backButton.addListener(
            new ClickListener() {

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
            }
        );

        stashTable.add(backButton)
            .width(140f)
            .height(24f)
            .align(Align.left);

        stashTable.row();


        // ----------------------------
        // SCREEN POSITION
        // ----------------------------

        fullTable.add(stashTable)
            .align(Align.left)
            .padLeft(70f);

        fullTable.addAction(
            Actions.sequence(
                Actions.fadeOut(0.0001f),
                Actions.fadeIn(0.2f)
            )
        );

        addGamepadEntry(backButton);
    }


    private void addGamepadEntry(
        final TextButton button) {

        GamepadEntry entry =
            new GamepadEntry(

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


    private void clickButton(
        TextButton button) {

        for(EventListener listener
            : button.getListeners()) {

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
            new ProfileScreen()
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
