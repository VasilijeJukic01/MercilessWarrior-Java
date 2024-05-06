package platformer.model;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * The AdvancedRenderable interface provides additional methods for rendering game objects with animations.
 *
 * @param <G> the type of the graphics object used for rendering
 */
public interface AdvancedRenderable<G> {

        /**
         * Renders the game object at the specified level offset using the provided animations.
         *
         * @param g the graphics object used for rendering
         * @param xLevelOffset the x-coordinate of the level offset
         * @param yLevelOffset the y-coordinate of the level offset
         * @param animations the array of BufferedImages representing the animations of the game object
         */
        void render(G g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations);

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
