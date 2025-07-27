package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;
import static platformer.constants.Constants.*;

public class AuraPulseBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(3) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setParticleColor(new Color(180, 224, 255, 200));
        p.setCurrentAlpha(0.0f);
        p.setAlphaFadeSpeed(-0.035f);
        double radius = (rand.nextDouble() * 15 + 10) * SCALE;
        p.setAngle(rand.nextDouble() * 2 * Math.PI);
        p.setOffset(new Point2D.Double(radius, 0));
        p.setPulsePhase(rand.nextDouble() * Math.PI);
        p.setGravity(0);
    }

    @Override
    public void update(DustParticle p) {
        double cX = p.getTarget().getHitBox().getCenterX();
        double cY = p.getTarget().getHitBox().getCenterY();
        double pulseAmount = 15 * SCALE;
        double currentRadius = p.getOffset().getX() + Math.sin(p.getPulsePhase()) * pulseAmount;
        p.getParticleShape().x = cX + currentRadius * Math.cos(p.getAngle());
        p.getParticleShape().y = cY + currentRadius * Math.sin(p.getAngle());
        p.setPulsePhase(p.getPulsePhase() + 0.1);
        if (p.getCurrentAlpha() >= 0.9f) p.setAlphaFadeSpeed(0.03f);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}