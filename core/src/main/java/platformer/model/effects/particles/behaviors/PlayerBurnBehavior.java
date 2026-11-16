package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Path2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

/**
 * Procedural fire and char-smoke particle behavior for burning entities.
 * Renders rising teardrop flame tongues that transition from yellow-white core to orange, crimson, and dark smoke.
 */
public class PlayerBurnBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextDouble() * 3.5 + 3.5) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size * 1.45;

        if (p.getTarget() != null) {
            double spreadX = (rand.nextDouble() - 0.5) * (p.getTarget().getHitBox().width * 0.95);
            double spreadY = (rand.nextDouble() - 0.5) * (p.getTarget().getHitBox().height * 0.85);
            p.getParticleShape().x = p.getTarget().getHitBox().getCenterX() + spreadX;
            p.getParticleShape().y = p.getTarget().getHitBox().getCenterY() + spreadY;
        }

        p.setXSpeed((rand.nextDouble() - 0.5) * 0.35 * SCALE);
        p.setYSpeed(-(rand.nextDouble() * 0.85 + 0.55) * SCALE);
        p.setGravity(-0.012 * SCALE);

        p.setPulsePhase(rand.nextDouble() * Math.PI * 2);
        p.setInitialAlpha(0.95f);
        p.setCurrentAlpha(0.95f);
        p.setAlphaFadeSpeed(0.022f + rand.nextFloat() * 0.012f);
        p.setParticleColor(new Color(255, 220, 80));
    }

    @Override
    public void update(DustParticle p) {
        p.setPulsePhase(p.getPulsePhase() + 0.18);
        double flickerX = Math.sin(p.getPulsePhase()) * (0.22 * SCALE);

        p.getParticleShape().x += p.getXSpeed() + flickerX;
        p.getParticleShape().y += p.getYSpeed();
        p.getParticleShape().width *= 0.982;
        p.getParticleShape().height *= 0.985;

        float lifeRatio = p.getCurrentAlpha() / Math.max(0.01f, p.getInitialAlpha());
        if (lifeRatio > 0.72f) p.setParticleColor(new Color(255, 235, 110));
        else if (lifeRatio > 0.42f) p.setParticleColor(new Color(255, 120, 20));
        else if (lifeRatio > 0.20f) p.setParticleColor(new Color(205, 35, 10));
        else p.setParticleColor(new Color(45, 38, 38));
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        double cx = p.getParticleShape().x - xLevelOffset;
        double cy = p.getParticleShape().y - yLevelOffset;
        double hw = p.getParticleShape().width * 0.5;
        double hh = p.getParticleShape().height * 0.5;

        Graphics2D gFlame = (Graphics2D) g2d.create();
        gFlame.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Path2D.Double flame = new Path2D.Double();
        flame.moveTo(cx, cy - hh * 1.3);
        flame.quadTo(cx + hw * 1.2, cy + hh * 0.2, cx, cy + hh);
        flame.quadTo(cx - hw * 1.2, cy + hh * 0.2, cx, cy - hh * 1.3);
        flame.closePath();

        gFlame.fill(flame);
        gFlame.dispose();
    }
}