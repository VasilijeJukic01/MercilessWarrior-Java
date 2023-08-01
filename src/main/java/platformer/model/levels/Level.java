package platformer.model.levels;

import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Ghoul;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.objects.*;
import platformer.model.objects.Container;
import platformer.model.spells.Flash;
import platformer.model.spells.Lightning;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import static platformer.constants.Constants.*;

@SuppressWarnings("FieldCanBeLocal")
public class Level {

    // Data
    private final BufferedImage dataL1, dataL2;
    private int[][] lvlData, decoData, layerData;
    // Enemies
    private final ArrayList<Skeleton> skeletons = new ArrayList<>();
    private final ArrayList<Ghoul> ghouls = new ArrayList<>();
    private SpearWoman spearWoman;
    // Objects
    private final ArrayList<Potion> potions = new ArrayList<>();
    private final ArrayList<Container> containers = new ArrayList<>();
    private final ArrayList<Spike> spikes = new ArrayList<>();
    private final ArrayList<ArrowLauncher> arrowLaunchers = new ArrayList<>();
    private final ArrayList<Shop> shops = new ArrayList<>();
    private final ArrayList<Blocker> blockers = new ArrayList<>();
    private final ArrayList<Blacksmith> blacksmiths = new ArrayList<>();
    private final ArrayList<Dog> dogs = new ArrayList<>();
    // Spells
    private final ArrayList<Lightning> lightnings = new ArrayList<>();
    private final ArrayList<Flash> flashes = new ArrayList<>();
    // Other
    private int levelTilesWidth, levelTilesHeight;
    private int xMaxTilesOffset, xMaxLevelOffset;
    private int yMaxTilesOffset, yMaxLevelOffset;
    private final Point playerSpawn;

    public Level(BufferedImage dataL1, BufferedImage dataL2) {
        this.dataL1 = dataL1;
        this.dataL2 = dataL2;
        init();
        setOffset();
        this.playerSpawn = getPlayerSpawn(dataL1);
    }

    // Data gatherer
    private void getEnemyData() {
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int value = color.getGreen();
                if (value >= EnemyType.MAX.ordinal()) continue;
                switch (EnemyType.values()[value]) {
                    case SKELETON:
                        skeletons.add(new Skeleton(i*TILES_SIZE, (j-1)*TILES_SIZE)); break;
                    case GHOUL:
                        ghouls.add(new Ghoul(i*TILES_SIZE, (j-1)*TILES_SIZE)); break;
                    case SPEAR_WOMAN:
                        spearWoman = new SpearWoman(i*TILES_SIZE, (j-1)*TILES_SIZE); break;
                    default: break;
                }
            }
        }
    }

    public void getObjectData() {
        clear();
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int value = color.getBlue();
                if (value >= ObjType.MAX.ordinal()) continue;
                switch (ObjType.values()[value]) {
                    case HEAL_POTION:
                        potions.add(new Potion(ObjType.HEAL_POTION, (int)((i+0.5)*TILES_SIZE), j*TILES_SIZE)); break;
                    case STAMINA_POTION:
                        potions.add(new Potion(ObjType.STAMINA_POTION, (int)((i+0.5)*TILES_SIZE), j*TILES_SIZE)); break;
                    case BOX:
                        containers.add(new Container(ObjType.BOX, i*TILES_SIZE, j*TILES_SIZE)); break;
                    case BARREL:
                        containers.add(new Container(ObjType.BARREL, i*TILES_SIZE, j*TILES_SIZE)); break;
                    case SPIKE:
                        spikes.add(new Spike(ObjType.SPIKE, i*TILES_SIZE, j*TILES_SIZE)); break;
                    case ARROW_TRAP_LEFT:
                    case ARROW_TRAP_RIGHT:
                        arrowLaunchers.add(new ArrowLauncher(ObjType.values()[value], i*TILES_SIZE, j*TILES_SIZE)); break;
                    case SHOP:
                        shops.add(new Shop(ObjType.SHOP, i*TILES_SIZE, j*TILES_SIZE)); break;
                    case BLOCKER:
                        blockers.add(new Blocker(ObjType.BLOCKER, (int)((i-1.75)*TILES_SIZE), (j-1)*TILES_SIZE)); break;
                    case BLACKSMITH:
                        blacksmiths.add(new Blacksmith(ObjType.BLACKSMITH, i*TILES_SIZE, j*TILES_SIZE)); break;
                    case DOG:
                        dogs.add(new Dog(ObjType.DOG, i*TILES_SIZE, j*TILES_SIZE)); break;
                    default: break;
                }
            }
        }
    }

    private void getLightningPos() {
        lightnings.clear();
        flashes.clear();
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int valueG = color.getGreen();
                int valueB = color.getBlue();
                // 100 : >100 Rule
                if (valueG == 100 && valueB == 101) {
                    Lightning l = new Lightning(SpellType.LIGHTNING, i*TILES_SIZE, j*TILES_SIZE);
                    lightnings.add(l);
                }
                else if (valueG == 100 && valueB == 102) {
                    Flash f = new Flash(SpellType.FLASH, i*TILES_SIZE, (j+1)*TILES_SIZE);
                    flashes.add(f);
                }
            }
        }
    }

    private void init() {
        this.lvlData = getLevelData(dataL1);
        this.decoData = getDecoData(dataL2, false);
        this.layerData = getDecoData(dataL2, true);
        getEnemyData();
        getObjectData();
        getLightningPos();
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
        this.levelTilesWidth = dataL1.getWidth();
        this.xMaxTilesOffset = levelTilesWidth - TILES_WIDTH;
        this.xMaxLevelOffset = xMaxTilesOffset * TILES_SIZE;
        this.levelTilesHeight = dataL1.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - TILES_HEIGHT;
        this.yMaxLevelOffset = yMaxTilesOffset * TILES_SIZE;
    }

    private void clear() {
        potions.clear();
        containers.clear();
        spikes.clear();
        shops.clear();
        arrowLaunchers.clear();
        blockers.clear();
        blacksmiths.clear();
        dogs.clear();
    }

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

    public ArrayList<Skeleton> getSkeletons() {
        return skeletons;
    }

    public ArrayList<Ghoul> getGhouls() {
        return ghouls;
    }

    public SpearWoman getSpearWoman() {
        return spearWoman;
    }

    public Point getPlayerSpawn() {
        return playerSpawn;
    }

    public ArrayList<Potion> getPotions() {
        return potions;
    }

    public ArrayList<Container> getContainers() {
        return containers;
    }

    public ArrayList<Spike> getSpikes() {
        return spikes;
    }

    public ArrayList<ArrowLauncher> getArrowLaunchers() {
        return arrowLaunchers;
    }

    public ArrayList<Shop> getShops() {
        return shops;
    }

    public ArrayList<Blocker> getBlockers() {
        return blockers;
    }

    public ArrayList<Blacksmith> getBlacksmiths() {
        return blacksmiths;
    }

    public ArrayList<Dog> getDogs() {
        return dogs;
    }

    public ArrayList<Lightning> getLightnings() {
        return lightnings;
    }

    public ArrayList<Flash> getFlashes() {
        return flashes;
    }
}
