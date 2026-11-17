package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders directional crimson blood droplets and white impact shards when the player takes damage.
 */
public class PlayerHitBehavior implements ParticleBehavior {

    private static final Color[] HIT_PALETTE = {
            new Color(255, 30, 40),
            new Color(200, 10, 20),
            new Color(255, 90, 80),
            new Color(255, 240, 240)
    };

    @Override
    public void init(DustParticle p, Random rand) {
        boolean isStreak = rand.nextDouble() < 0.65;
        if (isStreak) {
            p.setLineLength(1);
            p.getParticleShape().width = (1.8 + rand.nextDouble() * 1.4) * SCALE;
            p.getParticleShape().height = (6.5 + rand.nextDouble() * 5.5) * SCALE;
        }
        else {
            p.setLineLength(0);
            double size = (3.0 + rand.nextDouble() * 3.0) * SCALE;
            p.getParticleShape().width = size;
            p.getParticleShape().height = size;
        }

        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 3.2 + 1.4) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed - 0.6 * SCALE);
        p.setGravity(0.065 * SCALE);
        p.setAlphaFadeSpeed(0.032f);
        p.setParticleColor(HIT_PALETTE[rand.nextInt(HIT_PALETTE.length)]);
        p.setCurrentAlpha(1.0f);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
        p.setXSpeed(p.getXSpeed() * 0.95);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gHit = (Graphics2D) g2d.create();
        gHit.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        int w = Math.max(2, (int) p.getParticleShape().width);
        int h = Math.max(2, (int) p.getParticleShape().height);

        if (p.getLineLength() == 1) {
            AffineTransform old = gHit.getTransform();
            double angle = Math.atan2(p.getYSpeed(), p.getXSpeed()) - Math.PI / 2.0;
            gHit.translate(cx, cy);
            gHit.rotate(angle);
            gHit.fillRoundRect(-w / 2, -h / 2, w, h, w, w);
            gHit.setTransform(old);
        }
        else {
            gHit.fillOval((int) (cx - w / 2.0), (int) (cy - h / 2.0), w, h);
        }

        gHit.dispose();
    }
}