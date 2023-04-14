package platformer.utils;

import platformer.model.entities.Direction;
import platformer.model.entities.effects.Particle;
import platformer.model.Tiles;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.levels.Level;
import platformer.model.objects.*;
import platformer.model.objects.Container;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;

public class Utils {

    public static Utils instance = null;

    private Utils() {}

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
    public  BufferedImage rotateImage(BufferedImage src, double angle) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage dest = new BufferedImage(h, w, src.getType());
        Graphics2D graphics2D = dest.createGraphics();
        graphics2D.translate((h - w) / 2, (h - w) / 2);
        graphics2D.rotate(angle, h / 2.0, w / 2.0);
        graphics2D.drawRenderedImage(src, null);
        return dest;
    }

    // Data gatherer
    public BufferedImage[] getAllLevelsL1() {
        BufferedImage[] levels = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            levels[i] = importImage("src/main/resources/images/levels/level"+(i+1)+"_layer1.png", -1, -1);
        }
        return levels;
    }

    public BufferedImage[] getAllLevelsL2() {
        BufferedImage[] levels = new BufferedImage[2];
        for (int i = 0; i < 2; i++) {
            levels[i] = importImage("src/main/resources/images/levels/level"+(i+1)+"_layer2.png", -1, -1);
        }
        return levels;
    }

    public int[][] getLevelData(BufferedImage level) {
        int[][] lvlData = new int[level.getWidth()][level.getHeight()];
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getRed();
                if (value == 50) value = -2;
                else if (value == 51) value = -3;
                else if (value >= 49) value = -1;
                lvlData[i][j] = value;
            }
        }
        return lvlData;
    }

    public int[][] getDecoData(BufferedImage level) {
        int[][] decoData = new int[level.getWidth()][level.getHeight()];
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getBlue();
                if (value >= 64) value = -1;
                decoData[i][j] = value;
            }
        }
        return decoData;
    }

    public ArrayList<Skeleton> getSkeletonData(BufferedImage level) {
        ArrayList<Skeleton> skeletons = new ArrayList<>();
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getGreen();
                if (value == 0) skeletons.add(new Skeleton((int)(i*Tiles.TILES_SIZE.getValue()), (int)((j-1)*Tiles.TILES_SIZE.getValue())));
            }
        }
        return skeletons;
    }

    public ArrayList<Potion> getPotionData(BufferedImage level) {
        ArrayList<Potion> potions = new ArrayList<>();
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getBlue();
                if (value == ObjType.HEAL_POTION.ordinal()) potions.add(new Potion(ObjType.HEAL_POTION, (int)((i+0.5)*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.STAMINA_POTION.ordinal()) potions.add(new Potion(ObjType.STAMINA_POTION, (int)((i+0.5)*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
            }
        }
        return potions;
    }

    public ArrayList<Container> getContainerData(BufferedImage level) {
        ArrayList<Container> containers = new ArrayList<>();
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getBlue();
                if (value == ObjType.BOX.ordinal()) containers.add(new Container(ObjType.BOX, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.BARREL.ordinal()) containers.add(new Container(ObjType.BARREL, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
            }
        }
        return containers;
    }

    public ArrayList<Spike> getSpikeData(BufferedImage level) {
        ArrayList<Spike> spikes = new ArrayList<>();
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getBlue();
                if (value == ObjType.SPIKE.ordinal()) spikes.add(new Spike(ObjType.SPIKE, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
            }
        }
        return spikes;
    }

    public ArrayList<ArrowLauncher> getArrowLauncherData(BufferedImage level) {
        ArrayList<ArrowLauncher> arrowLaunchers = new ArrayList<>();
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getBlue();
                if (value == ObjType.ARROW_LAUNCHER_LEFT.ordinal())
                    arrowLaunchers.add(new ArrowLauncher(ObjType.ARROW_LAUNCHER_LEFT, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.ARROW_LAUNCHER_RIGHT.ordinal())
                    arrowLaunchers.add(new ArrowLauncher(ObjType.ARROW_LAUNCHER_RIGHT, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
            }
        }
        return arrowLaunchers;
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
        int xMaxSize = levelData.length * (int)Tiles.TILES_SIZE.getValue();
        int yMaxSize = levelData[0].length * (int)Tiles.TILES_SIZE.getValue();
        if (x < 0 || x >= xMaxSize) return true;
        if (y < 0 || y >= yMaxSize) return true;

        int xIndex = (int)(x / Tiles.TILES_SIZE.getValue());
        int yIndex = (int)(y / Tiles.TILES_SIZE.getValue());

        return isTileSolid(xIndex, yIndex, levelData);
    }

    public boolean isTileSolid(int xTile, int yTile, int[][] levelData) {
        int value = levelData[xTile][yTile];
        if (value == -1) return false;
        return (value >= 0 && value < 49);
    }

    private boolean isSolidBetween(double x, double yA, double yB, int[][] levelData) {
        int xIndex = (int)(x / Tiles.TILES_SIZE.getValue());
        int yAIndex = (int)(yA / Tiles.TILES_SIZE.getValue());
        int yBIndex = (int)(yB / Tiles.TILES_SIZE.getValue());

        for (int i = yAIndex; i <= yBIndex; i++) {
            int value = levelData[xIndex][i];
            if (value > -1 && value < 49) return true;
        }

        return false;
    }

    public double getXPosOnTheWall(Rectangle2D.Double hitBox, double dx) {
        int currentTile = (int)(hitBox.x / Tiles.TILES_SIZE.getValue());
        if (dx > 0) {
            int tileX = (int)(currentTile * Tiles.TILES_SIZE.getValue());
            int offsetX = (int)(Tiles.TILES_SIZE.getValue() - hitBox.width);
            return tileX + offsetX - 1;
        }
        else {
            return hitBox.x;
        }
    }

    public double getYPosOnTheCeil(Rectangle2D.Double hitBox, double airSpeed) {
        int currentTile = (int)(hitBox.y / Tiles.TILES_SIZE.getValue()+1);
        if (airSpeed > 0) {
            // Fall
            int tileY = (int)(currentTile * Tiles.TILES_SIZE.getValue());
            int offsetY = (int)(Tiles.TILES_SIZE.getValue() - hitBox.height);
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
        int xTileEnemy = (int)(enemyHitBox.x / Tiles.TILES_SIZE.getValue());
        int xTilePlayer = (int)(playerHitBox.x / Tiles.TILES_SIZE.getValue());
        if (xTileEnemy > xTilePlayer) return areAllTilesWalkable(xTilePlayer, xTileEnemy, yTileEnemy, levelData);
        else return areAllTilesWalkable(xTileEnemy, xTilePlayer, yTileEnemy, levelData);
    }

    public boolean canLauncherSeePlayer(int[][] levelData, Rectangle2D.Double playerHitBox, Rectangle2D.Double launcherHitBox, int yTile) {
        int xTileEnemy = (int)(launcherHitBox.x / Tiles.TILES_SIZE.getValue());
        int xTilePlayer = (int)(playerHitBox.x / Tiles.TILES_SIZE.getValue());
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
        int xTile = (int)(hitBox.x / Tiles.TILES_SIZE.getValue());
        if (direction == Direction.LEFT) {
            int xTileLeft = xTile - 1;
            return (x >= (int)(xTileLeft*Tiles.TILES_SIZE.getValue()+Tiles.TILES_SIZE.getValue()) && x <= (int)(xTileLeft*Tiles.TILES_SIZE.getValue()+Tiles.TILES_SIZE.getValue()+2));
        }
        else if (direction == Direction.RIGHT) {
            int xTileRight = xTile + 1;
            double dp = hitBox.x+hitBox.width - xTileRight*Tiles.TILES_SIZE.getValue();
            return (dp >= -2 && dp <= 2);
        }
        return false;
    }

    public boolean isOnWall(Rectangle2D.Double hitBox, int[][] levelData, Direction direction) {
        int xTile = (int)(hitBox.x / Tiles.TILES_SIZE.getValue());
        int yTile = (int)(hitBox.y / Tiles.TILES_SIZE.getValue());
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
        int xTile = (int)(hitBox.x/ Tiles.TILES_SIZE.getValue());
        int yTile = (int)(hitBox.y / Tiles.TILES_SIZE.getValue());
        int xTileRight = (int)((hitBox.x+ hitBox.width)/ Tiles.TILES_SIZE.getValue());
        if (level.getSpriteIndex(xTile, yTile) == -2 || level.getSpriteIndex(xTileRight, yTile) == -2) return 1;
        if (level.getSpriteIndex(xTile, yTile) == -3) return -1;
        return 0;
    }

    public boolean isProjectileHitLevel(int[][] lvlData, Projectile projectile) {
        return isSolid(projectile.getHitBox().x+PRSet.ARROW_DEF_WID.getValue()/2, projectile.getHitBox().y+PRSet.ARROW_DEF_HEI.getValue()/2, lvlData);
    }

    // Other
    public Particle[] loadParticles() {
        Particle[] particles = new Particle[50];
        Random rand = new Random();
        for (int i = 0; i < particles.length; i++) {
            int size = (int)((rand.nextInt(15-5) + 5) * Tiles.SCALE.getValue());
            int xPos = rand.nextInt((int)Tiles.GAME_WIDTH.getValue()-10) + 10;
            int yPos = rand.nextInt((int)Tiles.GAME_HEIGHT.getValue()-10) + 10;
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
                if (value == 100) return new Point(i*(int)Tiles.TILES_SIZE.getValue(), j*(int)Tiles.TILES_SIZE.getValue());
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
