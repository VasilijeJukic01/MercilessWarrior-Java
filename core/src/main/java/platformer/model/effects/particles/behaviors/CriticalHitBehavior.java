package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;
import static platformer.constants.Constants.*;

public class CriticalHitBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        int streakWidth = (int)((rand.nextInt(10) + 20) * SCALE);
        int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
        p.setParticleShape(new Rectangle2D.Double(p.getParticleShape().x, p.getParticleShape().y, streakWidth, streakHeight));
        p.setParticleColor(new Color(255, 60, 30));
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = (rand.nextDouble() * 3.0 + 2.0) * SCALE;
        p.setXSpeed(Math.cos(angle) * speed);
        p.setYSpeed(Math.sin(angle) * speed);
        p.setGravity(0.05 * SCALE);
        p.setAlphaFadeSpeed(0.04f);
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