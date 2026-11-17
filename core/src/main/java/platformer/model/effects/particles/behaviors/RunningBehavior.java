package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders organic footstep particles combining soft expanding dust puffs and kicked-up moss/soil specks.
 */
public class RunningBehavior implements ParticleBehavior {

    private static final Color[] PUFF_PALETTE = {
            new Color(145, 160, 142),
            new Color(130, 142, 125),
            new Color(165, 155, 135)
    };

    private static final Color[] SPECK_PALETTE = {
            new Color(72, 112, 68),
            new Color(98, 138, 76),
            new Color(52, 88, 78),
            new Color(115, 92, 68)
    };

    @Override
    public void init(DustParticle p, Random rand) {
        int dir = p.getFlipSign() != 0 ? -p.getFlipSign() : (rand.nextBoolean() ? 1 : -1);
        boolean isMossSpeck = rand.nextDouble() < 0.42;

        if (isMossSpeck) {
            p.setLineLength(1);
            double w = (2.0 + rand.nextDouble() * 1.8) * SCALE;
            double h = (1.4 + rand.nextDouble() * 1.2) * SCALE;
            p.getParticleShape().width = w;
            p.getParticleShape().height = h;
            p.getParticleShape().y -= 2 * SCALE;

            p.setXSpeed(dir * (0.45 + rand.nextDouble() * 0.75) * SCALE);
            p.setYSpeed(-(0.65 + rand.nextDouble() * 0.75) * SCALE);
            p.setGravity(0.045 * SCALE);
            p.setAngle(rand.nextDouble() * Math.PI * 2.0);
            p.setLineAngle((rand.nextDouble() - 0.5) * 0.22);

            p.setInitialAlpha(0.85f);
            p.setCurrentAlpha(0.85f);
            p.setAlphaFadeSpeed(0.026f);
            p.setParticleColor(SPECK_PALETTE[rand.nextInt(SPECK_PALETTE.length)]);
        }
        else {
            p.setLineLength(0);
            double size = (3.2 + rand.nextDouble() * 2.6) * SCALE;
            p.getParticleShape().width = size;
            p.getParticleShape().height = size;
            p.getParticleShape().x += (rand.nextDouble() - 0.5) * 4 * SCALE;
            p.getParticleShape().y -= size * 0.5;

            p.setXSpeed(dir * (0.25 + rand.nextDouble() * 0.45) * SCALE);
            p.setYSpeed(-(0.22 + rand.nextDouble() * 0.30) * SCALE);
            p.setGravity(-0.003 * SCALE);

            p.setInitialAlpha(0.50f);
            p.setCurrentAlpha(0.50f);
            p.setAlphaFadeSpeed(0.018f);
            p.setParticleColor(PUFF_PALETTE[rand.nextInt(PUFF_PALETTE.length)]);
        }
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        p.setXSpeed(p.getXSpeed() * 0.94);

        if (p.getLineLength() == 1) {
            p.setAngle(p.getAngle() + p.getLineAngle());
        }
        else {
            double grow = 0.14 * SCALE;
            p.getParticleShape().x -= grow * 0.5;
            p.getParticleShape().y -= grow * 0.5;
            p.getParticleShape().width += grow;
            p.getParticleShape().height += grow;
        }
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gStep = (Graphics2D) g2d.create();
        gStep.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        int w = Math.max(2, (int) p.getParticleShape().width);
        int h = Math.max(2, (int) p.getParticleShape().height);

        if (p.getLineLength() == 1) {
            AffineTransform old = gStep.getTransform();
            gStep.translate(cx, cy);
            gStep.rotate(p.getAngle());
            gStep.fillRoundRect(-w / 2, -h / 2, w, h, 2, 2);
            gStep.setTransform(old);
        }
        else gStep.fillOval((int) (cx - w / 2.0), (int) (cy - h / 2.0), w, h);

        gStep.dispose();
    }
}