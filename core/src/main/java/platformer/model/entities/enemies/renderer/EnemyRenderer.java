package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Enemy;

import java.awt.*;

/**
 * This is a Strategy interface for rendering different types of enemies in the game.
 * It defines a single method, render, which is used to draw an enemy of type T on the screen.
 *
 * @param <T> The specific type of enemy to render. This type must extend from the Enemy class.
 */
public interface EnemyRenderer<T extends Enemy> {

    /**
     * Renders an enemy of type T on the screen.
     *
     * @param g The graphics object to draw on.
     * @param enemy The enemy of type T to render.
     * @param xLevelOffset The x offset for rendering.
     * @param yLevelOffset The y offset for rendering.
     */
    void render(Graphics g, T enemy, int xLevelOffset, int yLevelOffset);

}
