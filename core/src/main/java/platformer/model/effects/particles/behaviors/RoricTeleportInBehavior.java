package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Defines the "explosion" behavior for Roric's teleport arrival.
 * Particles spawn at the center and rush outward.
 */
public class RoricTeleportInBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double width = (rand.nextInt(2) + 1) * SCALE;
        double height = (rand.nextInt(8) + 10) * SCALE;
        p.getParticleShape().width = width;
        p.getParticleShape().height = height;

        p.setParticleColor(new Color(175, 239, 90, 220));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.03f);
        p.setGravity(0);
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.5 + 2.0) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}