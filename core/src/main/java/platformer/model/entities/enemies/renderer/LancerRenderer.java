package platformer.model.entities.enemies.renderer;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.boss.Lancer;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class LancerRenderer implements EnemyRenderer<Lancer> {

    private static final int GHOST_COUNT = 4;
    private static final int PIXEL_AURA_FRAMES = 8;
    private static final int PIXEL_GRID_SIZE = 48;
    private static final int SATELLITE_COUNT = 4;

    private final BufferedImage[][] animations;
    private final BufferedImage[] customPixelAura;

    private final int[] ghostX = new int[GHOST_COUNT];
    private final int[] ghostY = new int[GHOST_COUNT];
    private final int[] ghostFlipSign = new int[GHOST_COUNT];
    private final int[] ghostState = new int[GHOST_COUNT];
    private final int[] ghostFrame = new int[GHOST_COUNT];
    private int ghostHead = 0;
    private int ghostSize = 0;
    private int recordTick = 0;

    private double orbitPhase = 0.0;

    public LancerRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.LANCER);
        this.customPixelAura = generatePixelArtStormAura();
    }

    /**
     * Pre-renders an 8-frame 48x48 pixel-art electric storm ring so its pixels match Lancer's sprite resolution.
     */
    private BufferedImage[] generatePixelArtStormAura() {
        BufferedImage[] frames = new BufferedImage[PIXEL_AURA_FRAMES];
        Random rand = new Random(42L);

        int cyanOuter = new Color(0, 195, 255, 190).getRGB();
        int cyanMid = new Color(90, 245, 255, 235).getRGB();
        int whiteCore = new Color(235, 255, 255, 255).getRGB();

        double center = PIXEL_GRID_SIZE / 2.0;

        for (int f = 0; f < PIXEL_AURA_FRAMES; f++) {
            BufferedImage img = new BufferedImage(PIXEL_GRID_SIZE, PIXEL_GRID_SIZE, BufferedImage.TYPE_INT_ARGB);
            double frameAngleOffset = (f * Math.PI * 2.0) / PIXEL_AURA_FRAMES;
            int segments = 72;
            for (int s = 0; s < segments; s++) {
                double theta = (s * Math.PI * 2.0) / segments;
                double wave = Math.sin(theta * 4 + frameAngleOffset) * 1.8 + Math.cos(theta * 7 - frameAngleOffset * 2) * 1.2;
                double r = 17.5 + wave + (rand.nextDouble() - 0.5) * 1.4;

                // Rotating gaps
                if (Math.sin(theta * 2 + frameAngleOffset) < -0.55) continue;
                int px = (int) Math.round(center + Math.cos(theta) * r);
                int py = (int) Math.round(center + Math.sin(theta) * r);

                setPixelSafe(img, px, py, whiteCore);
                setPixelSafe(img, px + 1, py, cyanMid);
                setPixelSafe(img, px - 1, py, cyanMid);
                setPixelSafe(img, px, py + 1, cyanOuter);
                setPixelSafe(img, px, py - 1, cyanOuter);
            }

            // Orbiting pixel sparks inside and outside the ring
            for (int i = 0; i < 6; i++) {
                double sparkAngle = frameAngleOffset * 1.5 + (i * Math.PI / 3.0);
                double sparkR = (i % 2 == 0) ? 12.5 : 21.0;
                int sx = (int) Math.round(center + Math.cos(sparkAngle) * sparkR);
                int sy = (int) Math.round(center + Math.sin(sparkAngle) * sparkR);
                setPixelSafe(img, sx, sy, whiteCore);
                setPixelSafe(img, sx + 1, sy, cyanOuter);
            }

            frames[f] = img;
        }
        return frames;
    }

    private void setPixelSafe(BufferedImage img, int x, int y, int argb) {
        if (x >= 0 && x < img.getWidth() && y >= 0 && y < img.getHeight()) {
            img.setRGB(x, y, argb);
        }
    }

    @Override
    public void render(Graphics g, Lancer lancer, int xLevelOffset, int yLevelOffset) {
        Anim state = lancer.getEnemyAction();
        int fC = lancer.getFlipCoefficient(), fS = lancer.getFlipSign();
        int worldDrawX = (int) lancer.getHitBox().x - LANCER_X_OFFSET + fC - (fS == -1 ? (int) (21 * SCALE) : 0);
        int worldDrawY = (int) lancer.getHitBox().y - LANCER_Y_OFFSET + (int) lancer.getPushOffset() + 1;

        boolean showAfterimages = (state == Anim.SPELL_1 && lancer.getAnimIndex() >= 2 && lancer.getAnimIndex() <= 11) || (state == Anim.ATTACK_3 && lancer.getAnimIndex() >= 1 && lancer.getAnimIndex() <= 4);
        if (showAfterimages) {
            recordTick++;
            if (recordTick >= 3) {
                recordTick = 0;
                ghostX[ghostHead] = worldDrawX;
                ghostY[ghostHead] = worldDrawY;
                ghostFlipSign[ghostHead] = fS;
                ghostState[ghostHead] = state.ordinal();
                ghostFrame[ghostHead] = lancer.getAnimIndex();
                ghostHead = (ghostHead + 1) % GHOST_COUNT;
                if (ghostSize < GHOST_COUNT) ghostSize++;
            }
            renderAfterimages(g, xLevelOffset, yLevelOffset);
        }
        else ghostSize = 0;

        boolean isAerialChanneling = (state == Anim.SPELL_4);
        int centerX = (int) (lancer.getHitBox().getCenterX() - xLevelOffset);
        int centerY = (int) (lancer.getHitBox().getCenterY() - yLevelOffset);
        if (isAerialChanneling) {
            orbitPhase += 0.055;
            renderBackStormAura(g, centerX, centerY);
            renderOrbitingSatellites(g, centerX, centerY, false);
        }

        int screenX = worldDrawX - xLevelOffset;
        int screenY = worldDrawY - yLevelOffset;
        g.drawImage(animations[state.ordinal()][lancer.getAnimIndex()], screenX, screenY, LANCER_WIDTH * fS, LANCER_HEIGHT, null);

        if (isAerialChanneling) renderOrbitingSatellites(g, centerX, centerY, true);
        lancer.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        lancer.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderBackStormAura(Graphics g, int cx, int cy) {
        Graphics2D g2d = (Graphics2D) g.create();
        double pulse = (1.0 + Math.sin(orbitPhase * 1.4)) / 2.0;
        int glowRadius = (int) ((40 + pulse * 10) * SCALE);
        RadialGradientPaint backGlow = new RadialGradientPaint(
                new Point2D.Float(cx, cy), glowRadius,
                new float[]{0.0f, 0.55f, 1.0f},
                new Color[]{new Color(180, 250, 255, 135), new Color(0, 185, 255, 70), new Color(0, 70, 200, 0)}
        );
        g2d.setPaint(backGlow);
        g2d.fillOval(cx - glowRadius, cy - glowRadius, glowRadius * 2, glowRadius * 2);
        int frameIdx = (int) ((System.currentTimeMillis() / 75) % PIXEL_AURA_FRAMES);
        int auraSize = (int) ((88 + pulse * 8) * SCALE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2d.drawImage(customPixelAura[frameIdx], cx - auraSize / 2, cy - auraSize / 2, auraSize, auraSize, null);

        g2d.dispose();
    }

    /**
     * Renders 4 plasma satellites orbiting Lancer along two intersecting 3D tilted ellipses.
     * Satellites with z < 0 render behind Lancer; satellites with z >= 0 render in front of Lancer.
     */
    private void renderOrbitingSatellites(Graphics g, int cx, int cy, boolean frontPass) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double rx = 38 * SCALE;
        double ry = 13 * SCALE;

        for (int i = 0; i < SATELLITE_COUNT; i++) {
            double angle = orbitPhase * 1.3 + (i * Math.PI / 2.0);
            double z = Math.sin(angle);
            boolean isFront = (z >= 0);
            if (isFront != frontPass) continue;

            // Alternate between two +/- 28-degree tilted orbital planes (X-gyroscope)
            double planeTilt = (i % 2 == 0) ? Math.toRadians(26) : Math.toRadians(-26);
            double localX = Math.cos(angle) * rx;
            double localY = Math.sin(angle) * ry;

            // Fading trail steps behind each orbiting satellite
            for (int t = 3; t >= 1; t--) {
                double trailAngle = angle - (t * 0.14);
                double txLocal = Math.cos(trailAngle) * rx;
                double tyLocal = Math.sin(trailAngle) * ry;
                int tx = cx + (int) (txLocal * Math.cos(planeTilt) - tyLocal * Math.sin(planeTilt));
                int ty = cy + (int) (txLocal * Math.sin(planeTilt) + tyLocal * Math.cos(planeTilt));
                int tSize = (int) ((6 - t) * SCALE);
                int tAlpha = (4 - t) * 28;
                g2d.setColor(new Color(0, 215, 255, tAlpha));
                g2d.fillOval(tx - tSize / 2, ty - tSize / 2, tSize, tSize);
            }

            int ox = cx + (int) (localX * Math.cos(planeTilt) - localY * Math.sin(planeTilt));
            int oy = cy + (int) (localX * Math.sin(planeTilt) + localY * Math.cos(planeTilt));

            double depthScale = 0.85 + 0.30 * ((z + 1.0) / 2.0);
            int haloR = Math.max(3, (int) (9 * SCALE * depthScale));
            int coreR = Math.max(2, (int) (3.5 * SCALE * depthScale));
            RadialGradientPaint satGlow = new RadialGradientPaint(
                    new Point2D.Float(ox, oy), haloR,
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{new Color(230, 255, 255, 240), new Color(0, 215, 255, 165), new Color(0, 90, 220, 0)}
            );
            g2d.setPaint(satGlow);
            g2d.fillOval(ox - haloR, oy - haloR, haloR * 2, haloR * 2);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(ox - coreR, oy - coreR, coreR * 2, coreR * 2);
        }

        g2d.dispose();
    }

    private void renderAfterimages(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        int count = ghostSize;
        int head = ghostHead;
        for (int i = count - 1; i >= 0; i--) {
            int idx = (head - 1 - i + GHOST_COUNT) % GHOST_COUNT;
            float alpha = (1.0f - (float) i / GHOST_COUNT) * 0.42f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            BufferedImage frame = animations[ghostState[idx]][ghostFrame[idx]];
            int gx = ghostX[idx] - xLevelOffset;
            int gy = ghostY[idx] - yLevelOffset;
            g2d.drawImage(frame, gx, gy, LANCER_WIDTH * ghostFlipSign[idx], LANCER_HEIGHT, null);
        }
        g2d.dispose();
    }
}