package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;

/**
 * Heavy wooden-shafted iron javelin hurled by the Goblin Spearman.
 */
public class GoblinSpear extends Projectile {

    private static final int LENGTH = (int) (28 * SCALE);
    private static final int HEIGHT = (int) (6 * SCALE);
    private static final double SPEED = 2.05 * SCALE;
    private static final double GRAVITY = 0.006 * SCALE;
    private static final int MAX_LIFETIME = 200;

    private final double vx;
    private double vy = -0.18 * SCALE;
    private int lifeTicks = 0;

    public GoblinSpear(int xPos, int yPos, Direction direction) {
        super(PRType.ARROW, direction);
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        this.vx = dirSign * SPEED;
        this.hitBox = new Rectangle2D.Double(xPos - LENGTH / 2.0, yPos - HEIGHT / 2.0, LENGTH, HEIGHT);
    }

    @Override
    public void updatePosition(Player player) {
        lifeTicks++;
        if (lifeTicks >= MAX_LIFETIME) {
            alive = false;
            return;
        }
        vy += GRAVITY;
        hitBox.x += vx;
        hitBox.y += vy;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) Math.round(hitBox.getCenterX() - xLevelOffset);
        int cy = (int) Math.round(hitBox.getCenterY() - yLevelOffset);
        double angle = Math.atan2(vy, vx);

        AffineTransform old = g2d.getTransform();
        g2d.translate(cx, cy);
        g2d.rotate(angle);

        int halfLen = LENGTH / 2;
        g2d.setColor(new Color(128, 88, 52));
        g2d.fillRect(-halfLen, (int) (-1.2 * SCALE), (int) (LENGTH * 0.72), (int) (2.4 * SCALE));

        Path2D.Double tip = new Path2D.Double();
        tip.moveTo(halfLen * 0.35, -3.2 * SCALE);
        tip.lineTo(halfLen + 2 * SCALE, 0);
        tip.lineTo(halfLen * 0.35, 3.2 * SCALE);
        tip.closePath();
        g2d.setColor(new Color(205, 215, 222));
        g2d.fill(tip);
        g2d.setColor(new Color(245, 252, 255));
        g2d.drawLine((int) (halfLen * 0.45), 0, halfLen + (int) SCALE, 0);

        g2d.setTransform(old);
        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.YELLOW);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}