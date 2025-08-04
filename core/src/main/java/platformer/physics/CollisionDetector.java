package platformer.physics;

import platformer.model.entities.Direction;
import platformer.model.projectiles.types.LightningBall;
import platformer.model.projectiles.Projectile;
import platformer.model.levels.Level;

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
     * It checks the four corners of the hitbox. If any corner is inside a solid tile, movement is blocked.
     *
     * @param x         The target x-coordinate of the hitbox's top-left corner.
     * @param y         The target y-coordinate of the hitbox's top-left corner.
     * @param width     The width of the hitbox.
     * @param height    The height of the hitbox.
     * @param levelData The 2D integer array representing the level's tilemap.
     * @return {@code true} if the position is clear, {@code false} if it collides with a solid tile.
     */
    public static boolean canMoveHere(double x, double y, double width, double height, int[][] levelData) {
        if (isSolid(x, y, levelData)) return false;
        if (isSolid(x+width, y+height, levelData)) return false;
        if (isSolid(x+width, y, levelData)) return false;
        if (isSolid(x, y+height, levelData)) return false;
        if (isSolidBetween(x+width, y, y+height, levelData)) return false;
        if (isSolidBetween(x, y, y+height, levelData)) return false;
        return true;
    }

    /**
     * Internal helper to determine if a specific point in the world is inside a solid tile.
     * Also performs bounds checking to treat areas outside the level as solid.
     *
     * @param x         The world x-coordinate.
     * @param y         The world y-coordinate.
     * @param levelData The level's tilemap.
     * @return {@code true} if the point is solid or out of bounds, {@code false} otherwise.
     */
    private static boolean isSolid(double x, double y, int[][] levelData) {
        int levelWidth = levelData.length * TILES_SIZE;
        int levelHeight = levelData[0].length * TILES_SIZE;

        if (x < 0 || x >= levelWidth) return true;
        if (y < 0 || y >= levelHeight) return true;

        int xIndex = (int) (x / TILES_SIZE);
        int yIndex = (int) (y / TILES_SIZE);

        return isTileSolid(xIndex, yIndex, levelData);
    }

    /**
     * Checks for any solid tiles along a vertical line at a given x-coordinate.
     * This is essential for preventing entities from clipping through vertical walls when moving horizontally.
     *
     * @param x         The world x-coordinate to check along.
     * @param yA    The starting world y-coordinate of the line.
     * @param yB      The ending world y-coordinate of the line.
     * @param levelData The level's tilemap.
     * @return {@code true} if a solid tile is found along the line, {@code false} otherwise.
     */
    private static boolean isSolidBetween(double x, double yA, double yB, int[][] levelData) {
        int xIndex = (int)(x / TILES_SIZE);
        int yAIndex = (int)(yA / TILES_SIZE);
        int yBIndex = (int)(yB / TILES_SIZE);

        for (int i = yAIndex; i <= yBIndex; i++) {
            int value = levelData[xIndex][i];
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
        int value = levelData[xTile][yTile];
        if (value == EMPTY_TILE) return false;
        return (value >= 0 && value < MAX_TILE_VALUE);
    }

    /**
     * Calculates the corrected X position for an entity that has collided with a wall.
     * Prevents the entity from moving inside the wall tile.
     *
     * @param hitBox The hitbox of the entity.
     * @param dx     The horizontal velocity or movement delta.
     * @return The new x-coordinate, flush with the wall.
     */
    public static double getXPosOnTheWall(Rectangle2D.Double hitBox, double dx) {
        int currentTile = (int) (hitBox.x / TILES_SIZE);
        if (dx > 0) {
            int tileXPos = currentTile * TILES_SIZE;
            int xOffset = (int) (TILES_SIZE - hitBox.width);
            return tileXPos + xOffset - 1;
        }
        else {
            return hitBox.x;
        }
    }

    /**
     * This method calculates the y-coordinate of the entity's position on the ceiling.
     * If the entity is falling, it calculates the y-coordinate of the bottommost tile that the entity's hitbox is touching.
     * If the entity is jumping, it returns the current y-coordinate of the entity.
     *
     * @param hitBox The hitbox of the entity.
     * @param airSpeed The vertical speed of the entity.
     * @return The y-coordinate of the entity's position on the ceiling.
     */
    public static double getYPosOnTheCeil(Rectangle2D.Double hitBox, double airSpeed) {
        int currentTile = (int)(hitBox.y / TILES_SIZE+1);
        // Fall
        if (airSpeed > 0) {
            int tileY = (currentTile * TILES_SIZE);
            int offsetY = (int)(TILES_SIZE - hitBox.height);
            return tileY + offsetY - 1;
        }
        // Jump
        else {
            return hitBox.y;
        }
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
        int currentTileY = (int) (y / TILES_SIZE);
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
        if (isSolid(hitBox.x, hitBox.y+hitBox.height+2, levelData)) return true;
        return (isSolid(hitBox.x+hitBox.width, hitBox.y+hitBox.height+2, levelData));
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
        if (direction == Direction.LEFT) return isSolid(hitBox.x + xSpeed, hitBox.y + hitBox.height+2, levelData);
        else return isSolid(hitBox.x + xSpeed + hitBox.width, hitBox.y + hitBox.height+2, levelData);
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
        int xTileEnemy = (int)(firstHitbox.x / TILES_SIZE);
        int xTilePlayer = (int)(secondHitbox.x / TILES_SIZE);
        int xTilePlayerRight = xTilePlayer+1;
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
        for (int i = 0; i < xEnd-xStart; i++) {
            if (!isTileSolid(xStart+i, y+1, levelData)) return false;
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
        for (int i = 0; i < xEnd-xStart; i++) {
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
        int x = (int)hitBox.x;
        int xTile = (int)(hitBox.x / TILES_SIZE);
        if (direction == Direction.LEFT) {
            int xTileLeft = xTile - 1;
            return (x >= (xTileLeft*TILES_SIZE+TILES_SIZE) && x <= (xTileLeft*TILES_SIZE+TILES_SIZE+2));
        }
        else if (direction == Direction.RIGHT) {
            int xTileRight = xTile + 1;
            double dp = hitBox.x+hitBox.width - xTileRight*TILES_SIZE;
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
        int xTile = (int)(hitBox.x / TILES_SIZE);
        int yTile = (int)(hitBox.y / TILES_SIZE);
        if (direction == Direction.LEFT) {
            if (xTile-1 < 0) return true;
            return isTileSolid(xTile-1, yTile, levelData) && isTileSolid(xTile-1, yTile+1, levelData) && isTouchingWall(hitBox, Direction.LEFT);
        }
        else if (direction == Direction.RIGHT) {
            if (xTile+1 >= levelData.length) return true;
            return isTileSolid(xTile+1, yTile, levelData) && isTileSolid(xTile+1, yTile+1, levelData) && isTouchingWall(hitBox, Direction.RIGHT);
        }
        return false;
    }

    /**
     * Checks if an entity's hitbox overlaps with a level exit tile.
     * It checks the type of the tile that the entity's hitbox is touching.
     * If the tile is an exit tile, the method returns the type of the exit.
     *
     * @param level  The current Level object.
     * @param hitBox The hitbox of the entity.
     * @return The constant representing the exit type (e.g., RIGHT_EXIT), or 0 if not on an exit.
     */
    public static int isEntityOnExit(Level level, Rectangle2D.Double hitBox) {
        int xTile = (int) (hitBox.x / TILES_SIZE);
        int yTile = (int) (hitBox.y / TILES_SIZE);
        int xTileRight = (int) ((hitBox.x + hitBox.width) / TILES_SIZE);

        if (level.getDecoSpriteIndex(xTile, yTile) == RIGHT_EXIT || level.getDecoSpriteIndex(xTileRight, yTile) == RIGHT_EXIT) return RIGHT_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile) == LEFT_EXIT) return LEFT_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile) == UPPER_EXIT) return UPPER_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile + 1) == BOTTOM_EXIT) return BOTTOM_EXIT;
        return 0;
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
        double wid = projectile.getShapeBounds().getBounds().width;
        double hei = projectile.getShapeBounds().getBounds().height;
        if (projectile instanceof LightningBall) hei = ARROW_HEI;
        return isSolid(projectile.getShapeBounds().getBounds().x + wid, projectile.getShapeBounds().getBounds().y + hei, lvlData);
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
        int xTileLauncher = (int)(launcherHitBox.x / TILES_SIZE);
        int xTileEntity = (int)(hitbox.x / TILES_SIZE);
        if (xTileLauncher > xTileEntity) return areAllTilesClear(xTileEntity, xTileLauncher, yTile, levelData);
        else return areAllTilesClear(xTileLauncher, xTileEntity, yTile, levelData);
    }
}