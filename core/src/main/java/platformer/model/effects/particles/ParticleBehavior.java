package platformer.model.effects.particles;

import java.awt.*;
import java.util.Random;

/**
 * An interface defining the contract for a particle's behavior (Strategy).
 * Each implementation of this interface will define the unique initialization, update logic, and rendering method for a specific type of particle effect.
 *
 * @see platformer.model.effects.particles.DustParticle
 * @see platformer.model.effects.particles.ParticleFactory
 */
public interface ParticleBehavior {

    /**
     * Initializes the state of a particle (e.g., speed, color, gravity).
     * This is called once by the factory upon particle creation.
     *
     * @param particle The particle instance to initialize.
     * @param rand A shared Random instance for generating varied effects.
     */
    void init(DustParticle particle, Random rand);

    /**
     * Updates the particle's state for a single frame.
     * This typically involves moving the particle according to its velocity and other unique rules.
     *
     * @param particle The particle instance to update.
     */
    void update(DustParticle particle);

    /**
     * Renders the particle onto the screen.
     * This allows for custom rendering logic, such as drawing lines for trails instead of simple rectangles.
     *
     * @param particle The particle instance to render.
     * @param g2d The graphics context.
     * @param xLevelOffset The horizontal camera offset of the level.
     * @param yLevelOffset The vertical camera offset of the level.
     */
    void render(DustParticle particle, Graphics2D g2d, int xLevelOffset, int yLevelOffset);
}