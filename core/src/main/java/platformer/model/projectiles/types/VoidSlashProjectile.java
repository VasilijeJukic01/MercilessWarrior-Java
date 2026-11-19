package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;

/**
 * A crescent wave of dark-purple void energy hurled by a Corrupted Knight during ATTACK_2.
 */
public class VoidSlashProjectile extends Projectile {

    private static final int WIDTH = (int) (18 * SCALE);
    private static final int HEIGHT = (int) (34 * SCALE);
    private static final double SPEED = 1.65 * SCALE;
    private static final int TRAIL_COUNT = 5;
    private static final int MAX_LIFETIME = 190;

    private final double[] trailX = new double[TRAIL_COUNT];
    private int trailSize = 0;
    private int trailHead = 0;
    private int lifeTicks = 0;

    public VoidSlashProjectile(int xPos, int yPos, Direction direction) {
        super(PRType.CELESTIAL_ORB, direction);
        this.hitBox = new Rectangle2D.Double(xPos, yPos, WIDTH, HEIGHT);
    }

    @Override
    public void updatePosition(Player player) {
        lifeTicks++;
        if (lifeTicks >= MAX_LIFETIME) {
            alive = false;
            return;
        }

        trailX[trailHead] = hitBox.x;
        trailHead = (trailHead + 1) % TRAIL_COUNT;
        if (trailSize < TRAIL_COUNT) trailSize++;

        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        hitBox.x += dirSign * SPEED;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        int cy = (int) (hitBox.getCenterY() - yLevelOffset);

        for (int i = 0; i < trailSize; i++) {
            int idx = (trailHead - 1 - i + TRAIL_COUNT) % TRAIL_COUNT;
            float ratio = 1.0f - ((float) i / TRAIL_COUNT);
            int tx = (int) (trailX[idx] + WIDTH / 2.0 - xLevelOffset);
            drawCrescent(g2d, tx, cy, dirSign, (int) (WIDTH * ratio), (int) (HEIGHT * ratio), new Color(145, 40, 235, (int) (95 * ratio)));
        }

        int cx = (int) (hitBox.getCenterX() - xLevelOffset);
        drawCrescent(g2d, cx, cy, dirSign, (int) (WIDTH * 1.25), (int) (HEIGHT * 1.1), new Color(165, 55, 255, 180));
        drawCrescent(g2d, cx, cy, dirSign, WIDTH, HEIGHT, new Color(235, 195, 255, 245));

        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    private void drawCrescent(Graphics2D g2d, int cx, int cy, int dirSign, int w, int h, Color color) {
        double halfH = h / 2.0;
        double outerX = cx + dirSign * (w * 0.65);
        double innerX = cx - dirSign * (w * 0.15);
        double tipX = cx - dirSign * (w * 0.45);

        Path2D.Double crescent = new Path2D.Double();
        crescent.moveTo(tipX, cy - halfH);
        crescent.quadTo(outerX, cy, tipX, cy + halfH);
        crescent.quadTo(innerX, cy, tipX, cy - halfH);
        crescent.closePath();

        g2d.setColor(color);
        g2d.fill(crescent);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}