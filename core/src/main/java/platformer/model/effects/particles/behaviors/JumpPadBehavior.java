package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;
import static platformer.constants.Constants.*;

public class JumpPadBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(6) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setYSpeed(-(rand.nextDouble() * 2.5 + 1.0) * SCALE);
        p.setXSpeed((rand.nextDouble() - 0.5) * 1.5 * SCALE);
        p.setGravity(0.06 * SCALE);
        p.setAlphaFadeSpeed(0.03f);
        p.setParticleColor(new Color(57, 211, 228));
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