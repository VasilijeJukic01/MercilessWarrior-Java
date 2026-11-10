package platformer.model.effects.particles;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Represents a single particle for a magical, ambient floating effect, inspired by Celeste's style.
 * These particles drift vertically with a gentle side-to-side sway, controlled by a sine wave.
 */
public class WindParticle {

    private static final Color[] STORM_PALETTE = {
            new Color(80, 245, 255),
            new Color(195, 250, 255),
            new Color(30, 180, 255),
            new Color(150, 130, 255)
    };

    private final int levelWidth, levelHeight;
    private double x, y;
    private double xDrift;
    private double ySpeed;
    private int size;
    private float alpha;
    private Color color;
    private final Random rand = new Random();

    private double baseX;
    private double angle;
    private double amplitude;
    private double frequency;
    private boolean stormPeakMode = false;

    private final double parallaxFactor = 0.8;

    public WindParticle(int levelWidth, int levelHeight) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        reset(true);
    }

    public void setStormPeakMode(boolean stormPeakMode) {
        if (this.stormPeakMode != stormPeakMode) {
            this.stormPeakMode = stormPeakMode;
            reset(true);
        }
    }

    public void reset(boolean isInitial) {
        this.size = (int) ((rand.nextInt(3) + 1) * SCALE);
        this.alpha = rand.nextFloat() * 0.45f + 0.45f;
        if (stormPeakMode) {
            this.ySpeed = -(0.28 + rand.nextDouble() * 0.42) * SCALE;
            this.xDrift = (rand.nextDouble() - 0.5) * 0.08 * SCALE;
            this.color = STORM_PALETTE[rand.nextInt(STORM_PALETTE.length)];
        }
        else {
            this.ySpeed = (0.2 + rand.nextDouble() * 0.3) * SCALE;
            if (rand.nextBoolean()) this.ySpeed *= -1;
            this.xDrift = (rand.nextDouble() - 0.5) * 0.1 * SCALE;
            this.color = (rand.nextInt(10) < 9) ? new Color(255, 240, 180) : new Color(255, 200, 110);
        }

        this.baseX = rand.nextInt(Math.max(1, levelWidth));
        if (isInitial) this.y = rand.nextInt(Math.max(1, levelHeight));
        else this.y = (ySpeed > 0) ? -size : levelHeight + size;
        this.x = this.baseX;

        this.angle = rand.nextDouble() * Math.PI * 2;
        this.amplitude = (rand.nextDouble() * 10 + 8) * SCALE;
        this.frequency = (rand.nextDouble() * 0.02 + 0.01);
    }

    /**
     * Updates the particle's position each frame and handles screen wrapping.
     * The particle drifts vertically and sways horizontally using a sine wave.
     */
    public void update() {
        update(1.0);
    }

    public void update(double speedMultiplier) {
        baseX += xDrift * speedMultiplier;
        y += ySpeed * speedMultiplier;
        angle += frequency * speedMultiplier;
        x = baseX + Math.sin(angle) * amplitude;
        if ((ySpeed > 0 && y > levelHeight + size) || (ySpeed < 0 && y < -size)) {
            reset(false);
        }
    }

    /**
     * Renders the particle as a glowing, semi-transparent square with a parallax effect.
     *
     * @param g2d The graphics context to draw on.
     * @param xLevelOffset The camera's horizontal offset.
     * @param yLevelOffset The camera's vertical offset.
     * @param visibilityMultiplier A multiplier for the particle's alpha to simulate day/night visibility.
     */
    public void render(Graphics2D g2d, int xLevelOffset, int yLevelOffset, float visibilityMultiplier) {
        float pulse = stormPeakMode ? (float) (0.75 + 0.25 * Math.sin(angle * 2.5)) : 1.0f;
        float finalAlpha = alpha * visibilityMultiplier * pulse;
        if (finalAlpha <= 0.02f) return;

        int renderX = (int) (this.x - (xLevelOffset * parallaxFactor));
        int renderY = (int) (this.y - (yLevelOffset * parallaxFactor));

        Composite originalComposite = g2d.getComposite();
        if (stormPeakMode && size >= 2 * SCALE) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, finalAlpha * 0.35f)));
            g2d.setColor(this.color);
            int glowSize = size * 3;
            g2d.fillOval(renderX - glowSize / 3, renderY - glowSize / 3, glowSize, glowSize);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, finalAlpha)));
        g2d.setColor(this.color);
        g2d.fillRect(renderX, renderY, size, size);
        g2d.setComposite(originalComposite);
    }
}