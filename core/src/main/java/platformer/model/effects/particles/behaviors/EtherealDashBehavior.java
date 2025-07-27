package platformer.model.effects.particles.behaviors;

import platformer.model.effects.particles.DustParticle;
import platformer.model.effects.particles.ParticleBehavior;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.util.Random;

import static platformer.constants.Constants.SCALE;

public class EtherealDashBehavior implements ParticleBehavior {

    @Override
    public void init(DustParticle p, Random rand) {
        p.getParticleShape().width = 1;
        p.getParticleShape().height = 1;

        p.setParticleColor(new Color(175, 239, 90, 199));
        p.setCurrentAlpha(1.0f);
        p.setAlphaFadeSpeed(0.05f);
        p.setXSpeed((rand.nextDouble() - 0.5) * 0.3 * SCALE);
        p.setYSpeed((rand.nextDouble() - 0.5) * 0.3 * SCALE);
        p.setGravity(0);

        double angle = rand.nextDouble() * Math.PI * 2;
        double length = (rand.nextInt(20) + 25) * SCALE;
        Point2D.Double start = new Point2D.Double(0, 0);
        Point2D.Double end = new Point2D.Double(length * Math.cos(angle), length * Math.sin(angle));
        double midX = (start.x + end.x) / 2;
        double midY = (start.y + end.y) / 2;
        double perpX = -(end.y - start.y);
        double perpY = end.x - start.x;
        double mag = Math.sqrt(perpX * perpX + perpY * perpY);
        if (mag != 0) {
            perpX /= mag;
            perpY /= mag;
        }
        double controlOffset = (rand.nextDouble() * 0.6 + 0.2) * length;
        Point2D.Double control = new Point2D.Double(midX + perpX * controlOffset, midY + perpY * controlOffset);

        p.setStartPoint(start);
        p.setEndPoint(end);
        p.setControlPoint(control);
    }

    @Override
    public void update(DustParticle p) {
        p.getParticleShape().x += p.getXSpeed();
        p.getParticleShape().y += p.getYSpeed();
    }

    @Override
    public void render(DustParticle p, Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        double anchorX = p.getParticleShape().x - xLevelOffset;
        double anchorY = p.getParticleShape().y - yLevelOffset;

        QuadCurve2D.Double curve = new QuadCurve2D.Double(
                anchorX + p.getStartPoint().x, anchorY + p.getStartPoint().y,
                anchorX + p.getControlPoint().x, anchorY + p.getControlPoint().y,
                anchorX + p.getEndPoint().x, anchorY + p.getEndPoint().y
        );

        g2d.setStroke(new BasicStroke(2.0f * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(curve);
        g2d.setStroke(new BasicStroke(1));
    }
}