package com.interrupt.dungeoneer.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.GameApplication;
import com.interrupt.dungeoneer.portals.PortalDestination;
import com.interrupt.dungeoneer.portals.PortalDestinationManager;
import com.interrupt.dungeoneer.entities.Portal;

public class PortalScreen extends BaseScreen {

    private Portal portal;

    private Table fullTable;
    private Table menuTable;

    private boolean ignoreEscapeKey = false;

    public PortalScreen(Portal portal) {

        this.portal = portal;

        screenName = "PortalScreen";

        ui = new Stage(viewport);

        fullTable = new Table(skin);
        fullTable.setFillParent(true);
        fullTable.align(Align.center);

        menuTable = new Table(skin);
        menuTable.align(Align.center);

        ui.addActor(fullTable);

        Gdx.input.setInputProcessor(ui);
    }

    private void makeContent() {

        fullTable.clearChildren();
        menuTable.clearChildren();

        Label title =
            new Label(
                "SELECT DESTINATION",
                skin
            );

        title.setFontScale(1.15f);

        menuTable.add(title)
            .align(Align.center)
            .padBottom(20f);

        menuTable.row();

        Array<PortalDestination> destinations =
            PortalDestinationManager.getUnlockedDestinations();

        for(final PortalDestination destination : destinations) {

            TextButton destinationButton =
                new TextButton(
                    " " + destination.name + " ",
                    skin
                );

            destinationButton.addListener(
                new ClickListener() {

                    @Override
                    public void clicked(
                        InputEvent event,
                        float x,
                        float y
                    ) {

                        Audio.playSound(
                            "/ui/ui_button_click.mp3",
                            0.3f
                        );

                        System.out.println(
                            "Selected portal destination: "
                                + destination.id
                        );
                        if(portal != null) {
                            portal.setSelectedDestination(destination);
                        }
                    }

                }
            );

            menuTable.add(destinationButton)
                .width(220f)
                .height(24f)
                .padBottom(6f);

            menuTable.row();
        }

        TextButton backButton =
            new TextButton(
                " BACK ",
                skin
            );

        backButton.addListener(
            new ClickListener() {

                @Override
                public void clicked(
                    InputEvent event,
                    float x,
                    float y
                ) {

                    Audio.playSound(
                        "/ui/ui_button_click.mp3",
                        0.3f
                    );

                    GameApplication.SetScreen(
                        new MainMenuScreen()
                    );
                }
            }
        );

        menuTable.add(backButton)
            .width(220f)
            .height(24f)
            .padTop(14f);

        menuTable.row();

        fullTable.add(menuTable);

        fullTable.addAction(
            Actions.sequence(
                Actions.fadeOut(0.0001f),
                Actions.fadeIn(0.2f)
            )
        );
    }

    @Override
    public void show() {

        super.show();

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

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {

            if(!ignoreEscapeKey) {

                GameApplication.SetScreen(
                    new MainMenuScreen()
                );
            }
        }
        else {
            ignoreEscapeKey = false;
        }

        ui.act(delta);
    }
}
