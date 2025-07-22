package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class SummonVortexBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(4) + 4) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setParticleColor(new Color(180, 255, 200, 150));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.02f);
        p.setGravity(0);
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 1.0 + 1.5) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setXSpeed(p.getXSpeed() * 0.96 - p.getParticleShape().x * 0.05);
        p.setYSpeed(p.getYSpeed() * 0.96 - p.getParticleShape().y * 0.05);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}