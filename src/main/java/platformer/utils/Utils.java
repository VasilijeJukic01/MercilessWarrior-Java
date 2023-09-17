package platformer.utils;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.Direction;
import platformer.model.gameObjects.projectiles.LightningBall;
import platformer.model.levels.Level;
import platformer.model.gameObjects.projectiles.Projectile;

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
    // Importing image: [(w, h) = (-1, -1) Original Size]
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

    public BufferedImage flipImage(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(w, h, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.drawImage(src, w, 0, -w, h, null);
        graphics2D.dispose();
        return dest;
    }

    public BufferedImage resizeImage(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImg;
    }

    // Checkers
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

    public double getYPosOnTheCeil(Rectangle2D.Double hitBox, double airSpeed) {
        int currentTile = (int)(hitBox.y / TILES_SIZE+1);
        if (airSpeed > 0) {
            // Fall
            int tileY = (currentTile * TILES_SIZE);
            int offsetY = (int)(TILES_SIZE - hitBox.height);
            return tileY + offsetY - 1;
        }
        else {
            // Jump
            return hitBox.y;
        }
    }

    public boolean isEntityOnFloor(Rectangle2D.Double hitBox, int[][] levelData) {
        if (isSolid(hitBox.x, hitBox.y+hitBox.height+2, levelData)) return true;
        return (isSolid(hitBox.x+hitBox.width, hitBox.y+hitBox.height+2, levelData));
    }

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

    public boolean isSightClear(int[][] levelData, Rectangle2D.Double enemyHitBox, Rectangle2D.Double playerHitBox, int yTileEnemy) {
        int xTileEnemy = (int)(enemyHitBox.x / TILES_SIZE);
        int xTilePlayer = (int)(playerHitBox.x / TILES_SIZE);
        int xTilePlayerRight = xTilePlayer+1;
        if (xTileEnemy > xTilePlayer) return areAllTilesWalkable(xTilePlayerRight, xTileEnemy, yTileEnemy, levelData);
        else return areAllTilesWalkable(xTileEnemy, xTilePlayer, yTileEnemy, levelData);
    }

    public boolean canLauncherSeePlayer(int[][] levelData, Rectangle2D.Double playerHitBox, Rectangle2D.Double launcherHitBox, int yTile) {
        int xTileEnemy = (int)(launcherHitBox.x / TILES_SIZE);
        int xTilePlayer = (int)(playerHitBox.x / TILES_SIZE);
        if (xTileEnemy > xTilePlayer) return areAllTilesClear(xTilePlayer, xTileEnemy, yTile, levelData);
        else return areAllTilesClear(xTileEnemy, xTilePlayer, yTile, levelData);
    }

    public boolean areAllTilesWalkable(int xStart, int xEnd, int y, int[][] levelData) {
        if (!areAllTilesClear(xStart, xEnd, y, levelData)) return false;
        for (int i = 0; i < xEnd-xStart; i++) {
            if (!isTileSolid(xStart+i, y+1, levelData)) return false;
        }
        return true;
    }

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

    public <T> List<T> getAllItems(Map<?, List<T>> itemMap) {
        List<T> allItems = new ArrayList<>();
        itemMap.values().forEach(allItems::addAll);
        return allItems;
    }

}
