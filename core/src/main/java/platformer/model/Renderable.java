package platformer.model;

import java.awt.*;

/**
 * The Renderable interface provides methods for rendering game objects.
 *
 * @param <G> the type of the graphics object used for rendering
 */
public interface Renderable<G> {

    /**
     * Renders the game object at the specified level offset.
     *
     * @param g the graphics object used for rendering
     * @param xLevelOffset the x-coordinate of the level offset
     * @param yLevelOffset the y-coordinate of the level offset
     */
    void render(G g, int xLevelOffset, int yLevelOffset);

    /**
     * Renders the hitbox of the game object at the specified level offset with the specified color.
     *
     * @param g the graphics object used for rendering
     * @param xLevelOffset the x-coordinate of the level offset
     * @param yLevelOffset the y-coordinate of the level offset
     * @param color the color of the hitbox
     */
    void renderHitBox(G g, int xLevelOffset, int yLevelOffset, Color color);

}
