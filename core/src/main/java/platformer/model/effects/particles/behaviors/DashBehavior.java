package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;

public class DashBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        double size = (rand.nextInt(4) + 4) * SCALE;
        p.getParticleShape().width = size;
        p.getParticleShape().height = size;

        int streakWidth = (int)((rand.nextInt(10) + 15) * SCALE);
        int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
        p.setParticleShape(new Rectangle2D.Double(p.getParticleShape().x, p.getParticleShape().y, streakWidth, streakHeight));
        p.setParticleColor(DUST_COLOR_DASH);
        p.setYSpeed((rand.nextDouble() - 0.5) * 0.15 * SCALE);
        p.setXSpeed(-p.getTarget().getFlipSign() * (rand.nextDouble() * 1.5 + 0.5) * SCALE);
        p.setGravity(0);
        p.setAlphaFadeSpeed(0.05f);
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