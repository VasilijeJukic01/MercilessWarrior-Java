package platformer.model.levels;

import platformer.model.entities.enemies.*;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.Lancer;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.gameObjects.npc.Npc;
import platformer.model.gameObjects.npc.NpcType;
import platformer.model.gameObjects.objects.Container;
import platformer.model.gameObjects.objects.*;
import platformer.model.spells.types.Flash;
import platformer.model.spells.types.Lightning;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;
import platformer.utils.CollectionUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

/**
 * Represents a model for level in the game.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Level {

    // Data
    private final String name;
    private final String tilesetName;
    private int npcIndicator = 0;
    private final BufferedImage levelImg;
    private int[][] lvlData, decoData, layerData;

    // Items
    private final Map<EnemyType, List<Enemy>> enemiesMap = new HashMap<>();
    private final Map<ObjType, List<GameObject>> objectsMap = new HashMap<>();
    private final Map<SpellType, List<Spell>> spellsMap = new HashMap<>();
    private static final Map<String, List<NpcType>> npcMap = new HashMap<>();

    private final List<Trigger> triggers = new ArrayList<>();
    private final Map<LvlTriggerType, Point> spawnPoints = new HashMap<>();

    // Configuration
    private int levelTilesWidth, levelTilesHeight;
    private int xMaxTilesOffset, xMaxLevelOffset;
    private int yMaxTilesOffset, yMaxLevelOffset;

    static {
        npcMap.put("level02", List.of(NpcType.ANITA));
        npcMap.put("level11", List.of(NpcType.NIKOLAS));
        npcMap.put("level10", List.of(NpcType.SIR_DEJANOVIC));
        npcMap.put("level13", List.of(NpcType.RORIC));
    }

    public Level(String name, BufferedImage levelImg, String tilesetName) {
        this.name = name;
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
        reset();
        for (int i = 0; i < panelWidth; i++) {
            for (int j = 0; j < levelImg.getHeight(); j++) {
                Color color = new Color(levelImg.getRGB(i, j));
                getEnemyData(i, j, color.getGreen());
                getObjectData(i, j, color.getBlue());
                getSpellData(i, j, color.getGreen(), color.getBlue());
            }
        }
        getTriggerData(panelWidth);
    }

    private void getEnemyData(int i, int j, int valueG) {
        if (valueG >= EnemyType.MAX.ordinal()) return;
        switch (EnemyType.values()[valueG]) {
            case SKELETON:
                addEnemy(new Skeleton(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            case GHOUL:
                addEnemy(new Ghoul(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            case LANCER:
                addEnemy(new Lancer(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            case KNIGHT:
                addEnemy(new Knight(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            case WRAITH:
                addEnemy(new Wraith(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            case RORIC:
                addEnemy(new Roric(i*TILES_SIZE, (j-1)*TILES_SIZE));
                break;
            default: break;
        }
    }

    private void getObjectData(int i, int j, int valueB) {
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
            case BLACKSMITH:
                addGameObject(new Blacksmith(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE)); break;
            case DOG:
                addGameObject(new Dog(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE)); break;
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
                addGameObject(new Table(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case BOARD:
                addGameObject(new Board(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
                break;
            case NPC:
                if (npcMap.containsKey(name)) {
                    List<NpcType> npcTypes = npcMap.get(name);
                    addGameObject(new Npc(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE, npcTypes.get(npcIndicator++)));
                }
                break;
            case LAVA:
                addGameObject(new Lava(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
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
            default: break;
        }
    }

    private void getSpellData(int i, int j, int valueG, int valueB) {
        // 100 : >100 Rule
        if (valueG == 100 && valueB == 101) {
            addSpell(new Lightning(SpellType.LIGHTNING, i*TILES_SIZE, j*TILES_SIZE));
        }
        else if (valueG == 100 && valueB == 102) {
            addSpell(new Flash(SpellType.FLASH, i*TILES_SIZE, (j+1)*TILES_SIZE));
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

    private void addSpell(Spell spell) {
        SpellType type = spell.getSpellType();
        spellsMap.computeIfAbsent(type, k -> new ArrayList<>()).add(spell);
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
                if ((value >= 80 && !layer) || (value > 4 && layer)) value = -1;
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
                int triggerValue = color.getBlue();

                if (triggerValue < LvlTriggerType.MAX.ordinal() && color.getRed() == 254 && color.getGreen() == 254) {
                    LvlTriggerType type = LvlTriggerType.values()[triggerValue];
                    int worldX = (i - (panelWidth * 2)) * TILES_SIZE;
                    int worldY = j * TILES_SIZE;
                    if (type.name().startsWith("SPAWN")) {
                        spawnPoints.put(type, new Point(worldX, worldY));
                    }
                    else {
                        Rectangle2D.Double bounds = new Rectangle2D.Double(worldX, worldY, TILES_SIZE, TILES_SIZE);
                        triggers.add(new Trigger(bounds, type));
                    }
                }
            }
        }
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
        this.npcIndicator = 0;
        enemiesMap.clear();
        objectsMap.clear();
        spellsMap.clear();
        triggers.clear();
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

    /**
     * Returns the spawn point for the player based on the given location.
     *
     * @param targetSpawn The trigger type representing the desired spawn location.
     * @return The spawn point for the player.
     */
    public Point getPlayerSpawn(LvlTriggerType targetSpawn) {
        if (spawnPoints.containsKey(targetSpawn)) {
            return spawnPoints.get(targetSpawn);
        }
        // Fallback
        if (!spawnPoints.isEmpty()) return spawnPoints.values().iterator().next();
        return new Point(5 * TILES_SIZE, 8 * TILES_SIZE);
    }

    private List<Spell> getAllSpells() {
        return CollectionUtils.getAllItems(spellsMap);
    }

    public <T> List<T> getSpells(Class<T> spellType) {
        return getAllSpells().stream()
                .filter(spellType::isInstance)
                .map(spellType::cast)
                .collect(Collectors.toList());
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
}
