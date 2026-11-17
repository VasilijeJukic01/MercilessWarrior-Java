package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

/**
 * A spectral soul-bolt projectile fired by the Wraith, featuring an undulating ectoplasm trail and soft vertical homing.
 */
public class WraithBolt extends Projectile {

    private static final int HITBOX_SIZE = scale(12);
    private static final int TRAIL_LENGTH = 8;
    private static final double BOLT_SPEED = 1.35 * SCALE;
    private static final int MAX_LIFETIME = 240;

    private final double[] trailX = new double[TRAIL_LENGTH];
    private final double[] trailY = new double[TRAIL_LENGTH];
    private int trailCount = 0;
    private int trailHead = 0;

    private final double vx;
    private double vy = 0;
    private double baseY;
    private double wavePhase = 0;
    private int lifeTicks = 0;

    public WraithBolt(int xPos, int yPos, Direction direction) {
        super(PRType.CELESTIAL_ORB, direction);
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        this.vx = dirSign * BOLT_SPEED;
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
        trailHead = (trailHead + 1) % TRAIL_LENGTH;
        if (trailCount < TRAIL_LENGTH) trailCount++;

        wavePhase += 0.14;
        hitBox.x += vx;

        if (player != null) {
            double targetY = player.getHitBox().getCenterY();
            double dy = targetY - baseY;
            vy = Math.max(-0.28 * SCALE, Math.min(0.28 * SCALE, dy * 0.015));
            baseY += vy;
        }
        hitBox.y = baseY + Math.sin(wavePhase) * (3.5 * SCALE) - HITBOX_SIZE / 2.0;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < trailCount; i++) {
            int idx = (trailHead - 1 - i + TRAIL_LENGTH) % TRAIL_LENGTH;
            float ratio = 1.0f - ((float) i / TRAIL_LENGTH);
            int size = Math.max(2, (int) (HITBOX_SIZE * 1.1 * ratio));
            int tx = (int) trailX[idx] - xLevelOffset - size / 2;
            int ty = (int) trailY[idx] - yLevelOffset - size / 2;
            int alpha = Math.max(0, Math.min(255, (int) (ratio * 135)));
            g2d.setColor(new Color(0, 225, 200, alpha));
            g2d.fillOval(tx, ty, size, size);
        }

        int cx = (int) hitBox.getCenterX() - xLevelOffset;
        int cy = (int) hitBox.getCenterY() - yLevelOffset;
        int outerR = (int) (11 * SCALE);

        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(cx, cy),
                outerR,
                new float[]{0.0f, 0.5f, 1.0f},
                new Color[]{new Color(220, 255, 250, 245), new Color(0, 220, 195, 175), new Color(0, 110, 120, 0)}
        );
        g2d.setPaint(glow);
        g2d.fillOval(cx - outerR, cy - outerR, outerR * 2, outerR * 2);

        int coreR = Math.max(2, (int) (3.5 * SCALE));
        g2d.setColor(new Color(240, 255, 255));
        g2d.fillOval(cx - coreR, cy - coreR, coreR * 2, coreR * 2);

        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}