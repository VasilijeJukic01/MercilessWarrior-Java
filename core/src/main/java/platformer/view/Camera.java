package platformer.view;

import platformer.model.levels.Level;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

/**
 * Manages the game's viewport, including its position, offsets, and smooth following of a target.
 * The camera ensures that the view stays within the boundaries of the current level.
 */
public class Camera {

    private double cameraX, cameraY;
    private int xLevelOffset, yLevelOffset;
    private int xMaxLevelOffset, yMaxLevelOffset;

    public Camera(double initialX, double initialY) {
        this.cameraX = initialX;
        this.cameraY = initialY;
    }

    /**
     * Updates the camera's position to smoothly follow a target.
     * The camera's final position is clamped to ensure it doesn't show areas outside the level bounds.
     *
     * @param target The hitbox of the entity the camera should follow.
     */
    public void update(Rectangle2D.Double target) {
        float targetX = (float)target.x - (GAME_WIDTH / 2.0f);
        float targetY = (float)target.y - (GAME_HEIGHT / 2.0f);

        cameraX += (targetX - cameraX) * CAMERA_LERP_FACTOR_X;
        cameraY += (targetY - cameraY) * CAMERA_LERP_FACTOR_Y;

        xLevelOffset = (int)cameraX;
        yLevelOffset = (int)cameraY;

        xLevelOffset = Math.max(0, Math.min(xLevelOffset, xMaxLevelOffset));
        yLevelOffset = Math.max(0, Math.min(yLevelOffset, yMaxLevelOffset));
    }

    /**
     * Updates the maximum scrollable boundaries based on the current level's dimensions.
     * This should be called every time a new level is loaded.
     *
     * @param level The current level being played.
     */
    public void updateLevelBounds(Level level) {
        this.xMaxLevelOffset = level.getXMaxLevelOffset();
        this.yMaxLevelOffset = level.getYMaxLevelOffset();
    }

    // Getters
    public int getXOffset() {
        return xLevelOffset;
    }

    public int getYOffset() {
        return yLevelOffset;
    }
}