package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders rising friction smoke puffs and angled wall-scrape sparks when sliding down a wall.
 */
public class WallSlideBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        boolean isSpark = rand.nextDouble() < 0.35;
        int wallNormal = -p.getFlipSign();
        if (isSpark) {
            p.setLineLength(1);
            double w = (1.5 + rand.nextDouble() * 1.2) * SCALE;
            double h = (4.5 + rand.nextDouble() * 3.5) * SCALE;
            p.getParticleShape().width = w;
            p.getParticleShape().height = h;
            p.setXSpeed(wallNormal * (0.5 + rand.nextDouble() * 1.1) * SCALE);
            p.setYSpeed(-(0.6 + rand.nextDouble() * 1.2) * SCALE);
            p.setGravity(0.08 * SCALE);
            p.setInitialAlpha(0.95f);
            p.setCurrentAlpha(0.95f);
            p.setAlphaFadeSpeed(0.045f);
            p.setParticleColor(rand.nextBoolean() ? new Color(255, 225, 130) : new Color(170, 245, 255));
        }
        else {
            p.setLineLength(0);
            double size = (3.5 + rand.nextDouble() * 3.0) * SCALE;
            p.getParticleShape().width = size;
            p.getParticleShape().height = size;
            p.setXSpeed(wallNormal * (0.15 + rand.nextDouble() * 0.35) * SCALE);
            p.setYSpeed(-(0.35 + rand.nextDouble() * 0.45) * SCALE);
            p.setGravity(-0.008 * SCALE);
            p.setInitialAlpha(0.65f);
            p.setCurrentAlpha(0.65f);
            p.setAlphaFadeSpeed(0.024f);
            p.setParticleColor(new Color(200, 215, 220));
        }
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        if (p.getLineLength() == 0) {
            double grow = 0.14 * SCALE;
            p.getParticleShape().x -= grow * 0.5;
            p.getParticleShape().y -= grow * 0.5;
            p.getParticleShape().width += grow;
            p.getParticleShape().height += grow;
        }
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gSlide = (Graphics2D) g2d.create();
        gSlide.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        int w = Math.max(2, (int) p.getParticleShape().width);
        int h = Math.max(2, (int) p.getParticleShape().height);

        if (p.getLineLength() == 1) {
            AffineTransform old = gSlide.getTransform();
            double angle = Math.atan2(p.getYSpeed(), p.getXSpeed()) - Math.PI / 2.0;
            gSlide.translate(cx, cy);
            gSlide.rotate(angle);
            gSlide.fillRoundRect(-w / 2, -h / 2, w, h, w, w);
            gSlide.setTransform(old);
        }
        else gSlide.fillOval((int) cx - w / 2, (int) cy - h / 2, w, h);
        gSlide.dispose();
    }
}