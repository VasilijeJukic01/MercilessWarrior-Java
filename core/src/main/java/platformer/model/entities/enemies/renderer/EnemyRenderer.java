package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Enemy;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

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

    /**
     * Renders a sharp 4-point telegraph glint during the enemy's parryable windup frames.
     */
    default void renderParryGlint(Graphics g, T enemy, int xLevelOffset, int yLevelOffset) {
        if (!enemy.isParryWindup()) return;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int dirSign = enemy.getFlipSign();
        double cx = enemy.getHitBox().getCenterX() - xLevelOffset + dirSign;
        double cy = enemy.getHitBox().y - yLevelOffset - (8 * SCALE);

        double pulse = 0.75 + 0.35 * Math.sin(System.currentTimeMillis() * 0.035);
        double longRay = 13.0 * SCALE * pulse;
        double shortRay = 7.5 * SCALE * pulse;
        double thickness = 2.4 * SCALE * pulse;

        int glowR = (int) (11 * SCALE * pulse);
        g2d.setColor(new Color(255, 210, 65, 95));
        g2d.fillOval((int) cx - glowR, (int) cy - glowR, glowR * 2, glowR * 2);

        Path2D.Double primaryStar = new Path2D.Double();
        primaryStar.moveTo(cx, cy - longRay);
        primaryStar.lineTo(cx + thickness, cy - thickness);
        primaryStar.lineTo(cx + longRay, cy);
        primaryStar.lineTo(cx + thickness, cy + thickness);
        primaryStar.lineTo(cx, cy + longRay);
        primaryStar.lineTo(cx - thickness, cy + thickness);
        primaryStar.moveTo(cx - thickness, cy + thickness);
        primaryStar.lineTo(cx - longRay, cy);
        primaryStar.lineTo(cx - thickness, cy - thickness);
        primaryStar.closePath();

        g2d.setColor(new Color(255, 220, 75, 235));
        g2d.fill(primaryStar);

        AffineTransform old = g2d.getTransform();
        g2d.translate(cx, cy);
        g2d.rotate(Math.PI / 4.0);
        Path2D.Double diagStar = new Path2D.Double();
        double dThick = thickness * 0.65;
        diagStar.moveTo(0, -shortRay);
        diagStar.lineTo(dThick, -dThick);
        diagStar.lineTo(shortRay, 0);
        diagStar.lineTo(dThick, dThick);
        diagStar.lineTo(0, shortRay);
        diagStar.lineTo(-dThick, dThick);
        diagStar.lineTo(-shortRay, 0);
        diagStar.lineTo(-dThick, -dThick);
        diagStar.closePath();
        g2d.setColor(new Color(180, 250, 255, 210));
        g2d.fill(diagStar);
        g2d.setTransform(old);

        int coreR = Math.max(2, (int) (3.0 * SCALE * pulse));
        g2d.setColor(Color.WHITE);
        g2d.fillOval((int) cx - coreR, (int) cy - coreR, coreR * 2, coreR * 2);

        g2d.dispose();
    }
}