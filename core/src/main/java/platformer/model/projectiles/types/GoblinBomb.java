package platformer.model.projectiles.types;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.event.EventBus;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;

/**
 * Ballistic homemade bomb tossed by the Goblin Assassin.
 * Follows a parabolic arc with a sparking fuse and detonates in an AoE blast on impact.
 */
public class GoblinBomb extends Projectile {

    private static final int BOMB_SIZE = (int) (12 * SCALE);
    private static final double GRAVITY = 0.032 * SCALE;
    private static final double BLAST_RADIUS = 38.0 * SCALE;
    private static final int EXPLOSION_TICKS = 22;

    private double vx, vy;
    private double spinAngle = 0;
    private boolean exploding = false;
    private boolean blastDamageApplied = false;
    private int explosionTick = 0;
    private final Rectangle2D.Double emptyBounds = new Rectangle2D.Double(0, 0, 0, 0);

    public GoblinBomb(int xPos, int yPos, Direction direction, double targetDistance) {
        super(PRType.CELESTIAL_ORB, direction);
        int dirSign = (direction == Direction.LEFT) ? -1 : 1;
        double clampedDist = Math.max(40 * SCALE, Math.min(240 * SCALE, targetDistance));
        this.vx = dirSign * (0.85 + (clampedDist / (240.0 * SCALE)) * 0.95) * SCALE;
        this.vy = -(1.55 + (clampedDist / (320.0 * SCALE)) * 0.55) * SCALE;
        this.hitBox = new Rectangle2D.Double(xPos - BOMB_SIZE / 2.0, yPos - BOMB_SIZE / 2.0, BOMB_SIZE, BOMB_SIZE);
    }

    @Override
    public void updatePosition(Player player) {
        if (!alive) return;
        if (exploding) {
            explosionTick++;
            if (!blastDamageApplied && player != null) {
                blastDamageApplied = true;
                double dist = Math.hypot(player.getHitBox().getCenterX() - hitBox.getCenterX(), player.getHitBox().getCenterY() - hitBox.getCenterY());
                if (dist <= BLAST_RADIUS) {
                    player.changeHealth(-18, this);
                }
            }
            if (explosionTick >= EXPLOSION_TICKS) {
                alive = false;
            }
            return;
        }

        spinAngle += (vx >= 0 ? 0.12 : -0.12);
        vy += GRAVITY;
        hitBox.x += vx;
        hitBox.y += vy;

        if (player != null && hitBox.intersects(player.getHitBox())) {
            detonate();
        }
    }

    @Override
    public void updatePosition(Player player, ObjectManager objectManager, int[][] levelData) {
        if (!alive || exploding || levelData == null) return;
        if (CollisionDetector.isSolid(hitBox.getCenterX(), hitBox.getMaxY(), levelData) || CollisionDetector.isSolid(hitBox.getCenterX() + Math.signum(vx) * (hitBox.width * 0.5), hitBox.getCenterY(), levelData)) {
            detonate();
        }
    }

    private void detonate() {
        if (exploding) return;
        exploding = true;
        explosionTick = 0;
        Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
        EventBus.getInstance().publish(new ScreenShakeEvent(12, 6.0));
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int cx = (int) Math.round(hitBox.getCenterX() - xLevelOffset);
        int cy = (int) Math.round(hitBox.getCenterY() - yLevelOffset);

        if (exploding) {
            float progress = (float) explosionTick / EXPLOSION_TICKS;
            float r = (float) (BLAST_RADIUS * (0.35 + 0.65 * Math.sin(progress * Math.PI * 0.65)));
            int alpha = Math.max(0, Math.min(255, (int) ((1.0f - progress) * 245)));
            RadialGradientPaint blast = new RadialGradientPaint(
                    new Point2D.Float(cx, cy), Math.max(4f, r),
                    new float[]{0.0f, 0.45f, 0.80f, 1.0f},
                    new Color[]{new Color(255, 250, 190, alpha), new Color(255, 130, 20, alpha), new Color(210, 35, 10, alpha / 2), new Color(40, 20, 20, 0)}
            );
            g2d.setPaint(blast);
            g2d.fillOval((int) (cx - r), (int) (cy - r), (int) (r * 2), (int) (r * 2));
        }
        else {
            int rad = BOMB_SIZE / 2;
            g2d.setColor(new Color(28, 30, 36));
            g2d.fillOval(cx - rad, cy - rad, BOMB_SIZE, BOMB_SIZE);
            g2d.setColor(new Color(75, 82, 92));
            g2d.fillOval(cx - rad + (int) (2 * SCALE), cy - rad + (int) (2 * SCALE), (int) (4 * SCALE), (int) (4 * SCALE));

            int fuseX = cx + (int) Math.round(Math.cos(spinAngle - Math.PI / 2.0) * (rad + 3 * SCALE));
            int fuseY = cy + (int) Math.round(Math.sin(spinAngle - Math.PI / 2.0) * (rad + 3 * SCALE));
            g2d.setColor(new Color(255, 150, 20, 180));
            g2d.fillOval(fuseX - (int) (3.5 * SCALE), fuseY - (int) (3.5 * SCALE), (int) (7 * SCALE), (int) (7 * SCALE));
            g2d.setColor(new Color(255, 245, 170));
            g2d.fillOval(fuseX - (int) (1.5 * SCALE), fuseY - (int) (1.5 * SCALE), (int) (3 * SCALE), (int) (3 * SCALE));
        }

        g2d.dispose();
        if (!exploding) renderHitBox(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public Shape getShapeBounds() {
        return exploding ? emptyBounds : hitBox;
    }
}