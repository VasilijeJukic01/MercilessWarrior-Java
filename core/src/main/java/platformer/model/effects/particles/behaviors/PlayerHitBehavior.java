package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;
import static platformer.constants.Constants.*;

public class PlayerHitBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(3) + 4) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.5 + 1.0) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
        p.setGravity(0.05 * SCALE);
        p.setAlphaFadeSpeed(0.025f);
        p.setParticleColor(new Color(255, 40, 40));
        p.setCurrentAlpha(1.0f);
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