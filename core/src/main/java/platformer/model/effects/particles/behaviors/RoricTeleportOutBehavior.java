package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Defines the "implosion" behavior for Roric's teleport departure.
 * Particles spawn in a circle and rush inward towards the center.
 */
public class RoricTeleportOutBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double initialX = p.getParticleShape().x;
        double initialY = p.getParticleShape().y;
        double width = (rand.nextInt(2) + 1) * SCALE;
        double height = (rand.nextInt(8) + 10) * SCALE;
        p.setParticleShape(new Rectangle2D.Double(0, 0, width, height));

        p.setParticleColor(new Color(175, 239, 90, 220));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.04f);
        p.setGravity(0);
        double angle = rand.nextDouble() * 2 * Math.PI;
        double radius = (rand.nextDouble() * 20 + 60) * SCALE;
        p.getParticleShape().x = initialX + radius * Math.cos(angle);
        p.getParticleShape().y = initialY + radius * Math.sin(angle);
        double speed = (rand.nextDouble() * 3.0 + 4.0) * SCALE;
        p.setXSpeed(-Math.cos(angle) * speed);
        p.setYSpeed(-Math.sin(angle) * speed);
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