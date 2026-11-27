package platformer.physics;

import platformer.model.entities.Direction;
import platformer.model.projectiles.types.GoblinBomb;
import platformer.model.projectiles.types.LightningBall;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

/**
 * A dedicated class for handling collision detection against the static level geometry.
 * This class centralizes all the logic for checking whether an entity can move to a certain position, is on the floor, touching a wall, or has a clear line of sight.
 */
public final class CollisionDetector {

    private CollisionDetector() {}

    /**
     * Checks if a hitbox can occupy a given position without colliding with solid tiles.
     * Checks all 4 corners as well as intermediate vertical and horizontal tile spans so entities taller or wider than 1 tile cannot clip through thin walls or pillars.
     */
    public static boolean canMoveHere(double x, double y, double width, double height, int[][] levelData) {
        if (isSolid(x, y, levelData)) return false;
        if (isSolid(x + width, y + height, levelData)) return false;
        if (isSolid(x + width, y, levelData)) return false;
        if (isSolid(x, y + height, levelData)) return false;
        if (isSolidBetweenVertical(x + width, y, y + height, levelData)) return false;
        if (isSolidBetweenVertical(x, y, y + height, levelData)) return false;
        if (isSolidBetweenHorizontal(x, x + width, y, levelData)) return false;
        if (isSolidBetweenHorizontal(x, x + width, y + height, levelData)) return false;
        return true;
    }

    /**
     * Determines if a specific point in the world is inside a solid tile.
     * Also performs bounds checking to treat areas outside the level as solid.
     *
     * @param x         The world x-coordinate.
     * @param y         The world y-coordinate.
     * @param levelData The level's tilemap.
     * @return {@code true} if the point is solid or out of bounds, {@code false} otherwise.
     */
    public static boolean isSolid(double x, double y, int[][] levelData) {
        int levelWidth = levelData.length * TILES_SIZE;
        int levelHeight = levelData[0].length * TILES_SIZE;

        if (x < 0 || x >= levelWidth) return true;
        if (y < 0 || y >= levelHeight) return true;

        int xIndex = (int) (x / TILES_SIZE);
        int yIndex = (int) (y / TILES_SIZE);

        return isTileSolid(xIndex, yIndex, levelData);
    }

    private static boolean isSolidBetweenVertical(double x, double yA, double yB, int[][] levelData) {
        int xIndex = (int) (x / TILES_SIZE);
        if (xIndex < 0 || xIndex >= levelData.length) return true;

        int yAIndex = Math.max(0, (int) (yA / TILES_SIZE));
        int yBIndex = Math.min(levelData[0].length - 1, (int) (yB / TILES_SIZE));
        for (int i = yAIndex; i <= yBIndex; i++) {
            int value = levelData[xIndex][i];
            if (value > EMPTY_TILE && value < MAX_TILE_VALUE) return true;
        }
        return false;
    }

    private static boolean isSolidBetweenHorizontal(double xA, double xB, double y, int[][] levelData) {
        int yIndex = (int) (y / TILES_SIZE);
        if (yIndex < 0 || yIndex >= levelData[0].length) return true;

        int xAIndex = Math.max(0, (int) (xA / TILES_SIZE));
        int xBIndex = Math.min(levelData.length - 1, (int) (xB / TILES_SIZE));
        for (int i = xAIndex; i <= xBIndex; i++) {
            int value = levelData[i][yIndex];
            if (value > EMPTY_TILE && value < MAX_TILE_VALUE) return true;
        }
        return false;
    }

    /**
     * Checks if a specific tile in the level data is solid based on its value.
     *
     * @param xTile     The x-index of the tile.
     * @param yTile     The y-index of the tile.
     * @param levelData The level's tilemap.
     * @return {@code true} if the tile's value represents a solid block, {@code false} otherwise.
     */
    public static boolean isTileSolid(int xTile, int yTile, int[][] levelData) {
        if (xTile < 0 || xTile >= levelData.length || yTile < 0 || yTile >= levelData[0].length) {
            return true;
        }
        int value = levelData[xTile][yTile];
        if (value == EMPTY_TILE) return false;
        return (value >= 0 && value < MAX_TILE_VALUE);
    }

