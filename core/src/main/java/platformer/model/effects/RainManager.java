package platformer.model.effects;

import platformer.model.effects.particles.RainParticle;
import java.awt.*;
import java.util.stream.Stream;

/**
 * Manages the atmospheric rain effect for the game.
 * It creates and controls a pool of RainParticle objects.
 */
public class RainManager {

    private final RainParticle[] rainParticles;
    private boolean isRaining = false;

    public RainManager() {
        this.rainParticles = new RainParticle[300];
        for (int i = 0; i < rainParticles.length; i++) {
            rainParticles[i] = new RainParticle();
        }
    }

    /**
     * Updates all rain particles if the rain effect is active.
     */
    public void update() {
        if (isRaining) Stream.of(rainParticles).parallel().forEach(RainParticle::update);
    }

    /**
     * Renders all rain particles if the rain effect is active.
     *
     * @param g The graphics context.
     */
    public void render(Graphics g) {
        if (isRaining) {
            Graphics2D g2d = (Graphics2D) g;
            for (RainParticle particle : rainParticles) {
                particle.render(g2d);
            }
        }
    }

    /**
     * Starts the rain effect.
     */
    public void startRaining() {
        isRaining = true;
    }

    /**
     * Stops the rain effect.
     */
    public void stopRaining() {
        isRaining = false;
    }

    public void reset() {
        isRaining = false;
    }
}