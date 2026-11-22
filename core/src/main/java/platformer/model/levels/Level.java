package platformer.model.levels;

import platformer.model.entities.enemies.*;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.*;
import platformer.model.gameObjects.objects.traps.*;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;

/**
 * Represents a model for level in the game.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Level {

    // Data
    private final String tilesetName;
    private final BufferedImage levelImg;
    private int[][] lvlData, decoData, layerData;
    private boolean initialized = false;

    // Items
    private final Map<EnemyType, List<Enemy>> enemiesMap = new HashMap<>();
    private final Map<ObjType, List<GameObject>> objectsMap = new HashMap<>();
    private final Set<Point> cutDecorations = new HashSet<>();
    private final Map<Point, Double> decorationBends = new HashMap<>();

    private final List<Trigger> triggers = new ArrayList<>();
    private final Map<LvlTriggerType, List<Point>> spawnPoints = new HashMap<>();

    // Configuration
    private int levelTilesWidth, levelTilesHeight;
    private int xMaxTilesOffset, xMaxLevelOffset;
    private int yMaxTilesOffset, yMaxLevelOffset;

    public Level(BufferedImage levelImg, String tilesetName) {
        this.levelImg = levelImg;
        this.tilesetName = tilesetName;
        init();
        setOffset();
    }

    // Init
    private void init() {
        int panelWidth = levelImg.getWidth() / 3;
        this.lvlData = getLevelData(panelWidth);
        this.decoData = getDecoData(false, panelWidth);
        this.layerData = getDecoData(true, panelWidth);
        gatherData();
    }

    // Level items Data
    public void gatherData() {
        int panelWidth = levelImg.getWidth() / 3;
        boolean[][] waterGrid = new boolean[panelWidth][levelImg.getHeight()];
        boolean[][] lavaGrid = new boolean[panelWidth][levelImg.getHeight()];
        reset();
        for (int i = 0; i < panelWidth; i++) {
            for (int j = 0; j < levelImg.getHeight(); j++) {
                Color colorP1 = new Color(levelImg.getRGB(i, j));
                Color colorP3 = new Color(levelImg.getRGB(i + panelWidth * 2, j));

                getEnemyData(i, j, colorP1.getGreen());
                getObjectData(i, j, colorP1.getBlue(), colorP3, waterGrid, lavaGrid);
            }
        }
        getTriggerData(panelWidth);
        buildMergedWaterPools(waterGrid, panelWidth, levelImg.getHeight());
        buildMergedLavaPools(lavaGrid, panelWidth, levelImg.getHeight());
        this.initialized = true;
    }

    private void getEnemyData(int i, int j, int valueG) {
        if (valueG >= EnemyType.MAX.ordinal()) return;
        Enemy enemy = EnemyFactory.create(EnemyType.values()[valueG], i * TILES_SIZE, (j - 1) * TILES_SIZE);
        if (enemy != null) addEnemy(enemy);
    }

    private void getObjectData(int i, int j, int valueB, Color colorP3, boolean[][] waterGrid, boolean[][] lavaGrid) {
        if (valueB >= ObjType.MAX.ordinal()) return;
        switch (ObjType.values()[valueB]) {
            case HEAL_POTION:
            case STAMINA_POTION:
                addGameObject(new Potion(ObjType.values()[valueB], (int)((i+0.5)*TILES_SIZE), j*TILES_SIZE));
                break;
            case BOX:
            case BARREL:
                addGameObject(new Container(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case ARROW_TRAP_LEFT:
            case ARROW_TRAP_RIGHT:
                addGameObject(new ArrowLauncher(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE)); break;
            case SHOP:
                addGameObject(new Shop(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE)); break;
            case BLOCKER:
                addGameObject(new Blocker(ObjType.values()[valueB], (int)((i-1.75)*TILES_SIZE), (j-1)*TILES_SIZE)); break;
            case SAVE_TOTEM:
                addGameObject(new SaveTotem(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case SMASH_TRAP:
                addGameObject(new SmashTrap(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case CANDLE:
                addGameObject(new Candle(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case TABLE:
                addGameObject(new Table(ObjType.values()[valueB], i*TILES_SIZE, (j + 1)*TILES_SIZE));
                break;
            case BOARD:
                addGameObject(new Board(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case LAVA:
                lavaGrid[i][j] = true;
                break;
            case BRICK:
                addGameObject(new Brick(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case JUMP_PAD:
                addGameObject(new JumpPad(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case SPIKE_UP:
            case SPIKE_DOWN:
            case SPIKE_LEFT:
            case SPIKE_RIGHT:
                addGameObject(new Spike(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case HERB:
                addGameObject(new Herb(ObjType.HERB, i * TILES_SIZE, j * TILES_SIZE));
                break;
            case DOOR:
                String dest = "exit";
                if (colorP3.getRed() == 254 && colorP3.getGreen() == 254) {
                    dest = getInteriorName(colorP3.getBlue());
                }
                addGameObject(new Door(ObjType.DOOR, i*TILES_SIZE, j*TILES_SIZE, dest));
                break;
            case WATER:
                waterGrid[i][j] = true;
                break;
            case CRUMBLING_PLATFORM:
                addGameObject(new CrumblingPlatform(i * TILES_SIZE, j * TILES_SIZE));
                break;
            case PENDULUM_TRAP:
                addGameObject(new PendulumTrap(i * TILES_SIZE, j * TILES_SIZE));
                break;
            case SPORE_TRAP:
                addGameObject(new SporeTrap(i * TILES_SIZE, j * TILES_SIZE));
                break;
            case SAW_TRAP:
                addGameObject(new SawTrap(i * TILES_SIZE, j * TILES_SIZE));
                break;
            default: break;
        }
    }

    private void addGameObject(GameObject gameObject) {
        ObjType type = gameObject.getObjType();
        objectsMap.computeIfAbsent(type, k -> new ArrayList<>()).add(gameObject);
    }

    private void addEnemy(Enemy enemy) {
        EnemyType type = enemy.getEnemyType();
        enemiesMap.computeIfAbsent(type, k -> new ArrayList<>()).add(enemy);
    }

    // Data Gatherer
    /**
     * Gathers data about the level from the level image.
     * It scans the image and assigns a value to each pixel based on its red color value.
     *
     * @param panelWidth The width of the level panel.
     * @return A 2D array representing the level data.
     */
    private int[][] getLevelData(int panelWidth) {
        int[][] data = new int[panelWidth][levelImg.getHeight()];
        for (int i = 0; i < panelWidth; i++) {
            for (int j = 0; j < levelImg.getHeight(); j++) {
                Color color = new Color(levelImg.getRGB(i, j));
                int value = color.getRed();
                if (value >= 49) value = -1;
                if (color.getBlue() == 255 && color.getGreen() == 255) value += 255;
                data[i][j] = value;
            }
        }
        return data;
    }

    /**
     * Gathers decoration data from the level image.
     * It scans the image and assigns a value to each pixel based on its green or blue color value.
     *
     * @param panelWidth The width of the level panel.
     * @param layer A boolean value indicating whether to gather layer data (true) or object data (false).
     * @return A 2D array representing the decoration data.
     */
    // layer = true -> Layer data;  layer = false -> Object data
    private int[][] getDecoData(boolean layer, int panelWidth) {
        int[][] data = new int[panelWidth][levelImg.getHeight()];
        for (int i = panelWidth; i < panelWidth * 2; i++) {
            for (int j = 0; j < levelImg.getHeight(); j++) {
                Color color = new Color(levelImg.getRGB(i, j));
                int value = layer ? color.getGreen() : color.getBlue();
                if ((value >= 200 && !layer) || (value > 4 && layer)) value = -1;
                data[i - panelWidth][j] = value;
            }
        }
        return data;
    }

    /**
     * Gathers trigger data from the level image.
     * It scans the image for specific color codes that represent different triggers and creates Trigger objects accordingly.
     *
     * @param panelWidth The width of the panel, used to calculate the world coordinates of the triggers.
     */
    private void getTriggerData(int panelWidth) {
        for (int i = panelWidth * 2; i < levelImg.getWidth(); i++) {
            for (int j = 0; j < levelImg.getHeight(); j++) {
                Color color = new Color(levelImg.getRGB(i, j));
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                // Standard Triggers (R=254, G=254)
                if (b < LvlTriggerType.MAX.ordinal() && r == 254 && g == 254) {
                    LvlTriggerType type = LvlTriggerType.values()[b];
                    int worldX = (i - (panelWidth * 2)) * TILES_SIZE;
                    int worldY = j * TILES_SIZE;

                    if (type.name().startsWith("SPAWN")) {
                        spawnPoints.computeIfAbsent(type, k -> new ArrayList<>()).add(new Point(worldX, worldY));
                    }
                    else {
                        Rectangle2D.Double bounds = new Rectangle2D.Double(worldX, worldY, TILES_SIZE, TILES_SIZE);
                        triggers.add(new Trigger(bounds, type));
                    }
                }
                // NPC Triggers (R=0, G=0)
                else if (r == 0 && g == 0 && b < NpcType.values().length) {
                    int worldX = (i - (panelWidth * 2)) * TILES_SIZE;
                    int worldY = j * TILES_SIZE;
                    NpcType type = NpcType.values()[b];
                    addGameObject(new Npc(ObjType.NPC, worldX, worldY, type));
                }
            }
        }
    }

    private String getInteriorName(int blueValue) {
        return switch (blueValue) {
            case 10 -> "tavern";
            default -> "exit";
        };
    }

    // Fluids
    private void buildMergedWaterPools(boolean[][] waterGrid, int width, int height) {
        boolean[][] visited = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!waterGrid[x][y] || visited[x][y]) continue;

                int spanW = 1;
                while (x + spanW < width && waterGrid[x + spanW][y] && !visited[x + spanW][y]) {
                    spanW++;
                }

                int spanH = 1;
                outer:
                while (y + spanH < height) {
                    for (int k = 0; k < spanW; k++) {
                        if (!waterGrid[x + k][y + spanH] || visited[x + k][y + spanH]) {
                            break outer;
                        }
                    }
                    spanH++;
                }

                for (int dy = 0; dy < spanH; dy++) {
                    for (int dx = 0; dx < spanW; dx++) {
                        visited[x + dx][y + dy] = true;
                    }
                }
                addGameObject(new Water(x * TILES_SIZE, y * TILES_SIZE, spanW, spanH));
            }
        }
    }

    private void buildMergedLavaPools(boolean[][] lavaGrid, int width, int height) {
        boolean[][] visited = new boolean[width][height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!lavaGrid[x][y] || visited[x][y]) continue;

                int spanW = 1;
                while (x + spanW < width && lavaGrid[x + spanW][y] && !visited[x + spanW][y]) {
                    spanW++;
                }

                int spanH = 1;
                outer:
                while (y + spanH < height) {
                    for (int k = 0; k < spanW; k++) {
                        if (!lavaGrid[x + k][y + spanH] || visited[x + k][y + spanH]) {
                            break outer;
                        }
                    }
                    spanH++;
                }

                for (int dy = 0; dy < spanH; dy++) {
                    for (int dx = 0; dx < spanW; dx++) {
                        visited[x + dx][y + dy] = true;
                    }
                }
                addGameObject(new Lava(x * TILES_SIZE, y * TILES_SIZE, spanW, spanH));
            }
        }
    }

    // Cutting
    public void markDecorationCut(int tileX, int tileY) {
        cutDecorations.add(new Point(tileX, tileY));
    }

    public boolean isDecorationCut(int tileX, int tileY) {
        return cutDecorations.contains(new Point(tileX, tileY));
    }

    public void setDecorationBend(Point pt, double bend) {
        decorationBends.put(pt, bend);
    }

    public double getDecorationBend(int tileX, int tileY) {
        if (decorationBends.isEmpty()) return 0.0;
        return decorationBends.getOrDefault(new Point(tileX, tileY), 0.0);
    }

    // Other
    /**
     * Sets the offset values for the level based on the width and height of the level image.
     */
    public void setOffset() {
        this.levelTilesWidth = levelImg.getWidth() / 3;
        this.xMaxTilesOffset = levelTilesWidth - TILES_WIDTH;
        this.xMaxLevelOffset = xMaxTilesOffset * TILES_SIZE;
        this.levelTilesHeight = levelImg.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - TILES_HEIGHT;
        this.yMaxLevelOffset = yMaxTilesOffset * TILES_SIZE;
    }

    private void reset() {
        enemiesMap.clear();
        objectsMap.clear();
        triggers.clear();
        cutDecorations.clear();
        decorationBends.clear();
        spawnPoints.clear();
    }

    // Getters
    public int getSpriteIndex(int x, int y) {
        return lvlData[x][y];
    }

    public int getDecoSpriteIndex(int x, int y) {
        return decoData[x][y];
    }

    public int getLayerSpriteIndex(int x, int y) {
        return layerData[x][y];
    }

    public int[][] getLvlData() {
        return lvlData;
    }

    public int[][] getDecoData() {
        return decoData;
    }

    public String getTilesetName() {
        return tilesetName;
    }

    public int getXMaxLevelOffset() {
        return xMaxLevelOffset;
    }

    public int getYMaxLevelOffset() {
        return yMaxLevelOffset;
    }

    public int getLevelTilesWidth() {
        return levelTilesWidth;
    }

    public int getLevelTilesHeight() {
        return levelTilesHeight;
    }

    public Map<Point, Double> getDecorationBends() {
        return decorationBends;
    }


    /**
     * Finds the SaveTotem object in this level and returns the ground spawn position for the player.
     * Falls back to SPAWN_A if no SaveTotem object is present in the room.
     */
    public Point getSaveTotemSpawnPoint() {
        List<GameObject> totems = objectsMap.get(ObjType.SAVE_TOTEM);
        if (totems != null && !totems.isEmpty()) {
            GameObject totem = totems.get(0);
            int spawnX = (int) totem.getHitBox().x;
            double groundY = CollisionDetector.getGroundY(
                    totem.getHitBox().getCenterX(),
                    totem.getHitBox().y,
                    lvlData
            );
            int spawnY = (int) (groundY - PLAYER_HB_HEI - 1);
            return new Point(spawnX, spawnY);
        }
        return getPlayerSpawn(LvlTriggerType.SPAWN_A);
    }

    /**
     * Returns the spawn point for the player based on the given location.
     *
     * @param targetSpawn The trigger type representing the desired spawn location.
     * @return The spawn point for the player.
     */
    public Point getPlayerSpawn(LvlTriggerType targetSpawn) {
        return getPlayerSpawn(targetSpawn, null, null);
    }

    public Point getPlayerSpawn(LvlTriggerType targetSpawn, Integer hintTileX, Integer hintTileY) {
        List<Point> points = spawnPoints.get(targetSpawn);
        if (hintTileX != null && hintTileY != null) {
            Point hintWorld = new Point(hintTileX * TILES_SIZE, hintTileY * TILES_SIZE);
            if (points != null && !points.isEmpty()) {
                Point closest = points.get(0);
                double bestDist = closest.distance(hintWorld);
                for (Point p : points) {
                    double d = p.distance(hintWorld);
                    if (d < bestDist) {
                        bestDist = d;
                        closest = p;
                    }
                }
                if (bestDist <= 10 * TILES_SIZE) return closest;
            }
            return hintWorld;
        }

        if (points != null && !points.isEmpty()) {
            return points.get(0);
        }
        for (List<Point> anyList : spawnPoints.values()) {
            if (!anyList.isEmpty()) return anyList.get(0);
        }
        return new Point(5 * TILES_SIZE, 8 * TILES_SIZE);
    }

    public Map<EnemyType, List<Enemy>> getEnemiesMap() {
        return enemiesMap;
    }

    public Map<ObjType, List<GameObject>> getObjectsMap() {
        return objectsMap;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    public boolean isInitialized() {
        return initialized;
    }
}
