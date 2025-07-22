package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class BeamChargeBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(3) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        float r = rand.nextFloat();
        if (r < 0.5f) p.setParticleColor(new Color(100, 255, 120, 180));
        else p.setParticleColor(new Color(200, 255, 210, 180));
        p.setCurrentAlpha(0.0f);
        p.setAlphaFadeSpeed(-0.04f);
        double radius = (rand.nextDouble() * 30 + 35) * SCALE;
        p.setAngle(rand.nextDouble() * 2 * Math.PI);
        p.setOffset(new java.awt.geom.Point2D.Double(radius, 0));
        p.setGravity(0);
        p.setXSpeed(0.04 + rand.nextDouble() * 0.03);
        p.setPulsePhase(rand.nextDouble() * Math.PI * 2);
    }

    @Override
    public void update(DustParticle p) {
        if (p.getTarget() == null) {
            p.setActive(false);
            return;
        }
        p.getOffset().x *= 0.99;
        p.setAngle(p.getAngle() + p.getXSpeed());
        double centerX = p.getTarget().getHitBox().getCenterX();
        double centerY = p.getTarget().getHitBox().getCenterY();
        double verticalBob = Math.sin(p.getAngle() * 3 + p.getPulsePhase()) * (p.getOffset().x / 3);
        p.getParticleShape().x = centerX + p.getOffset().getX() * Math.cos(p.getAngle());
        p.getParticleShape().y = centerY + p.getOffset().getX() * Math.sin(p.getAngle()) + verticalBob;
        if (p.getCurrentAlpha() >= 0.9f) p.setAlphaFadeSpeed(0.015f);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}