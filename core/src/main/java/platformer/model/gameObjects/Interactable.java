package platformer.model.gameObjects;

import platformer.model.entities.player.Player;

import java.awt.*;

/**
 * An interface for GameObject that the Player can interact with by intersection.
 * This allows objects to define their own behavior when a player enters, stays within, or exits their hitbox.
 */
public interface Interactable {

    /**
     * Called once when the player's hitbox first enters the object's hitbox.
     * Ideal for setting initial states.
     *
     * @param player The player who entered the intersection.
     */
    void onEnter(Player player);

    /**
     * Called continuously every frame while the player's hitbox remains inside the object's hitbox.
     * Ideal for continuous effects.
     *
     * @param player The player currently intersecting.
     */
    void onIntersect(Player player);

    /**
     * Called once when the player's hitbox leaves the object's hitbox.
     * Ideal for resetting states.
     *
     * @param player The player who exited the intersection.
     */
    void onExit(Player player);

    /**
     * Returns a unique identifier or prompt text for this interaction.
     *
     * @return A string identifier or null if no prompt is needed.
     */
    String getInteractionPrompt();

    /**
     * Renders any interaction-related UI elements, such as prompts or indicators.
     * This method is optional and can be left empty if no UI is needed.
     *
     * @param g             The Graphics context to draw on.
     * @param xLevelOffset  The x offset of the level for rendering.
     * @param yLevelOffset  The y offset of the level for rendering.
     */
    default void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) { }
}