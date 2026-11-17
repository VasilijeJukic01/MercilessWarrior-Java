package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Enemy;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.SCALE;

/**
 * This is a Strategy interface for rendering different types of enemies in the game.
 * It defines a single method, render, which is used to draw an enemy of type T on the screen.
 *
 * @param <T> The specific type of enemy to render. This type must extend from the Enemy class.
 */
public interface EnemyRenderer<T extends Enemy> {

    /**
     * Renders an enemy of type T on the screen.
     *
     * @param g The graphics object to draw on.
     * @param enemy The enemy of type T to render.
     * @param xLevelOffset The x offset for rendering.
     * @param yLevelOffset The y offset for rendering.
     */
    void render(Graphics g, T enemy, int xLevelOffset, int yLevelOffset);

    /**
     * Draws the enemy sprite frame and, if the enemy is in its parryable windup frames, applies aN outline and pulsing flash directly to the sprite's pixels.
     */
    default void drawEnemySprite(Graphics g, T enemy, BufferedImage frame, int x, int y, int width, int height) {
        if (frame == null) return;
        if (!enemy.isParryWindup()) {
            g.drawImage(frame, x, y, width, height, null);
            return;
        }

        int absW = Math.abs(width);
        int absH = Math.abs(height);
        BufferedImage flashBuf = new BufferedImage(absW, absH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bG2d = flashBuf.createGraphics();

        double progress = (double) enemy.getAnimTick() / Math.max(1, enemy.getAnimSpeed());
        float pulse = (float) Math.sin(progress * Math.PI);

        bG2d.drawImage(frame, 0, 0, absW, absH, null);
        bG2d.setComposite(AlphaComposite.SrcAtop);
        bG2d.setColor(new Color(255, 70, 55, 235));
        bG2d.fillRect(0, 0, absW, absH);
        bG2d.dispose();

        int rimStep = Math.max(1, (int) Math.round(SCALE));
        g.drawImage(flashBuf, x - rimStep, y, width, height, null);
        g.drawImage(flashBuf, x + rimStep, y, width, height, null);
        g.drawImage(flashBuf, x, y - rimStep, width, height, null);
        g.drawImage(flashBuf, x, y + rimStep, width, height, null);

        BufferedImage bodyBuf = new BufferedImage(absW, absH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bodyG2d = bodyBuf.createGraphics();
        bodyG2d.drawImage(frame, 0, 0, absW, absH, null);
        bodyG2d.setComposite(AlphaComposite.SrcAtop);
        int flashAlpha = (int) (75 + 95 * pulse);
        bodyG2d.setColor(new Color(255, 230, 140, flashAlpha));
        bodyG2d.fillRect(0, 0, absW, absH);
        bodyG2d.dispose();

        g.drawImage(bodyBuf, x, y, width, height, null);
    }

    /**
     * Renders golden 4-point stars and comet-tail arcs orbiting along two intersecting, tilted 3D ellipses above a stunned enemy's head.
     */
    default void renderStunStars(Graphics g, T enemy, int xLevelOffset, int yLevelOffset) {
        if (!enemy.isStunned()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double time = System.currentTimeMillis() * 0.0115;
        double centerX = enemy.getHitBox().getCenterX() - xLevelOffset;
        double centerY = enemy.getHitBox().y - yLevelOffset - (11 * SCALE) + Math.sin(time * 0.6) * (1.5 * SCALE);

        double rx = 14.5 * SCALE;
        double ry = 4.8 * SCALE;
        double[] planeTilts = {Math.toRadians(17) + Math.sin(time * 0.35) * 0.12, Math.toRadians(-17) + Math.cos(time * 0.35) * 0.12};
        for (int rIdx = 0; rIdx < planeTilts.length; rIdx++) {
            AffineTransform old = g2d.getTransform();
            g2d.translate(centerX, centerY);
            g2d.rotate(planeTilts[rIdx]);
            g2d.setColor(rIdx == 0 ? new Color(255, 215, 65, 70) : new Color(255, 180, 40, 50));
            g2d.setStroke(new BasicStroke(1.0f * SCALE));
            double rScale = (rIdx == 0) ? 1.0 : 0.86;
            g2d.drawOval((int) (-rx * rScale), (int) (-ry * rScale), (int) (rx * 2 * rScale), (int) (ry * 2 * rScale));
            g2d.setTransform(old);
        }

        int starCount = 4;
        for (int pass = 0; pass < 2; pass++) {
            for (int i = 0; i < starCount; i++) {
                int ringIdx = i % 2;
                double dir = (ringIdx == 0) ? 1.0 : -1.0;
                double rScale = (ringIdx == 0) ? 1.0 : 0.86;
                double angle = (time * dir * (ringIdx == 0 ? 1.0 : 1.18)) + (i * (Math.PI * 2.0 / starCount));
                double depth = Math.sin(angle);
                boolean isFront = depth >= 0;
                if ((pass == 0 && isFront) || (pass == 1 && !isFront)) continue;

                double tilt = planeTilts[ringIdx];
                double cosT = Math.cos(tilt);
                double sinT = Math.sin(tilt);

                for (int t = 3; t >= 1; t--) {
                    double trailAngle = angle - dir * (t * 0.18);
                    double txLocal = Math.cos(trailAngle) * rx * rScale;
                    double tyLocal = Math.sin(trailAngle) * ry * rScale;
                    int tx = (int) Math.round(centerX + txLocal * cosT - tyLocal * sinT);
                    int ty = (int) Math.round(centerY + txLocal * sinT + tyLocal * cosT);
                    int dotS = Math.max(2, (int) ((3.2 - t * 0.6) * SCALE));
                    int dotA = (4 - t) * (isFront ? 38 : 22);
                    g2d.setColor(new Color(255, 210, 50, dotA));
                    g2d.fillOval(tx - dotS / 2, ty - dotS / 2, dotS, dotS);
                }

                double lx = Math.cos(angle) * rx * rScale;
                double ly = depth * ry * rScale;
                double sx = centerX + lx * cosT - ly * sinT;
                double sy = centerY + lx * sinT + ly * cosT;

                double depthScale = 0.70 + 0.38 * ((depth + 1.0) * 0.5);
                double outerR = 4.2 * SCALE * depthScale;
                double innerR = 1.6 * SCALE * depthScale;

                Path2D.Double star = new Path2D.Double();
                for (int p = 0; p < 8; p++) {
                    double a = (p * Math.PI / 4.0) + (time * 0.85 * dir);
                    double r = (p % 2 == 0) ? outerR : innerR;
                    double px = sx + Math.cos(a) * r;
                    double py = sy + Math.sin(a) * r;
                    if (p == 0) star.moveTo(px, py);
                    else star.lineTo(px, py);
                }
                star.closePath();

                int alpha = isFront ? 245 : 150;
                g2d.setColor(ringIdx == 0 ? new Color(255, 205, 35, alpha) : new Color(255, 175, 25, alpha));
                g2d.fill(star);

                if (isFront) {
                    int coreR = Math.max(1, (int) (1.5 * SCALE));
                    g2d.setColor(new Color(255, 252, 220, 240));
                    g2d.fillOval((int) sx - coreR, (int) sy - coreR, coreR * 2, coreR * 2);
                }
            }
        }

        g2d.dispose();
    }

    default void renderParryGlint(Graphics g, T enemy, int xLevelOffset, int yLevelOffset) {
        // Replaced by pixel-accurate drawEnemySprite rim-flash
    }
}