package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders jump and landing impact bursts combining outward-rolling ground dust puffs and arcing turf/pebble specks.
 */
public class ImpactBehavior implements ParticleBehavior {

    private static final Color[] CLOUD_PALETTE = {
            new Color(150, 165, 148),
            new Color(135, 148, 132),
            new Color(170, 160, 140)
    };

    private static final Color[] TURF_PALETTE = {
            new Color(70, 110, 66),
            new Color(96, 136, 74),
            new Color(110, 90, 68)
    };

    @Override
    public void init(DustParticle p, Random rand) {
        boolean isTurfSpeck = rand.nextDouble() < 0.38;
        double side = rand.nextBoolean() ? 1.0 : -1.0;

        if (isTurfSpeck) {
            p.setLineLength(1);
            double w = (2.2 + rand.nextDouble() * 1.8) * SCALE;
            double h = (1.6 + rand.nextDouble() * 1.4) * SCALE;
            p.getParticleShape().width = w;
            p.getParticleShape().height = h;
            p.getParticleShape().y -= 2 * SCALE;

            p.setXSpeed(side * (0.6 + rand.nextDouble() * 1.2) * SCALE);
            p.setYSpeed(-(0.9 + rand.nextDouble() * 1.1) * SCALE);
            p.setGravity(0.065 * SCALE);
            p.setAngle(rand.nextDouble() * Math.PI * 2.0);
            p.setLineAngle((rand.nextDouble() - 0.5) * 0.25);

            p.setInitialAlpha(0.88f);
            p.setCurrentAlpha(0.88f);
            p.setAlphaFadeSpeed(0.025f);
            p.setParticleColor(TURF_PALETTE[rand.nextInt(TURF_PALETTE.length)]);
        }
        else {
            p.setLineLength(0);
            double size = (4.0 + rand.nextDouble() * 3.5) * SCALE;
            p.getParticleShape().width = size;
            p.getParticleShape().height = size * 0.78;
            p.getParticleShape().x += side * (rand.nextDouble() * 4 * SCALE);
            p.getParticleShape().y -= size * 0.35;

            p.setXSpeed(side * (0.45 + rand.nextDouble() * 0.95) * SCALE);
            p.setYSpeed(-(0.12 + rand.nextDouble() * 0.28) * SCALE);
            p.setGravity(-0.002 * SCALE);

            p.setInitialAlpha(0.58f);
            p.setCurrentAlpha(0.58f);
            p.setAlphaFadeSpeed(0.019f);
            p.setParticleColor(CLOUD_PALETTE[rand.nextInt(CLOUD_PALETTE.length)]);
        }
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        p.setXSpeed(p.getXSpeed() * 0.92);

        if (p.getLineLength() == 1) {
            p.setAngle(p.getAngle() + p.getLineAngle());
        }
        else {
            double growX = 0.20 * SCALE;
            double growY = 0.12 * SCALE;
            p.getParticleShape().x -= growX * 0.5;
            p.getParticleShape().y -= growY * 0.5;
            p.getParticleShape().width += growX;
            p.getParticleShape().height += growY;
        }
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gImpact = (Graphics2D) g2d.create();
        gImpact.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        int w = Math.max(2, (int) p.getParticleShape().width);
        int h = Math.max(2, (int) p.getParticleShape().height);

        if (p.getLineLength() == 1) {
            AffineTransform old = gImpact.getTransform();
            gImpact.translate(cx, cy);
            gImpact.rotate(p.getAngle());
            gImpact.fillRoundRect(-w / 2, -h / 2, w, h, 2, 2);
            gImpact.setTransform(old);
        }
        else gImpact.fillOval((int) (cx - w / 2.0), (int) (cy - h / 2.0), w, h);

        gImpact.dispose();
    }
}