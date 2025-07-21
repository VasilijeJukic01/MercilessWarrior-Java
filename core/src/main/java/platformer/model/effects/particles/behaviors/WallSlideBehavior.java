package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.*;

public class WallSlideBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(4) + 4) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setXSpeed((rand.nextDouble() * 0.5) * SCALE * -p.getTarget().getFlipSign());
        p.setYSpeed((rand.nextDouble() * 0.5) * SCALE);
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