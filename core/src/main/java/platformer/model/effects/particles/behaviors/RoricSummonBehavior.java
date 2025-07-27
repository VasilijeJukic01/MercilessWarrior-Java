package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class RoricSummonBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(10) + 5) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setParticleColor(new Color(150, 255, 170, 200));
        p.setCurrentAlpha(0.9f);
        p.setAlphaFadeSpeed(0.025f);
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.0 + 1.5) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
        p.setGravity(0);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}