    /**
     * Calculates the corrected X position flush with the collided wall tile for any hitbox width.
     */
    public static double getXPosOnTheWall(Rectangle2D.Double hitBox, double dx) {
        if (dx > 0) {
            int rightWallTile = (int) Math.floor((hitBox.x + hitBox.width + dx) / TILES_SIZE);
            return rightWallTile * TILES_SIZE - hitBox.width - 1;
        }
        else if (dx < 0) {
            int leftWallTile = (int) Math.floor((hitBox.x + dx) / TILES_SIZE);
            return (leftWallTile + 1) * TILES_SIZE;
        }
        return hitBox.x;
    }

    /**
     * Calculates the corrected Y position flush with the floor (when falling) or ceiling (when rising).
     */
    public static double getYPosOnTheCeil(Rectangle2D.Double hitBox, double airSpeed) {
        if (airSpeed > 0) {
            int floorTile = (int) Math.floor((hitBox.y + hitBox.height + airSpeed + 1) / TILES_SIZE);
            return floorTile * TILES_SIZE - hitBox.height - 1;
        }
        else if (airSpeed < 0) {
            int ceilTile = (int) Math.floor((hitBox.y + airSpeed) / TILES_SIZE);
            return (ceilTile + 1) * TILES_SIZE;
        }
        return hitBox.y;
    }

    /**
     * Finds the Y-coordinate of the ground directly below a given point.
     * It "raycasts" downwards until it hits a solid tile.
     *
     * @param x The x-coordinate to start the search from.
     * @param y The y-coordinate to start the search from.
     * @param levelData The level's collision data.
     * @return The Y-coordinate of the top of the ground tile. Returns the initial y if no ground is found below.
     */
    public static double getGroundY(double x, double y, int[][] levelData) {
        int currentTileY = Math.max(0, (int) (y / TILES_SIZE));
        int tileX = (int) (x / TILES_SIZE);
        if (tileX < 0 || tileX >= levelData.length) return y;
        for (int i = currentTileY; i < levelData[0].length; i++) {
            if (isTileSolid(tileX, i, levelData)) return i * TILES_SIZE;
        }
        return y;
    }

    /**
     * Checks if an entity is currently standing on a solid floor.
     * It checks the points just below the two bottom corners of the hitbox.
     * If any of the tiles are solid, the entity is considered to be on the floor.
     *
     * @param hitBox    The hitbox of the entity.
     * @param levelData The level's tilemap.
     * @return {@code true} if the entity is on the floor, {@code false} otherwise.
     */
    public static boolean isEntityOnFloor(Rectangle2D.Double hitBox, int[][] levelData) {
        if (isSolid(hitBox.x, hitBox.y + hitBox.height + 2, levelData)) return true;
        if (isSolid(hitBox.x + hitBox.width, hitBox.y + hitBox.height + 2, levelData)) return true;
        return hitBox.width > TILES_SIZE && isSolidBetweenHorizontal(hitBox.x, hitBox.x + hitBox.width, hitBox.y + hitBox.height + 2, levelData);
    }

    /**
     * Detects if the player's center of gravity is off the edge of a cliff.
     * If they are teetering, it returns a velocity to gently slide them off.
     */
    public static double getLedgeSlip(Rectangle2D.Double hitBox, int[][] levelData) {
        boolean centerSolid = isSolid(hitBox.getCenterX(), hitBox.y + hitBox.height + 2, levelData);
        if (centerSolid) return 0;
        boolean leftSolid = isSolid(hitBox.x, hitBox.y + hitBox.height + 2, levelData);
        boolean rightSolid = isSolid(hitBox.x + hitBox.width, hitBox.y + hitBox.height + 2, levelData);
        if (leftSolid && !rightSolid) return 1.0 * SCALE;
        if (rightSolid && !leftSolid) return -1.0 * SCALE;
        return 0;
    }

