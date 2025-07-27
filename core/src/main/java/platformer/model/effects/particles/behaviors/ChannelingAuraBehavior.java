package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Random;
import static platformer.constants.Constants.*;

public class ChannelingAuraBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(3) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setParticleColor(new Color(0, 168, 255, 150));
        p.setCurrentAlpha(0.0f);
        p.setAlphaFadeSpeed(-0.025f);
        double radius = (rand.nextDouble() * 20 + 40) * SCALE;
        p.setAngle(rand.nextDouble() * 2 * Math.PI);
        p.setOffset(new Point2D.Double(radius, 0));
        p.setGravity(0);
        p.setXSpeed(0.03);
    }

    @Override
    public void update(DustParticle p) {
        p.setAngle(p.getAngle() + p.getXSpeed());
        double centerX = p.getTarget().getHitBox().getCenterX();
        double centerY = p.getTarget().getHitBox().getCenterY();
        p.getParticleShape().x = centerX + p.getOffset().getX() * Math.cos(p.getAngle());
        p.getParticleShape().y = centerY + p.getOffset().getX() * Math.sin(p.getAngle());
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}