package platformer.model.levels;

import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Ghoul;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.objects.*;
import platformer.model.objects.Container;
import platformer.model.spells.Flash;
import platformer.model.spells.Lightning;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static platformer.constants.Constants.*;

@SuppressWarnings("FieldCanBeLocal")
public class Level {

    // Data
    private final BufferedImage layer1Img, layer2Img;
    private int[][] lvlData, decoData, layerData;

    // Items
    private final Map<EnemyType, List<Enemy>> enemiesMap = new HashMap<>();
    private final Map<ObjType, List<GameObject>> objectsMap = new HashMap<>();
    private final Map<SpellType, List<Spell>> spellsMap = new HashMap<>();

    // Other
    private int levelTilesWidth, levelTilesHeight;
    private int xMaxTilesOffset, xMaxLevelOffset;
    private int yMaxTilesOffset, yMaxLevelOffset;
    private final Point playerSpawn;

    public Level(BufferedImage layer1Img, BufferedImage layer2Img) {
        this.layer1Img = layer1Img;
        this.layer2Img = layer2Img;
        init();
        setOffset();
        this.playerSpawn = getPlayerSpawn(layer1Img);
    }

    // Init
    private void init() {
        this.lvlData = getLevelData(layer1Img);
        this.decoData = getDecoData(layer2Img, false);
        this.layerData = getDecoData(layer2Img, true);
        getData();
    }

    // Level items Data
    public void getData() {
        reset();
        for (int i = 0; i < layer1Img.getWidth(); i++) {
            for (int j = 0; j < layer1Img.getHeight(); j++) {
                Color color = new Color(layer1Img.getRGB(i, j));
                int valueG = color.getGreen();
                int valueB = color.getBlue();

                getEnemyData(i, j, valueG);
                getObjectData(i, j, valueB);
                getSpellData(i, j, valueG, valueB);
            }
        }
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
            case SPEAR_WOMAN:
                addEnemy(new SpearWoman(i*TILES_SIZE, (j-1)*TILES_SIZE));
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
            case SPIKE:
                addGameObject(new Spike(ObjType.values()[valueB], i*TILES_SIZE, j*TILES_SIZE));
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
    private int[][] getLevelData(BufferedImage level) {
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
    private int[][] getDecoData(BufferedImage level, boolean layer) {
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

    private Point getPlayerSpawn(BufferedImage level) {
        for (int i = 0; i < level.getWidth(); i++) {
            for (int j = 0; j < level.getHeight(); j++) {
                Color color = new Color(level.getRGB(i, j));
                int value = color.getGreen();
                if (value == 100) return new Point(i*TILES_SIZE, j*TILES_SIZE);
            }
        }
        return null;
    }

    // Other
    public void setOffset() {
        this.levelTilesWidth = layer1Img.getWidth();
        this.xMaxTilesOffset = levelTilesWidth - TILES_WIDTH;
        this.xMaxLevelOffset = xMaxTilesOffset * TILES_SIZE;
        this.levelTilesHeight = layer1Img.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - TILES_HEIGHT;
        this.yMaxLevelOffset = yMaxTilesOffset * TILES_SIZE;
    }

    private void reset() {
        enemiesMap.clear();
        objectsMap.clear();
        spellsMap.clear();
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

    public int getXMaxLevelOffset() {
        return xMaxLevelOffset;
    }

    public int getYMaxLevelOffset() {
        return yMaxLevelOffset;
    }

    public Point getPlayerSpawn() {
        return playerSpawn;
    }

    private List<Spell> getAllSpells() {
        return Utils.getInstance().getAllItems(spellsMap);
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
}