    /**
     * Checks if there is a solid floor ahead of an entity, used for basic AI pathing.
     * It checks the solidity of the tiles at the corners of the entity's hitbox at the new location.
     * If any of the tiles are solid, the floor is considered to be present.
     *
     * @param hitBox      The hitbox of the entity.
     * @param xSpeed      The horizontal speed of the entity.
     * @param levelData   The level's tilemap.
     * @param direction   The direction the entity is moving.
     * @return {@code true} if there is a floor tile ahead, {@code false} if there is a gap.
     */
    public static boolean isFloor(Rectangle2D.Double hitBox, double xSpeed, int[][] levelData, Direction direction) {
        if (direction == Direction.LEFT) return isSolid(hitBox.x + xSpeed, hitBox.y + hitBox.height + 2, levelData);
        else return isSolid(hitBox.x + xSpeed + hitBox.width, hitBox.y + hitBox.height + 2, levelData);
    }

    /**
     * Determines if the line of sight between two entities is clear of solid tiles.
     * Assumes a check on a horizontal plane (same Y tile).
     *
     * @param levelData    The level's tilemap.
     * @param firstHitbox  The hitbox of the first entity (e.g., enemy).
     * @param secondHitbox The hitbox of the second entity (e.g., player).
     * @param yTile        The y-tile index to perform the check on.
     * @return {@code true} if the sight is clear, {@code false} if blocked by a solid tile.
     */
    public static boolean isSightClear(int[][] levelData, Rectangle2D.Double firstHitbox, Rectangle2D.Double secondHitbox, int yTile) {
        int xTileEnemy = (int) (firstHitbox.x / TILES_SIZE);
        int xTilePlayer = (int) (secondHitbox.x / TILES_SIZE);
        int xTilePlayerRight = xTilePlayer + 1;
        if (xTileEnemy > xTilePlayer) return areAllTilesWalkable(xTilePlayerRight, xTileEnemy, yTile, levelData);
        else return areAllTilesWalkable(xTileEnemy, xTilePlayer, yTile, levelData);
    }

    /**
     * Checks if all tiles in a horizontal range are walkable (i.e., have a solid tile directly below them).
     * Used by AI to determine if a path is safe to walk on and won't lead off a cliff.
     *
     * @param xStart    The starting x-tile index.
     * @param xEnd      The ending x-tile index.
     * @param y         The y-tile index of the path.
     * @param levelData The level's tilemap.
     * @return {@code true} if the entire path is walkable, {@code false} otherwise.
     */
    public static boolean areAllTilesWalkable(int xStart, int xEnd, int y, int[][] levelData) {
        if (!areAllTilesClear(xStart, xEnd, y, levelData)) return false;
        for (int i = 0; i < xEnd - xStart; i++) {
            if (!isTileSolid(xStart + i, y + 1, levelData)) return false;
        }
        return true;
    }

    /**
     * Checks if all tiles in a horizontal range are clear of solid obstacles.
     *
     * @param xStart    The starting x-tile index.
     * @param xEnd      The ending x-tile index.
     * @param y         The y-tile index.
     * @param levelData The level's tilemap.
     * @return {@code true} if there are no solid tiles in the path, {@code false} otherwise.
     */
    private static boolean areAllTilesClear(int xStart, int xEnd, int y, int[][] levelData) {
        for (int i = 0; i < xEnd - xStart; i++) {
            if (isTileSolid(xStart + i, y, levelData)) return false;
        }
        return true;
    }

