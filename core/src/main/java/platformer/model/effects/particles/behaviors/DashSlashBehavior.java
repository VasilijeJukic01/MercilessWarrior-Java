package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.util.Random;
import static platformer.constants.Constants.*;

public class DashSlashBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        p.setXSpeed((rand.nextDouble() - 0.5) * 0.3 * SCALE);
        p.setYSpeed((rand.nextDouble() - 0.5) * 0.3 * SCALE);
        p.setGravity(0);
        p.setAlphaFadeSpeed(0.06f);
        p.setParticleColor(new Color(0, 255, 240));
        p.setCurrentAlpha(0.9f);
        p.setLineLength((int)((rand.nextInt(10) + 8) * SCALE));
        p.setLineAngle(rand.nextDouble() * Math.PI);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        int x1 = (int) (p.getParticleShape().x - xLevelOffset);
        int y1 = (int) (p.getParticleShape().y - yLevelOffset);
        int x2 = x1 + (int) (p.getLineLength() * Math.cos(p.getLineAngle()));
        int y2 = y1 + (int) (p.getLineLength() * Math.sin(p.getLineAngle()));
        g2d.setStroke(new BasicStroke(2 * (float)SCALE));
        g2d.drawLine(x1, y1, x2, y2);
        g2d.setStroke(new BasicStroke(1));
    }
}