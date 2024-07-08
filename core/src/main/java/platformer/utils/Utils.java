package platformer.utils;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Direction;
import platformer.model.gameObjects.projectiles.LightningBall;
import platformer.model.gameObjects.projectiles.Projectile;
import platformer.model.levels.Level;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static platformer.constants.Constants.*;

public class Utils {

    private static volatile Utils instance = null;

    private Utils() {}

    public static Utils getInstance() {
        if (instance == null) {
            synchronized (Utils.class) {
                if (instance == null) {
                    instance = new Utils();
                }
            }
        }
        return instance;
    }

    // Image operations
    /**
     * This method is used to import an image from a given path and resize it to the specified width and height.
     * If the width and height are both -1, the original size of the image is preserved.
     *
     * @param name The path of the image to be imported.
     * @param w The desired width of the image.
     * @param h The desired height of the image.
     * @return The imported image, resized to the specified dimensions. If an error occurs during import, null is returned.
     */
    public BufferedImage importImage(String name, int w, int h) {
        try {
            BufferedImage image = ImageIO.read(Objects.requireNonNull(getClass().getResource(name)));
            if (w == -1 && h == -1) return image;
            return resizeImage(image, w, h);

        } catch (Exception e) {
            if (name.contains("/levels/level")) return null;
            Logger.getInstance().notify("Importing image failed. "+"Name: " + name + " (w, h) = (" + w + ", " + h + ")", Message.ERROR);
        }
        return null;
    }

