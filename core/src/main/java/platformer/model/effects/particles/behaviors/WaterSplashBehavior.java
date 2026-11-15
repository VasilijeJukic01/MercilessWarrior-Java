package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Physics behavior for water splash droplets that launch in outward-curving arcs and orient themselves along their velocity vector.
 */
public class WaterSplashBehavior implements ParticleBehavior {

    private static final Color[] WATER_PALETTE = {
            new Color(220, 252, 255),
            new Color(135, 238, 250),
            new Color(70, 205, 230),
            new Color(175, 245, 255)
    };

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextDouble() * 2.2 + 2.0) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        int dir = p.getFlipSign();
        if (dir != 0) {
            // Launch in a fountain arc outward to the left (-1) or right (+1)
            double speed = (1.6 + rand.nextDouble() * 1.8) * SCALE;
            double angle = Math.toRadians(35 + rand.nextDouble() * 35);
            p.setXSpeed(dir * Math.cos(angle) * speed);
            p.setYSpeed(-Math.sin(angle) * speed);
        }
        else {
            p.setXSpeed((rand.nextDouble() - 0.5) * 2.4 * SCALE);
            p.setYSpeed(-(rand.nextDouble() * 2.4 + 1.2) * SCALE);
        }

        p.setGravity(0.075 * SCALE);
        p.setCurrentAlpha(0.95f);
        p.setAlphaFadeSpeed(0.020f);
        p.setParticleColor(WATER_PALETTE[rand.nextInt(WATER_PALETTE.length)]);
    }

    @Override
    public void update(DustParticle p) {
        p.setXSpeed(p.getXSpeed() * 0.985);
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        double cx = p.getParticleShape().x - xLevelOffset, cy = p.getParticleShape().y - yLevelOffset;
        double vx = p.getXSpeed(), vy = p.getYSpeed();

        double speed = Math.hypot(vx, vy);
        double angle = Math.atan2(vy, vx);
        int len = Math.max(2, (int) (p.getParticleShape().width * (1.0 + Math.min(1.4, speed * 0.28))));
        int thick = Math.max(2, (int) (p.getParticleShape().height * 0.75));

        AffineTransform old = g2d.getTransform();
        g2d.translate(cx, cy);
        g2d.rotate(angle);
        g2d.fillOval(-len / 2, -thick / 2, len, thick);
        g2d.setTransform(old);
    }
}