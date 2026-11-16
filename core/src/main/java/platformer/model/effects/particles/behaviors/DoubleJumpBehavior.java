package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Renders an expanding horizontal air-ring and downward wind streaks for the mid-air double jump.
 */
public class DoubleJumpBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        if (p.getFlipSign() == 0) {
            p.getParticleShape().width = 16 * SCALE;
            p.getParticleShape().height = 5 * SCALE;
            p.setXSpeed(0);
            p.setYSpeed(0.18 * SCALE);
            p.setGravity(0);
            p.setInitialAlpha(0.90f);
            p.setCurrentAlpha(0.90f);
            p.setAlphaFadeSpeed(0.045f);
            p.setParticleColor(new Color(215, 250, 255));
        }
        else {
            double spreadX = (rand.nextDouble() - 0.5) * 20 * SCALE;
            p.getParticleShape().x += spreadX;
            p.getParticleShape().width = (rand.nextDouble() * 1.5 + 1.8) * SCALE;
            p.getParticleShape().height = (rand.nextDouble() * 5.0 + 6.0) * SCALE;
            p.setXSpeed(spreadX * 0.06);
            p.setYSpeed((rand.nextDouble() * 1.4 + 0.8) * SCALE);
            p.setGravity(0.02 * SCALE);
            p.setInitialAlpha(0.85f);
            p.setCurrentAlpha(0.85f);
            p.setAlphaFadeSpeed(0.04f);
            p.setParticleColor(rand.nextBoolean() ? new Color(230, 252, 255) : new Color(130, 235, 250));
        }
    }

    @Override
    public void update(DustParticle p) {
        if (p.getFlipSign() == 0) {
            double growX = 2.4 * SCALE;
            double growY = 0.55 * SCALE;
            p.getParticleShape().x -= growX / 2.0;
            p.getParticleShape().y += p.getYSpeed() - growY / 2.0;
            p.getParticleShape().width += growX;
            p.getParticleShape().height += growY;
        }
        else {
            p.getParticleShape().x += p.getXSpeed();
            p.getParticleShape().y += p.getYSpeed();
            p.setYSpeed(p.getYSpeed() + p.getGravity());
        }
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Graphics2D gAir = (Graphics2D) g2d.create();
        gAir.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int x = (int) (p.getParticleShape().x - xLevelOffset), y = (int) (p.getParticleShape().y - yLevelOffset);
        int w = Math.max(2, (int) p.getParticleShape().width), h = Math.max(2, (int) p.getParticleShape().height);
        if (p.getFlipSign() == 0) {
            gAir.setStroke(new BasicStroke(1.8f * SCALE));
            gAir.drawOval(x, y, w, h);
        }
        else {
            AffineTransform old = gAir.getTransform();
            double angle = Math.atan2(p.getYSpeed(), p.getXSpeed()) - Math.PI / 2.0;
            gAir.translate(x + w / 2.0, y + h / 2.0);
            gAir.rotate(angle);
            gAir.fillRoundRect(-w / 2, -h / 2, w, h, w, w);
            gAir.setTransform(old);
        }

        gAir.dispose();
    }
}