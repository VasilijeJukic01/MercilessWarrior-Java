package platformer.utils;

import platformer.model.entities.Direction;
import platformer.model.entities.effects.Particle;
import platformer.model.levels.Level;
import platformer.model.objects.projectiles.Projectile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Utils {

    public static Utils instance = null;

    private Utils() {}

    // Image operations
    // Importing image: [(w, h) = (-1, -1) Original Size]
    public BufferedImage importImage(String name, int w, int h) {
        try {
            BufferedImage image = ImageIO.read(new File(name));
            if (w == -1 && h == -1) return image;
            Image temp = image.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            BufferedImage resized = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = resized.createGraphics();
            g2.drawImage(temp, 0, 0, null);
            g2.dispose();
            return resized;

        } catch (Exception e) {
            System.out.println("Error: Importing Image");
            System.out.println("Name: "+name+"(w, h) = ("+w+", "+h+")");
        }
        return null;
    }

    // Rotate image [Angle = {PI/2, PI, 3PI/2}]
    public BufferedImage rotateImage(BufferedImage src, double angle) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((h - w) / 2, (h - w) / 2);
        graphics2D.rotate(angle, h / 2.0, w / 2.0);
        graphics2D.drawRenderedImage(src, null);
        return dest;
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

    public BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage newImg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();
        return newImg;
    }

    // Data gatherer
    public BufferedImage[] getAllLevels(String layer) {
        BufferedImage[] levels = new BufferedImage[3];
        for (int i = 0; i < 3; i++) {
            levels[i] = importImage("src/main/resources/images/levels/level"+(i+1)+"_layer"+layer+".png", -1, -1);
        }
        return levels;
    }

    public int[][] getLevelData(BufferedImage level) {
        int[][] lvlData = new int[level.getWidth()][level.getHeight()];
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getRed();
                if (value >= 49) value = -1;
                if (color.getBlue() == 255 && color.getGreen() == 255) value += 255;   // Value > 255  ->  Different layer
                lvlData[i][j] = value;
            }
        }
        return lvlData;
    }

    // layer = true -> Layer data;  layer = false -> Object data
    public int[][] getDecoData(BufferedImage level, boolean layer) {
        int[][] data = new int[level.getWidth()][level.getHeight()];
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = layer ? color.getGreen() : color.getBlue();
                if ((value >= 40 && !layer) || (value >= 4 && layer)) value = -1;
                data[i][j] = value;
            }
        }
        return data;
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
        if (value == -1) return false;
        return (value >= 0 && value < 49);
    }

    private boolean isSolidBetween(double x, double yA, double yB, int[][] levelData) {
        int xIndex = (int)(x / TILES_SIZE);
        int yAIndex = (int)(yA / TILES_SIZE);
        int yBIndex = (int)(yB / TILES_SIZE);

        for (int i = yAIndex; i <= yBIndex; i++) {
            int value = levelData[xIndex][i];
            if (value > -1 && value < 49) return true;
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

    public int isOnExit(Level level, Rectangle2D.Double hitBox) {
        int xTile = (int)(hitBox.x/ TILES_SIZE);
        int yTile = (int)(hitBox.y / TILES_SIZE);
        int xTileRight = (int)((hitBox.x+ hitBox.width)/ TILES_SIZE);
        if (level.getDecoSpriteIndex(xTile, yTile) == 36 || level.getDecoSpriteIndex(xTileRight, yTile) == 36) return 1;
        if (level.getDecoSpriteIndex(xTile, yTile) == 35) return -1;
        return 0;
    }

    public boolean isProjectileHitLevel(int[][] lvlData, Projectile projectile) {
        return isSolid(projectile.getHitBox().x+ARROW_DEF_WID/2.0, projectile.getHitBox().y+ARROW_DEF_HEI/2.0, lvlData);
    }

    // Other
    public Particle[] loadParticles() {
        Particle[] particles = new Particle[50];
        Random rand = new Random();
        for (int i = 0; i < particles.length; i++) {
            int size = (int)((rand.nextInt(15-5) + 5) * SCALE);
            int xPos = rand.nextInt(GAME_WIDTH-10) + 10;
            int yPos = rand.nextInt(GAME_HEIGHT-10) + 10;
            BufferedImage[] images = new BufferedImage[8];
            for (int k = 0; k < 8; k++) images[k] = importImage("src/main/resources/images/particles/Default-Particle"+k+".png", size, size);
            particles[i] = new Particle(images, xPos, yPos);
        }
        return particles;
    }

    public Point getPlayerSpawn(BufferedImage level) {
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getGreen();
                if (value == 100) return new Point(i*TILES_SIZE, j*TILES_SIZE);
            }
        }
        return null;
    }

    public static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }
}
