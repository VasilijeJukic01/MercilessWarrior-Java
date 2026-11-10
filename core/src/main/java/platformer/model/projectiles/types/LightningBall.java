package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isSolid;

public class LightningBall extends Projectile {

    public enum Mode {
        RICOCHET,
        SPEAR_HURL_LOW,
    }

    private static final int HITBOX_SIZE = scale(13);
    private static final int ORB_RADIUS = scale(11);
    private static final int TRAIL_CAPACITY = 7;
    private static final int MAX_LIFETIME = 320;

    private final Mode mode;
    private final double[] trailX = new double[TRAIL_CAPACITY];
    private final double[] trailY = new double[TRAIL_CAPACITY];
    private int trailCount = 0;
    private int trailHead = 0;

    private final boolean bounceOffWalls;
    private double vx, vy;
    private int bouncesRemaining;
    private int lifeTicks = 0;
    private double phase = 0;

    public LightningBall(int handX, int handY, Direction direction, Mode mode) {
        super(PRType.LIGHTNING_BALL, direction);
        this.mode = mode;
        this.bounceOffWalls = false;
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        this.vx = dirSign * (1.22 * SCALE);
        this.vy = 0.85 * SCALE;
        this.bouncesRemaining = 1;
        this.hitBox = new Rectangle2D.Double(handX - HITBOX_SIZE / 2.0, handY - HITBOX_SIZE / 2.0, HITBOX_SIZE, HITBOX_SIZE);
    }

    public LightningBall(int centerX, int centerY, double angleRadians, double speed, int maxBounces, boolean bounceOffWalls) {
        super(PRType.LIGHTNING_BALL, Direction.DOWN);
        this.mode = Mode.RICOCHET;
        this.bouncesRemaining = maxBounces;
        this.bounceOffWalls = bounceOffWalls;
        this.vx = Math.cos(angleRadians) * speed;
        this.vy = Math.sin(angleRadians) * speed;
        this.hitBox = new Rectangle2D.Double(centerX - HITBOX_SIZE / 2.0, centerY - HITBOX_SIZE / 2.0, HITBOX_SIZE, HITBOX_SIZE);
    }

    @Override
    public void updatePosition(Player player) {
        phase += 0.12;
        lifeTicks++;
        if (lifeTicks >= MAX_LIFETIME) alive = false;
    }

    @Override
    public void updatePosition(Player player, ObjectManager objectManager, int[][] levelData) {
        if (!alive) return;
        recordTrailPoint(hitBox.getCenterX(), hitBox.getCenterY());
        if (mode == Mode.SPEAR_HURL_LOW) updateLowSkimmer(levelData);
        else updateRicochet(levelData);
    }

    private void recordTrailPoint(double x, double y) {
        trailX[trailHead] = x;
        trailY[trailHead] = y;
        trailHead = (trailHead + 1) % TRAIL_CAPACITY;
        if (trailCount < TRAIL_CAPACITY) trailCount++;
    }

    private void updateLowSkimmer(int[][] levelData) {
        hitBox.x += vx;
        if (vy > 0) {
            double checkBottom = hitBox.y + vy + hitBox.height + (4 * SCALE);
            if (isSolid(hitBox.getCenterX(), checkBottom, levelData)) {
                vy = 0;
            }
            else hitBox.y += vy;
        }
        if (isSolid(hitBox.getCenterX() + Math.signum(vx) * (hitBox.width / 2.0), hitBox.getCenterY(), levelData)) {
            alive = false;
        }
    }

    private void updateRicochet(int[][] levelData) {
        double nextX = hitBox.getCenterX() + vx + Math.signum(vx) * (hitBox.width / 2.0);
        double nextY = hitBox.getCenterY() + vy + Math.signum(vy) * (hitBox.height / 2.0);
        boolean hitX = isSolid(nextX, hitBox.getCenterY(), levelData);
        boolean hitY = isSolid(hitBox.getCenterX(), nextY, levelData);
        if (hitX && !bounceOffWalls) {
            alive = false;
            return;
        }
        if (hitX || hitY) {
            if (bouncesRemaining > 0) {
                bouncesRemaining--;
                if (hitX) vx = -vx;
                if (hitY) vy = -vy;
            }
            else {
                alive = false;
                return;
            }
        }
        hitBox.x += vx;
        hitBox.y += vy;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float fadeAlpha = lifeTicks > (MAX_LIFETIME - 50) ? Math.max(0f, (MAX_LIFETIME - lifeTicks) / 50.0f) : 1.0f;
        if (fadeAlpha < 1.0f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        }

        int cx = (int) hitBox.getCenterX() - xLevelOffset, cy = (int) hitBox.getCenterY() - yLevelOffset;
        int count = trailCount;
        int head = trailHead;
        for (int i = 0; i < count; i++) {
            int bufIdx = (head - 1 - i + TRAIL_CAPACITY) % TRAIL_CAPACITY;
            float ratio = 1.0f - ((float) i / TRAIL_CAPACITY);
            int tSize = (int) (HITBOX_SIZE * 0.85 * ratio);
            int tx = (int) trailX[bufIdx] - xLevelOffset - tSize / 2;
            int ty = (int) trailY[bufIdx] - yLevelOffset - tSize / 2;
            g2d.setColor(new Color(0, 210, 255, Math.max(0, Math.min(255, (int) (ratio * 105)))));
            g2d.fillOval(tx, ty, tSize, tSize);
        }

        double pulse = 1.0 + 0.10 * Math.sin(phase * 1.5);
        int haloR = Math.max(4, (int) (ORB_RADIUS * 1.45 * pulse));
        RadialGradientPaint halo = new RadialGradientPaint(
                new Point2D.Float(cx, cy), haloR,
                new float[]{0.0f, 0.45f, 1.0f},
                new Color[]{new Color(235, 255, 255, 245), new Color(0, 215, 255, 160), new Color(0, 90, 220, 0)}
        );
        g2d.setPaint(halo);
        g2d.fillOval(cx - haloR, cy - haloR, haloR * 2, haloR * 2);

        int rimR = Math.max(3, (int) (ORB_RADIUS * 0.62));
        g2d.setColor(new Color(60, 240, 255, 230));
        g2d.fillOval(cx - rimR, cy - rimR, rimR * 2, rimR * 2);

        int coreR = Math.max(2, (int) (ORB_RADIUS * 0.42));
        g2d.setColor(Color.WHITE);
        g2d.fillOval(cx - coreR, cy - coreR, coreR * 2, coreR * 2);
        g2d.dispose();
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }

    @Override
    public Shape getShapeBounds() {
        return super.hitBox;
    }
}