    /**
     * This method is used to flip an image horizontally.
     *
     * @param src The image to be flipped.
     * @return The flipped image.
     */
    public BufferedImage flipImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.drawImage(src, w, 0, -w, h, null);
        graphics2D.dispose();
        return dest;
    }

    /**
     * This method is used to resize an image to the specified width and height.
     *
     * @param img The image to be resized.
     * @param newW The desired width of the image.
     * @param newH The desired height of the image.
     * @return The resized image.
     */
    public BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImg;
    }

    // Checkers
    /**
     * This method checks if a given entity can move to a specified location in the game level.
     * It checks the solidity of the tiles at the corners of the entity's hitbox at the new location.
     * If any of the tiles are solid, the entity cannot move to the new location.
     *
     * @param x The x-coordinate of the new location.
     * @param y The y-coordinate of the new location.
     * @param width The width of the entity's hitbox.
     * @param height The height of the entity's hitbox.
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @return true if the entity can move to the new location, false otherwise.
     */
    public boolean canMoveHere(double x, double y, double width, double height, int[][] levelData) {
        if (isSolid(x, y, levelData)) return false;
        if (isSolid(x+width, y+height, levelData)) return false;
        if (isSolid(x+width, y, levelData)) return false;
        if (isSolid(x, y+height, levelData)) return false;
        if (isSolidBetween(x+width, y, y+height, levelData)) return false;
        if (isSolidBetween(x, y, y+height, levelData)) return false;
        return true;
    }

    private boolean isSolid(double x, double y, int[][] levelData) {
        int xMaxSize = levelData.length * TILES_SIZE;
        int yMaxSize = levelData[0].length * TILES_SIZE;
        if (x < 0 || x >= xMaxSize) return true;
        if (y < 0 || y >= yMaxSize) return true;

        int xIndex = (int)(x / TILES_SIZE);
        int yIndex = (int)(y / TILES_SIZE);

        return isTileSolid(xIndex, yIndex, levelData);
    }

    public boolean isTileSolid(int xTile, int yTile, int[][] levelData) {
        int value = levelData[xTile][yTile];
        if (value == EMPTY_TILE) return false;
        return (value >= 0 && value < MAX_TILE_VALUE);
    }

    private boolean isSolidBetween(double x, double yA, double yB, int[][] levelData) {
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
     * This method calculates the x-coordinate of the entity's position on the wall.
     * If the entity is moving to the right, it calculates the x-coordinate of the rightmost tile that the entity's hitbox is touching.
     * If the entity is moving to the left, it returns the current x-coordinate of the entity.
     *
     * @param hitBox The hitbox of the entity.
     * @param dx The horizontal speed of the entity.
     * @return The x-coordinate of the entity's position on the wall.
     */
    public double getXPosOnTheWall(Rectangle2D.Double hitBox, double dx) {
        int currentTile = (int)(hitBox.x / TILES_SIZE);
        if (dx > 0) {
            int tileX = (currentTile * TILES_SIZE);
            int offsetX = (int)(TILES_SIZE - hitBox.width);
            return tileX + offsetX - 1;
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
    public double getYPosOnTheCeil(Rectangle2D.Double hitBox, double airSpeed) {
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
     * This method checks if the entity is on the floor.
     * It checks the solidity of the tiles directly below the entity's hitbox.
     * If any of the tiles are solid, the entity is considered to be on the floor.
     *
     * @param hitBox The hitbox of the entity.
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @return true if the entity is on the floor, false otherwise.
     */
    public boolean isEntityOnFloor(Rectangle2D.Double hitBox, int[][] levelData) {
        if (isSolid(hitBox.x, hitBox.y+hitBox.height+2, levelData)) return true;
        return (isSolid(hitBox.x+hitBox.width, hitBox.y+hitBox.height+2, levelData));
    }

    /**
     * This method checks if the floor is present at the specified location in the game level.
     * It checks the solidity of the tiles at the corners of the entity's hitbox at the new location.
     * If any of the tiles are solid, the floor is considered to be present.
     *
     * @param hitBox The hitbox of the entity.
     * @param enemyXSpeed The horizontal speed of the entity.
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @param direction The direction in which the entity is moving.
     * @return true if the floor is present at the new location, false otherwise.
     */
    public boolean isFloor(Rectangle2D.Double hitBox, double enemyXSpeed, int[][] levelData, Direction direction) {
        if (direction == Direction.LEFT) return isSolid(hitBox.x + enemyXSpeed, hitBox.y + hitBox.height+2, levelData);
        else return isSolid(hitBox.x + enemyXSpeed + hitBox.width, hitBox.y + hitBox.height+2, levelData);
    }

    private boolean areAllTilesClear(int xStart, int xEnd, int y, int[][] levelData) {
        for (int i = 0; i < xEnd-xStart; i++) {
            if (isTileSolid(xStart + i, y, levelData)) return false;
        }
        return true;
    }

    /**
     * This method checks if the line of sight between an enemy and the player is clear.
     * It checks the solidity of the tiles between the enemy and the player.
     * If any of the tiles are solid, the line of sight is considered to be blocked.
     *
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @param enemyHitBox The hitbox of the enemy.
     * @param playerHitBox The hitbox of the player.
     * @param yTileEnemy The y-coordinate of the enemy's tile.
     * @return true if the line of sight is clear, false otherwise.
     */
    public boolean isSightClear(int[][] levelData, Rectangle2D.Double enemyHitBox, Rectangle2D.Double playerHitBox, int yTileEnemy) {
        int xTileEnemy = (int)(enemyHitBox.x / TILES_SIZE);
        int xTilePlayer = (int)(playerHitBox.x / TILES_SIZE);
        int xTilePlayerRight = xTilePlayer+1;
        if (xTileEnemy > xTilePlayer) return areAllTilesWalkable(xTilePlayerRight, xTileEnemy, yTileEnemy, levelData);
        else return areAllTilesWalkable(xTileEnemy, xTilePlayer, yTileEnemy, levelData);
    }

    /**
     * This method checks if a launcher can see the player.
     * It checks the solidity of the tiles between the launcher and the player.
     * If any of the tiles are solid, the launcher is considered to be unable to see the player.
     *
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @param playerHitBox The hitbox of the player.
     * @param launcherHitBox The hitbox of the launcher.
     * @param yTile The y-coordinate of the launcher's tile.
     * @return true if the launcher can see the player, false otherwise.
     */
    public boolean canLauncherSeePlayer(int[][] levelData, Rectangle2D.Double playerHitBox, Rectangle2D.Double launcherHitBox, int yTile) {
        int xTileEnemy = (int)(launcherHitBox.x / TILES_SIZE);
        int xTilePlayer = (int)(playerHitBox.x / TILES_SIZE);
        if (xTileEnemy > xTilePlayer) return areAllTilesClear(xTilePlayer, xTileEnemy, yTile, levelData);
        else return areAllTilesClear(xTileEnemy, xTilePlayer, yTile, levelData);
    }

    /**
     * This method checks if all tiles in a specified range are walkable.
     * It checks the solidity of the tiles in the range.
     * If any of the tiles are solid, the tiles are considered to be not walkable.
     *
     * @param xStart The x-coordinate of the start of the range.
     * @param xEnd The x-coordinate of the end of the range.
     * @param y The y-coordinate of the tiles.
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @return true if all tiles in the range are walkable, false otherwise.
     */
    public boolean areAllTilesWalkable(int xStart, int xEnd, int y, int[][] levelData) {
        if (!areAllTilesClear(xStart, xEnd, y, levelData)) return false;
        for (int i = 0; i < xEnd-xStart; i++) {
            if (!isTileSolid(xStart+i, y+1, levelData)) return false;
        }
        return true;
    }

    /**
     * This method checks if the entity is touching a wall.
     * It calculates the x-coordinate of the tile that the entity's hitbox is touching.
     * If the entity is moving to the left, it checks if the entity's x-coordinate is within 2 pixels of the right edge of the tile.
     * If the entity is moving to the right, it checks if the right edge of the entity's hitbox is within 2 pixels of the left edge of the tile.
     *
     * @param hitBox The hitbox of the entity.
     * @param direction The direction in which the entity is moving.
     * @return true if the entity is touching a wall, false otherwise.
     */
    public boolean isTouchingWall(Rectangle2D.Double hitBox, Direction direction) {
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
     * This method checks if the entity is on a wall.
     * It checks the solidity of the tiles directly to the left or right of the entity's hitbox, depending on the direction of movement.
     * If any of the tiles are solid, the entity is considered to be on a wall.
     *
     * @param hitBox The hitbox of the entity.
     * @param levelData The 2D array representing the game level, where each element is a tile.
     * @param direction The direction in which the entity is moving.
     * @return true if the entity is on a wall, false otherwise.
     */
    public boolean isOnWall(Rectangle2D.Double hitBox, int[][] levelData, Direction direction) {
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
     * This method checks if the entity is on an exit tile.
     * It checks the type of the tile that the entity's hitbox is touching.
     * If the tile is an exit tile, the method returns the type of the exit.
     *
     * @param level The game level.
     * @param hitBox The hitbox of the entity.
     * @return The type of the exit if the entity is on an exit tile, 0 otherwise.
     */
    public int isEntityOnExit(Level level, Rectangle2D.Double hitBox) {
        int xTile = (int)(hitBox.x / TILES_SIZE);
        int yTile = (int)(hitBox.y / TILES_SIZE);
        int xTileRight = (int)((hitBox.x + hitBox.width) / TILES_SIZE);

        if (level.getDecoSpriteIndex(xTile, yTile) == RIGHT_EXIT || level.getDecoSpriteIndex(xTileRight, yTile) == RIGHT_EXIT) return RIGHT_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile) == LEFT_EXIT) return LEFT_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile) == UPPER_EXIT) return UPPER_EXIT;
        if (level.getDecoSpriteIndex(xTile, yTile + 1) == BOTTOM_EXIT) return BOTTOM_EXIT;
        return 0;
    }

    public boolean isProjectileHitLevel(int[][] lvlData, Projectile projectile) {
        double wid = projectile.getHitBox().getWidth();
        double hei = projectile.getHitBox().getHeight();
        if (projectile instanceof LightningBall) hei = ARROW_HEI;
        return isSolid(projectile.getHitBox().x + wid, projectile.getHitBox().y + hei, lvlData);
    }

    // Other
    public <T> T[] reverseArray(T[] arr) {
        int start = 0;
        int end = arr.length - 1;

        while (start < end) {
            T temp = arr[start];
            arr[start] = arr[end];
            arr[end] = temp;

            start++;
            end--;
        }

        return arr;
    }

    /**
     * This method retrieves all items from a map where each value is a list of items.
     * It iterates over the values of the map, which are lists, and adds all items from these lists to a new list.
     * The new list, containing all items from the lists in the map, is then returned.
     *
     * @param itemMap The map containing lists of items as values.
     * @return A list containing all items from the lists in the map.
     */
    public <T> List<T> getAllItems(Map<?, List<T>> itemMap) {
        List<T> allItems = new ArrayList<>();
        itemMap.values().forEach(allItems::addAll);
        return allItems;
    }

}
