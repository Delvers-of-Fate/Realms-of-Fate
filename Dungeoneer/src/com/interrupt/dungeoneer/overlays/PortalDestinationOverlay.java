package com.interrupt.dungeoneer.overlays;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import com.interrupt.dungeoneer.Audio;
import com.interrupt.dungeoneer.entities.Portal;
import com.interrupt.dungeoneer.portals.PortalDestination;
import com.interrupt.dungeoneer.portals.PortalDestinationManager;

public class PortalDestinationOverlay extends WindowOverlay {

    private Portal portal;

    public PortalDestinationOverlay(Portal portal) {
        this.portal = portal;
    }

    @Override
    public void onShow() {

        super.onShow();

        System.out.println(
            "Portal destination overlay opened."
        );
    }

    @Override
    public Table makeContent() {

        final Table table = new Table();

        Label title = new Label(
            "SELECT DESTINATION",
            skin
        );

        title.setAlignment(Align.center);

        table.add(title)
            .padBottom(12f)
            .row();

        Array<PortalDestination> destinations =
            PortalDestinationManager.getUnlockedDestinations();

        for(final PortalDestination destination : destinations) {

            TextButton button = new TextButton(
                destination.name,
                skin
            );

            button.addListener(new ClickListener() {

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

                    if(portal != null) {

                        portal.setSelectedDestination(
                            destination
                        );
                    }

                    System.out.println(
                        "Selected portal destination: "
                            + destination.id
                    );

                    OverlayManager.instance.remove(
                        PortalDestinationOverlay.this
                    );
                }
            });

            table.add(button)
                .width(200f)
                .height(24f)
                .padBottom(6f)
                .row();
        }

        TextButton cancelButton =
            new TextButton(
                "CANCEL",
                skin
            );

        cancelButton.addListener(
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

                    OverlayManager.instance.remove(
                        PortalDestinationOverlay.this
                    );
                }
            }
        );

        table.add(cancelButton)
            .width(200f)
            .height(24f)
            .padTop(10f)
            .row();

        return table;
    }
}
