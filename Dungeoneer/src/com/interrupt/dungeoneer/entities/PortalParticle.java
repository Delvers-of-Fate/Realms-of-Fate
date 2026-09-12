package com.interrupt.dungeoneer.entities;

import com.badlogic.gdx.graphics.Color;
import com.interrupt.dungeoneer.game.Level;

public class PortalParticle extends Particle {

    private float centerX;
    private float centerY;
    private float centerZ;

    private float targetZ;

    private float angle;
    private float radius;

    private float orbitSpeed = 0.08f;
    private float inwardSpeed = 0.01f;
    private float riseSpeed = 0.01f;

    private float lifeTimer = 0f;
    private float maxLife = 180f;

    public PortalParticle(
        float centerX,
        float centerY,
        float centerZ,
        float startAngle,
        float startRadius,
        float orbitSpeed,
        float inwardSpeed,
        float riseSpeed,
        float targetZ,
        Color color
    ) {


        super();

        this.targetZ = targetZ;

        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;

        this.angle = startAngle;
        this.radius = startRadius;

        this.orbitSpeed = orbitSpeed;
        this.inwardSpeed = inwardSpeed;
        this.riseSpeed = riseSpeed;

        this.x =
            centerX
                + (float)Math.cos(angle) * radius;

        this.y =
            centerY
                + (float)Math.sin(angle) * radius;

        this.z = centerZ;

        this.color.set(color);

        fullbrite = true;
        checkCollision = false;
        floating = true;

        scale = 0.5f;
        startScale = 0.5f;
        endScale = 0f;

        tex = 0;

        persists = false;
    }

    @Override
    public void tick(Level level, float delta) {

        lifeTimer += delta;

        angle += orbitSpeed * delta;

        radius -= inwardSpeed * delta;

        if(radius < 0f) {
            radius = 0f;
        }

        x =
            centerX
                + (float)Math.cos(angle) * radius;

        y =
            centerY
                + (float)Math.sin(angle) * radius;

        z += riseSpeed * delta;

        float lifeProgress =
            Math.min(lifeTimer / maxLife, 1f);

        scale =
            startScale
                * (1f - lifeProgress);

        if(lifeTimer >= maxLife || radius <= 0.05f) {
            isActive = false;
        }
    }
}
