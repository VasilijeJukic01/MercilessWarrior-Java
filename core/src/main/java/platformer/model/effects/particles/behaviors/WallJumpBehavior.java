package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders angled wind-streaks and impact puffs when kicking off a wall.
 */
public class WallJumpBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        int kickDir = -p.getFlipSign();
        boolean isStreak = rand.nextDouble() < 0.65;

        if (isStreak) {
            p.setLineLength(1);
            p.getParticleShape().width = (1.8 + rand.nextDouble() * 1.2) * SCALE;
            p.getParticleShape().height = (7.0 + rand.nextDouble() * 5.0) * SCALE;
            p.setXSpeed(kickDir * (1.4 + rand.nextDouble() * 1.8) * SCALE);
            p.setYSpeed(-(0.4 + rand.nextDouble() * 1.4) * SCALE);
            p.setGravity(0.04 * SCALE);
            p.setInitialAlpha(0.90f);
            p.setCurrentAlpha(0.90f);
            p.setAlphaFadeSpeed(0.045f);
            p.setParticleColor(rand.nextBoolean() ? new Color(225, 252, 255) : new Color(120, 235, 245));
        }
        else {
            p.setLineLength(0);
            double size = (4.5 + rand.nextDouble() * 3.5) * SCALE;
            p.getParticleShape().width = size;
            p.getParticleShape().height = size;
            p.setXSpeed(kickDir * (0.5 + rand.nextDouble() * 1.0) * SCALE);
            p.setYSpeed((rand.nextDouble() - 0.5) * 1.2 * SCALE);
            p.setGravity(0.02 * SCALE);
            p.setInitialAlpha(0.75f);
            p.setCurrentAlpha(0.75f);
            p.setAlphaFadeSpeed(0.035f);
            p.setParticleColor(new Color(210, 225, 230));
        }
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        p.setXSpeed(p.getXSpeed() * 0.96);
        if (p.getLineLength() == 0) {
            double grow = 0.18 * SCALE;
            p.getParticleShape().width += grow;
            p.getParticleShape().height += grow;
        }
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gKick = (Graphics2D) g2d.create();
        gKick.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        int w = Math.max(2, (int) p.getParticleShape().width), h = Math.max(2, (int) p.getParticleShape().height);

        if (p.getLineLength() == 1) {
            AffineTransform old = gKick.getTransform();
            double angle = Math.atan2(p.getYSpeed(), p.getXSpeed()) - Math.PI / 2.0;
            gKick.translate(cx, cy);
            gKick.rotate(angle);
            gKick.fillRoundRect(-w / 2, -h / 2, w, h, w, w);
            gKick.setTransform(old);
        }
        else gKick.fillOval((int) cx - w / 2, (int) cy - h / 2, w, h);
        gKick.dispose();
    }
}