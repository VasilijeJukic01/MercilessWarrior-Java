package platformer.model.effects.particles;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Represents a single particle for a magical, ambient floating effect, inspired by Celeste's style.
 * These particles drift vertically with a gentle side-to-side sway, controlled by a sine wave.
 */
public class WindParticle {

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

    private final double parallaxFactor = 0.8;

    public WindParticle(int levelWidth, int levelHeight) {
        this.levelWidth = levelWidth;
        this.levelHeight = levelHeight;
        reset(true);
    }

    /**
     * Resets the particle's state. This is used for initialization and when the particle wraps around the screen.
     *
     * @param isInitial If true, the particle can spawn anywhere in the level.
     */
    public void reset(boolean isInitial) {
        this.size = (int)((rand.nextInt(3) + 1) * SCALE);

        this.ySpeed = (0.2 + rand.nextDouble() * 0.3) * SCALE;
        if (rand.nextBoolean()) this.ySpeed *= -1;

        this.xDrift = (rand.nextDouble() - 0.5) * 0.1 * SCALE;
        this.alpha = rand.nextFloat() * 0.4f + 0.5f;

        if (rand.nextInt(10) < 9) this.color = new Color(255, 240, 180);
        else this.color = new Color(255, 200, 110);

        if (isInitial) {
            this.baseX = rand.nextInt(levelWidth);
            this.y = rand.nextInt(levelHeight);
        }
        else {
            this.baseX = rand.nextInt(levelWidth);
            if (ySpeed > 0) this.y = -size;
            else this.y = levelHeight;
        }
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
        baseX += xDrift;
        y += ySpeed;
        angle += frequency;
        x = baseX + Math.sin(angle) * amplitude;
        if ((ySpeed > 0 && y > levelHeight) || (ySpeed < 0 && y < -size)) {
            reset(false);
        }
    }

    /**
     * Renders the particle as a glowing, semi-transparent square with a parallax effect.
     *
     * @param g2d The graphics context to draw on.
     * @param xLevelOffset The camera's horizontal offset.
     * @param yLevelOffset The camera's vertical offset.
     */
    public void render(Graphics2D g2d, int xLevelOffset, int yLevelOffset) {
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(this.color);

        int renderX = (int)(this.x - (xLevelOffset * parallaxFactor));
        int renderY = (int)(this.y - (yLevelOffset * parallaxFactor));

        g2d.fillRect(renderX, renderY, size, size);
        g2d.setComposite(originalComposite);
    }
}