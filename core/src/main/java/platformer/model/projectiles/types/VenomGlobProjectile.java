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
 * Glowing bio-Projectile with a fading droplet trail, used for Widow Spider acid spit,
 * Briar Husk thorn darts, and Root Weaver / Death Rider bolts.
 */
public class VenomGlobProjectile extends Projectile {

    private static final int HITBOX_SIZE = (int) (10 * SCALE);
    private static final int TRAIL_COUNT = 6;
    private static final int MAX_LIFETIME = 210;

    private final double[] trailX = new double[TRAIL_COUNT];
    private final double[] trailY = new double[TRAIL_COUNT];
    private int trailSize = 0;
    private int trailHead = 0;

    private final double vx;
    private double vy;
    private final double gravity;
    private final Color coreColor;
    private final Color outerColor;
    private int lifeTicks = 0;

    public VenomGlobProjectile(int xPos, int yPos, Direction direction, double speed, double initialVy, double gravity, Color coreColor, Color outerColor) {
        super(PRType.CELESTIAL_ORB, direction);
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        this.vx = dirSign * speed * SCALE;
        this.vy = initialVy * SCALE;
        this.gravity = gravity * SCALE;
        this.coreColor = coreColor;
        this.outerColor = outerColor;
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

        vy += gravity;
        hitBox.x += vx;
        hitBox.y += vy;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < trailSize; i++) {
            int idx = (trailHead - 1 - i + TRAIL_COUNT) % TRAIL_COUNT;
            float ratio = 1.0f - ((float) i / TRAIL_COUNT);
            int s = Math.max(2, (int) (HITBOX_SIZE * 0.95 * ratio));
            int tx = (int) trailX[idx] - xLevelOffset - s / 2;
            int ty = (int) trailY[idx] - yLevelOffset - s / 2;
            g2d.setColor(new Color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), (int) (135 * ratio)));
            g2d.fillOval(tx, ty, s, s);
        }

        int cx = (int) Math.round(hitBox.getCenterX() - xLevelOffset);
        int cy = (int) Math.round(hitBox.getCenterY() - yLevelOffset);
        int r = (int) (9.5 * SCALE);

        RadialGradientPaint glow = new RadialGradientPaint(
                new Point2D.Float(cx, cy), r,
                new float[]{0.0f, 0.55f, 1.0f},
                new Color[]{
                        coreColor,
                        new Color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), 185),
                        new Color(outerColor.getRed(), outerColor.getGreen(), outerColor.getBlue(), 0)
                }
        );
        g2d.setPaint(glow);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);

        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, outerColor);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}