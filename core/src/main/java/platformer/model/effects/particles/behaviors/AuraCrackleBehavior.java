package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;
import java.awt.*;
import java.util.Random;
import static platformer.constants.Constants.*;

public class AuraCrackleBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(2) + 1) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        p.setParticleColor(new Color(255, 255, 255, 220));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.15f);
        double speed = (rand.nextDouble() * 2.0 + 1.0) * SCALE;
        double sparkAngle = rand.nextDouble() * 2 * Math.PI;
        p.setXSpeed(Math.cos(sparkAngle) * speed);
        p.setYSpeed(Math.sin(sparkAngle) * speed);
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