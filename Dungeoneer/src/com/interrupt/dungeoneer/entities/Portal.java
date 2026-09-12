package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.annotations.EditorProperty;
import com.interrupt.dungeoneer.game.Game;
import com.interrupt.dungeoneer.game.Level;
import com.interrupt.dungeoneer.overlays.OverlayManager;
import com.interrupt.dungeoneer.overlays.PortalDestinationOverlay;
import com.interrupt.dungeoneer.portals.PortalDestination;

public class Portal extends Entity {

    @EditorProperty
    public float activationRadius = 1.25f;

    @EditorProperty
    public float activationTime = 3.0f;

    private transient float particleTimer = 0f;

    private float chargeTimer = 0f;
    private boolean charged = false;

    @EditorProperty
    public String destinationGroup = "default";

    @EditorProperty
    public boolean enabled = true;

    private boolean playerInside = false;

    private PortalDestination selectedDestination = null;

    public Portal() {

        super();

        collision.x = 0.75f;
        collision.y = 0.75f;
        collision.z = 0.25f;

        isSolid = false;
    }

    @Override
    public void tick(Level level, float delta) {

        super.tick(level, delta);

        if(!enabled) {
            return;
        }

        if(Game.instance == null || Game.instance.player == null) {
            return;
        }

        Player player = Game.instance.player;

        float dx = player.x - x;
        float dy = player.y - y;

        float distanceSquared =
            dx * dx + dy * dy;

        float radiusSquared =
            activationRadius * activationRadius;

        boolean insideNow =
            distanceSquared <= radiusSquared;

        if(insideNow) {

            if(!playerInside) {

                playerInside = true;
                onPlayerEntered();
            }

            if(selectedDestination != null && !charged) {

                chargeTimer += delta;

                float chargeProgress =
                    activationTime <= 0f
                        ? 1f
                        : Math.min(chargeTimer / activationTime, 1f);

                tickPortalParticles(
                    level,
                    chargeProgress,
                    delta
                );

                if(chargeTimer >= activationTime) {

                    chargeTimer = activationTime;
                    charged = true;

                    onPortalCharged(level);
                }
            }
        }
        else {

            if(playerInside) {

                playerInside = false;
                onPlayerExited();
            }

            if(chargeTimer > 0f || charged) {

                chargeTimer = 0f;
                particleTimer = 0f;
                charged = false;
                selectedDestination = null;

                System.out.println(
                    "Portal charge reset."
                );
            }
        }
    }

    private void onPortalCharged(Level level) {

        System.out.println(
            "Portal fully charged: "
                + destinationGroup
        );

        spawnActivationBurst(level);
    }

    private void spawnActivationBurst(Level level) {

        spawnPortalRing(
            level,
            z + 0.10f,
            1.25f,
            18
        );

        spawnPortalRing(
            level,
            z + 0.65f,
            1.00f,
            16
        );

        spawnPortalRing(
            level,
            z + 1.15f,
            0.75f,
            14
        );
    }

    private void spawnPortalRing(
        Level level,
        float ringZ,
        float ringRadius,
        int particleCount
    ) {

        for(int i = 0; i < particleCount; i++) {

            float angle =
                ((float)i / particleCount)
                    * (float)Math.PI
                    * 2f;

            PortalParticle particle =
                new PortalParticle(
                    x,
                    y,
                    ringZ,
                    angle,
                    ringRadius,

                    0.045f,
                    0.0045f,
                    0.0025f,

                    // Target height
                    ringZ + 1.5f,

                    new Color(
                        0.60f,
                        0.30f,
                        1.0f,
                        1.0f
                    )
                );

            level.SpawnNonCollidingEntity(
                particle
            );
        }
    }

    private void onPlayerEntered() {

        System.out.println(
            "Player entered portal: "
                + destinationGroup
        );

        OverlayManager.instance.push(
            new PortalDestinationOverlay(this)
        );
    }

    private void onPlayerExited() {

        System.out.println(
            "Player exited portal: "
                + destinationGroup
        );
    }

    public void setSelectedDestination(
        PortalDestination destination
    ) {

        selectedDestination = destination;

        chargeTimer = 0f;
        charged = false;

        if(destination != null) {

            System.out.println(
                "Portal destination set: "
                    + destination.name
            );
        }
    }

    private void tickPortalParticles(
        Level level,
        float chargeProgress,
        float delta
    ) {

        particleTimer += delta;

        float spawnInterval =
            0.45f - (chargeProgress * 0.30f);

        if(particleTimer < spawnInterval) {
            return;
        }

        particleTimer = 0f;

        int particleCount =
            1 + (int)(chargeProgress * 2f);

        for(int i = 0; i < particleCount; i++) {

            float startAngle =
                Game.rand.nextFloat()
                    * (float)Math.PI
                    * 2f;

            float startRadius =
                1.0f
                    + Game.rand.nextFloat() * 0.35f;

            float orbitSpeed =
                0.025f
                    + chargeProgress * 0.035f;

            float inwardSpeed =
                0.0025f
                    + chargeProgress * 0.0035f;

            float riseSpeed =
                0.002f
                    + chargeProgress * 0.003f;

            PortalParticle particle =
                new PortalParticle(
                    x,
                    y,
                    z + 0.15f,
                    startAngle,
                    startRadius,
                    orbitSpeed,
                    inwardSpeed,
                    riseSpeed,

                    // Target height
                    z + 1.75f,

                    new Color(
                        0.45f,
                        0.25f,
                        1f,
                        1f
                    )
                );

            level.SpawnNonCollidingEntity(
                particle
            );
        }
    }

    public PortalDestination getSelectedDestination() {
        return selectedDestination;
    }
}
