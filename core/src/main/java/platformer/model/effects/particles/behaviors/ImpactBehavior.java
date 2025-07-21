package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.*;

public class ImpactBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(6) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setYSpeed(-1 * (rand.nextDouble() + 0.3) * SCALE);
        p.setXSpeed((rand.nextDouble() - 0.5) * 0.8 * SCALE);
        p.setGravity(0.08 * SCALE);
        p.setAlphaFadeSpeed(0.02f);
        p.setParticleColor(DUST_COLOR);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.getParticleShape().y += p.getGravity();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}