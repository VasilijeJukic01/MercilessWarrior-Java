package platformer.model.effects.particles;

import java.awt.*;
import java.util.Random;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

/**
 * Represents a single raindrop particle for an atmospheric rain effect.
 */
public class RainParticle {

    private double x, y;
    private final double ySpeed;
    private final int length;
    private final Color color;
    private final Random rand = new Random();

    public RainParticle() {
        this.length = (int)((rand.nextInt(10) + 15) * SCALE);
        this.ySpeed = (rand.nextDouble() * 4 + 4) * SCALE;
        this.color = new Color(170, 190, 230, 150);
        reset(true);
    }

    /**
     * Resets the raindrop's position to a random location at the top of the screen.
     *
     * @param isInitial If true, the particle can spawn anywhere on the screen to fill it instantly.
     */
    private void reset(boolean isInitial) {
        this.x = rand.nextInt(GAME_WIDTH);
        if (isInitial) this.y = rand.nextInt(GAME_HEIGHT);
        else this.y = -length - rand.nextInt(GAME_HEIGHT);
    }

    /**
     * Updates the raindrop's position. If it falls off the bottom of the screen, it resets.
     */
    public void update() {
        y += ySpeed;
        if (y > GAME_HEIGHT) reset(false);
    }

    /**
     * Renders the raindrop as a single, slanted line to simulate motion.
     *
     * @param g The graphics context.
     */
    public void render(Graphics2D g) {
        g.setColor(color);
        g.setStroke(new BasicStroke(1.5f * SCALE));
        g.drawLine((int)x, (int)y, (int)(x - ySpeed / 4), (int)(y + length));
    }
}