package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class SummonRuptureBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double width = (rand.nextInt(3) + 2) * SCALE;
        double height = (rand.nextInt(10) + 15) * SCALE;
        p.setParticleShape(new Rectangle2D.Double(0, 0, width, height));

        p.setParticleColor(new Color(120, 255, 150, 200));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.04f);
        p.setXSpeed((rand.nextDouble() - 0.5) * 1.5 * SCALE);
        p.setYSpeed(-(rand.nextDouble() * 2.0 + 2.5) * SCALE);
        p.setGravity(0.1 * SCALE);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setYSpeed(p.getYSpeed() + p.getGravity());
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}