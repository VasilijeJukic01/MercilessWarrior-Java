package platformer.model.effects.particles;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Represents a pre-baked diagonal volumetric sunbeam (God Ray) that shines through the forest canopy during daytime.
 */
public class SunbeamParticle {

    private static final int VARIANT_COUNT = 3;
    private static final double RAY_ANGLE_RAD = Math.toRadians(24.0);
    private static BufferedImage[] rayVariants;

    private final int levelWidth;
    private final Random rand = new Random();

    private double baseX;
    private double swayPhase, swaySpeed, swayAmp;
    private double pulsePhase, pulseSpeed;
    private float maxAlpha;
    private int width, length;
    private int variantIndex;
    private double parallaxFactor;

    public SunbeamParticle(int levelWidth, int levelHeight, int index, int totalBeams) {
        this.levelWidth = Math.max(GAME_WIDTH, levelWidth);
        ensureTexturesBaked();
        initBeam(index, totalBeams, levelHeight);
    }

    private static synchronized void ensureTexturesBaked() {
        if (rayVariants != null) return;
        rayVariants = new BufferedImage[VARIANT_COUNT];

        Color[] coreColors = {
                new Color(255, 248, 200),
                new Color(255, 232, 165),
                new Color(235, 255, 215)
        };

        int texW = Math.max(64, scale(110));
        int texH = Math.max(256, (int) (GAME_HEIGHT * 1.45));

        for (int v = 0; v < VARIANT_COUNT; v++) {
            rayVariants[v] = bakeRayTexture(texW, texH, coreColors[v]);
        }
    }

    /**
     * Pre-bakes a 2D volumetric light shaft with soft horizontal bell-curve feathering and vertical canopy attenuation.
     */
    private static BufferedImage bakeRayTexture(int w, int h, Color baseColor) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();

        for (int y = 0; y < h; y++) {
            double vNorm = (double) y / (h - 1);
            double vFade;
            if (vNorm < 0.12) {
                vFade = Math.sin((vNorm / 0.12) * (Math.PI / 2.0));
            }
            else {
                vFade = Math.pow(1.0 - ((vNorm - 0.12) / 0.88), 1.35);
            }

            for (int x = 0; x < w; x++) {
                double hNorm = (double) x / (w - 1);
                double sinH = Math.sin(hNorm * Math.PI);
                double hFade = sinH * sinH;

                double streak = 0.82 + 0.18 * Math.sin(hNorm * Math.PI * 5.0);
                int alpha = (int) Math.round(255.0 * vFade * hFade * streak);
                if (alpha > 0) {
                    alpha = Math.min(255, alpha);
                    img.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
        return img;
    }

    private void initBeam(int index, int totalBeams, int levelHeight) {
        double segment = (double) this.levelWidth / Math.max(1, totalBeams);
        this.baseX = index * segment + rand.nextDouble() * (segment * 0.75);
        this.width = (int) ((75 + rand.nextInt(70)) * SCALE);
        this.length = Math.max((int) (GAME_HEIGHT * 1.45), (int) (levelHeight * 0.95));

        this.swayPhase = rand.nextDouble() * Math.PI * 2.0;
        this.swaySpeed = 0.004 + rand.nextDouble() * 0.004;
        this.swayAmp = (10 + rand.nextDouble() * 14) * SCALE;

        this.pulsePhase = rand.nextDouble() * Math.PI * 2.0;
        this.pulseSpeed = 0.006 + rand.nextDouble() * 0.006;

        this.maxAlpha = 0.11f + rand.nextFloat() * 0.08f;
        this.variantIndex = rand.nextInt(VARIANT_COUNT);
        this.parallaxFactor = 0.58 + rand.nextDouble() * 0.18;
    }

    public void update() {
        swayPhase += swaySpeed;
        pulsePhase += pulseSpeed;
    }

    public void render(Graphics2D g2d, int xLevelOffset, int yLevelOffset, float dayVisibility) {
        if (dayVisibility <= 0.02f) return;

        float shimmer = (float) (0.55 + 0.45 * Math.sin(pulsePhase));
        float finalAlpha = maxAlpha * dayVisibility * shimmer;
        if (finalAlpha <= 0.01f) return;

        double swayOffset = Math.sin(swayPhase) * swayAmp;
        double screenX = (baseX + swayOffset) - (xLevelOffset * parallaxFactor);
        double screenY = -scale(60) - (yLevelOffset * 0.25);

        if (screenX + width * 2 < -250 || screenX - width * 2 > GAME_WIDTH + 250) return;

        Composite oldComp = g2d.getComposite();
        AffineTransform oldTransform = g2d.getTransform();

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, finalAlpha)));
        g2d.translate(screenX, screenY);
        g2d.rotate(RAY_ANGLE_RAD);
        g2d.drawImage(rayVariants[variantIndex], -width / 2, 0, width, length, null);

        g2d.setTransform(oldTransform);
        g2d.setComposite(oldComp);
    }

    public static void preload() {
        ensureTexturesBaked();
    }
}