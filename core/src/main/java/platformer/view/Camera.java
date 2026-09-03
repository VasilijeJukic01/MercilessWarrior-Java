package platformer.view;

import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
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

    private double focusX, focusY;
    private static final double SMOOTH_SPEED_X = 0.02;
    private static final double SMOOTH_SPEED_Y = 0.03;

    public Camera(double initialX, double initialY) {
        this.cameraX = initialX;
        this.cameraY = initialY;
    }

    /**
     * Updates the camera's position to smoothly follow a target.
     * The camera's final position is clamped to ensure it doesn't show areas outside the level bounds.
     *
     * @param player The player the camera should follow.
     */
    public void update(Player player) {
        Rectangle2D.Double target = player.getHitBox();

        if (target.x < focusX - DEADZONE_X) focusX = target.x + DEADZONE_X;
        else if (target.x > focusX + DEADZONE_X) focusX = target.x - DEADZONE_X;
        if (target.y < focusY - DEADZONE_Y) focusY = target.y + DEADZONE_Y;
        else if (target.y > focusY + DEADZONE_Y) focusY = target.y - DEADZONE_Y;

        double targetX = focusX - (GAME_WIDTH / 2.0);
        double targetY = focusY - (GAME_HEIGHT / 2.0);
        if (player.checkAction(PlayerAction.LOOK_DOWN)) {
            targetY += CAMERA_LOOK_DOWN_OFFSET;
        }

        cameraX += (targetX - cameraX) * SMOOTH_SPEED_X;
        cameraY += (targetY - cameraY) * SMOOTH_SPEED_Y;

        xLevelOffset = (int)Math.round(cameraX);
        yLevelOffset = (int)Math.round(cameraY);
        xLevelOffset = Math.max(0, Math.min(xLevelOffset, xMaxLevelOffset));
        yLevelOffset = Math.max(0, Math.min(yLevelOffset, yMaxLevelOffset));
    }

    /**
     * Instantly snaps the camera to the player.
     * Used when teleporting or loading a new level to prevent the camera from aggressively sweeping across the entire map.
     */
    public void snapToPlayer(Player player) {
        Rectangle2D.Double target = player.getHitBox();
        this.focusX = target.x;
        this.focusY = target.y;
        this.cameraX = target.x - (GAME_WIDTH / 2.0);
        this.cameraY = target.y - (GAME_HEIGHT / 2.0);
        xLevelOffset = Math.max(0, Math.min((int)cameraX, xMaxLevelOffset));
        yLevelOffset = Math.max(0, Math.min((int)cameraY, yMaxLevelOffset));
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