package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;
import static platformer.constants.Constants.*;

public class MythicAuraBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(2) + 2) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        float r = rand.nextFloat();
        if (r < 0.33f) p.setParticleColor(new Color(6, 2, 151, 150));
        else if (r < 0.66f) p.setParticleColor(new Color(76, 221, 237, 150));
        else p.setParticleColor(new Color(233, 9, 226, 150));
        p.setCurrentAlpha(0.0f);
        p.setAlphaFadeSpeed(-0.015f);
        double radius = (rand.nextDouble() * 20 + 25) * SCALE;
        p.setAngle(rand.nextDouble() * 2 * Math.PI);
        p.setOffset(new Point2D.Double(radius, 0));
        p.setGravity(0);
        p.setXSpeed(0.02 + rand.nextDouble() * 0.02);
        p.setPulsePhase(rand.nextDouble() * Math.PI * 2);
    }

    @Override
    public void update(DustParticle p) {
        if (p.getTarget() == null) {
            p.setActive(false);
            return;
        }
        p.setAngle(p.getAngle() + p.getXSpeed());
        double centerX = p.getTarget().getHitBox().getCenterX();
        double centerY = p.getTarget().getHitBox().getCenterY();
        double verticalBob = Math.sin(p.getAngle() * 2 + p.getPulsePhase()) * (p.getOffset().x / 4);
        p.getParticleShape().x = centerX + p.getOffset().getX() * Math.cos(p.getAngle());
        p.getParticleShape().y = centerY + p.getOffset().getX() * Math.sin(p.getAngle()) + verticalBob;
        if (p.getCurrentAlpha() >= 0.9f) p.setAlphaFadeSpeed(0.015f);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}