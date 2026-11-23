package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;

/**
 * Swirling violet energy orb cast by the Goblin Mage during SPELL_1.
 */
public class GoblinMageBall extends Projectile {

    private static final int HITBOX_SIZE = (int) (11 * SCALE);
    private static final int TRAIL_COUNT = 6;
    private static final double SPEED = 1.45 * SCALE;
    private static final int MAX_LIFETIME = 230;

    private final double[] trailX = new double[TRAIL_COUNT];
    private final double[] trailY = new double[TRAIL_COUNT];
    private int trailSize = 0;
    private int trailHead = 0;

    private final double vx;
    private double baseY;
    private double phase = 0;
    private int lifeTicks = 0;

    public GoblinMageBall(int xPos, int yPos, Direction direction) {
        super(PRType.CELESTIAL_ORB, direction);
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        this.vx = dirSign * SPEED;
        this.baseY = yPos;
        this.hitBox = new Rectangle2D.Double(xPos - HITBOX_SIZE / 2.0, yPos - HITBOX_SIZE / 2.0, HITBOX_SIZE, HITBOX_SIZE);
    }

    @Override
    public void updatePosition(Player player) {
        lifeTicks++;
        if (lifeTicks >= MAX_LIFETIME) {
            alive = false;
            return;
        }

        trailX[trailHead] = hitBox.getCenterX();
        trailY[trailHead] = hitBox.getCenterY();
        trailHead = (trailHead + 1) % TRAIL_COUNT;
        if (trailSize < TRAIL_COUNT) trailSize++;

        phase += 0.16;
        hitBox.x += vx;
        hitBox.y = baseY + Math.sin(phase) * (2.5 * SCALE) - HITBOX_SIZE / 2.0;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < trailSize; i++) {
            int idx = (trailHead - 1 - i + TRAIL_COUNT) % TRAIL_COUNT;
            float ratio = 1.0f - ((float) i / TRAIL_COUNT);
            int s = Math.max(2, (int) (HITBOX_SIZE * ratio));
            int tx = (int) trailX[idx] - xLevelOffset - s / 2;
            int ty = (int) trailY[idx] - yLevelOffset - s / 2;
            g2d.setColor(new Color(175, 60, 255, (int) (130 * ratio)));
            g2d.fillOval(tx, ty, s, s);
        }

        int cx = (int) Math.round(hitBox.getCenterX() - xLevelOffset);
        int cy = (int) Math.round(hitBox.getCenterY() - yLevelOffset);
        int r = (int) (10 * SCALE);

        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(cx, cy), r,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{new Color(245, 220, 255, 245), new Color(170, 50, 255, 180), new Color(80, 0, 140, 0)}
        );
        g2d.setPaint(glow);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        int core = Math.max(2, (int) (3.2 * SCALE));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(cx - core, cy - core, core * 2, core * 2);
        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}