    /**
     * This method checks if the entity is touching a wall.
     * It calculates the x-coordinate of the tile that the entity's hitbox is touching.
     * If the entity is moving to the left, it checks if the entity's x-coordinate is within 2 pixels of the right edge of the tile.
     * If the entity is moving to the right, it checks if the right edge of the entity's hitbox is within 2 pixels of the left edge of the tile.
     *
     * @param hitBox    The hitbox of the entity.
     * @param direction The direction to check for a wall (LEFT or RIGHT).
     * @return {@code true} if touching a wall in the specified direction, {@code false} otherwise.
     */
    public static boolean isTouchingWall(Rectangle2D.Double hitBox, Direction direction) {
        int x = (int) hitBox.x;
        int xTile = (int) (hitBox.x / TILES_SIZE);
        if (direction == Direction.LEFT) {
            int xTileLeft = xTile - 1;
            return (x >= (xTileLeft * TILES_SIZE + TILES_SIZE) && x <= (xTileLeft * TILES_SIZE + TILES_SIZE + 2));
        }
        else if (direction == Direction.RIGHT) {
            int xTileRight = xTile + 1;
            double dp = hitBox.x + hitBox.width - xTileRight * TILES_SIZE;
            return (dp >= -2 && dp <= 2);
        }
        return false;
    }

    /**
     * Checks if an entity is on a wall (for wall sliding/jumping).
     * It verifies that there is a solid tile adjacent to the entity and that the entity is physically touching it.
     *
     * @param hitBox    The hitbox of the entity.
     * @param levelData The level's tilemap.
     * @param direction The direction to check (LEFT or RIGHT).
     * @return {@code true} if on a wall, {@code false} otherwise.
     */
    public static boolean isOnWall(Rectangle2D.Double hitBox, int[][] levelData, Direction direction) {
        int xTile = (int) (hitBox.x / TILES_SIZE);
        int yTile = (int) (hitBox.y / TILES_SIZE);
        if (direction == Direction.LEFT) {
            if (xTile - 1 < 0) return false;
            return isTileSolid(xTile - 1, yTile, levelData) && isTileSolid(xTile - 1, yTile + 1, levelData) && isTouchingWall(hitBox, Direction.LEFT);
        }
        else if (direction == Direction.RIGHT) {
            if (xTile + 1 >= levelData.length) return false;
            return isTileSolid(xTile + 1, yTile, levelData) && isTileSolid(xTile + 1, yTile + 1, levelData) && isTouchingWall(hitBox, Direction.RIGHT);
        }
        return false;
    }

    /**
     * Checks if a projectile's hitbox intersects with a solid tile in the level.
     * It checks the bottom-right corner of the projectile's hitbox to determine if it is touching a solid tile.
     *
     * @param lvlData    The level's tilemap.
     * @param projectile The projectile to check.
     * @return {@code true} if the projectile is hitting a solid tile, {@code false} otherwise.
     */
    public static boolean isProjectileHitLevel(int[][] lvlData, Projectile projectile) {
        if (projectile instanceof GoblinBomb) return false;
        Rectangle bounds = projectile.getShapeBounds().getBounds();
        if (projectile instanceof LightningBall) {
            double centerX = bounds.getCenterX(), centerY = bounds.getCenterY();
            if (centerX <= TILES_SIZE * 2 || centerX >= (lvlData.length - 2) * TILES_SIZE) {
                return isSolid(centerX, centerY, lvlData);
            }
            return centerY >= (lvlData[0].length - 1) * TILES_SIZE;
        }
        return isSolid(bounds.x + bounds.width, bounds.y + bounds.height, lvlData);
    }

    /**
     * This method checks if a launcher can see the entity.
     * It checks the solidity of the tiles between the launcher and the entity.
     * If any of the tiles are solid, the launcher is considered to be unable to see the entity.
     *
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @param hitbox The hitbox of the entity.
     * @param launcherHitBox The hitbox of the launcher.
     * @param yTile The y-coordinate of the launcher's tile.
     * @return true if the launcher can see the entity, false otherwise.
     */
    public static boolean canLauncherSeeEntity(int[][] levelData, Rectangle2D.Double hitbox, Rectangle2D.Double launcherHitBox, int yTile) {
        int xTileLauncher = (int) (launcherHitBox.x / TILES_SIZE);
        int xTileEntity = (int) (hitbox.x / TILES_SIZE);
        if (xTileLauncher > xTileEntity) return areAllTilesClear(xTileEntity, xTileLauncher, yTile, levelData);
        else return areAllTilesClear(xTileLauncher, xTileEntity, yTile, levelData);
    }
}