package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class CelestialAuraBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(3) + 3) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        float r = rand.nextFloat();
        if (r < 0.6f) p.setParticleColor(new Color(252, 239, 141, 181));
        else p.setParticleColor(new Color(91, 166, 117, 199));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.015f);
        p.setGravity(0);
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 2.0 + 1.0) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
        p.setXSpeed(p.getXSpeed() * 0.99);
        p.setYSpeed(p.getYSpeed() * 0.99);
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        g2d.fill(p.getTranslatedShape(xLevelOffset, yLevelOffset));
    }
}