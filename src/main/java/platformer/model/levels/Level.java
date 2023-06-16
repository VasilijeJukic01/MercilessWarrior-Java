package platformer.model.levels;

import platformer.model.Tiles;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Ghoul;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.entities.enemies.boss.SpearWoman;
import platformer.model.objects.*;
import platformer.model.objects.Container;
import platformer.model.spells.Flash;
import platformer.model.spells.Lightning;
import platformer.model.spells.SpellType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class Level {

    // Data
    private final BufferedImage dataL1;
    private final BufferedImage dataL2;
    private int[][] lvlData;
    private int[][] decoData;
    private int[][] layerData;
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
    // Spells
    private final ArrayList<Lightning> lightnings = new ArrayList<>();
    private final ArrayList<Flash> flashes = new ArrayList<>();
    // Other
    private int levelTilesWidth;
    private int xMaxTilesOffset;
    private int xMaxLevelOffset;
    private int levelTilesHeight;
    private int yMaxTilesOffset;
    private int yMaxLevelOffset;
    private final Point playerSpawn;

    public Level(BufferedImage dataL1, BufferedImage dataL2) {
        this.dataL1 = dataL1;
        this.dataL2 = dataL2;
        init();
        setOffset();
        this.playerSpawn = Utils.getInstance().getPlayerSpawn(dataL1);
    }

    private void getEnemyData() {
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int value = color.getGreen();
                if (value == EnemyType.SKELETON.ordinal())
                    skeletons.add(new Skeleton((int)(i*Tiles.TILES_SIZE.getValue()), (int)((j-1)*Tiles.TILES_SIZE.getValue())));
                else if (value == EnemyType.GHOUL.ordinal())
                    ghouls.add(new Ghoul((int)(i*Tiles.TILES_SIZE.getValue()), (int)((j-1)*Tiles.TILES_SIZE.getValue())));
                else if (value == EnemyType.SPEAR_WOMAN.ordinal())
                    spearWoman = new SpearWoman((int)(i*Tiles.TILES_SIZE.getValue()), (int)((j-1)*Tiles.TILES_SIZE.getValue()));
            }
        }
    }

    public void getObjectData() {
        clear();
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int value = color.getBlue();
                if (value == ObjType.HEAL_POTION.ordinal())
                    potions.add(new Potion(ObjType.HEAL_POTION, (int)((i+0.5)*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.STAMINA_POTION.ordinal())
                    potions.add(new Potion(ObjType.STAMINA_POTION, (int)((i+0.5)*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.BOX.ordinal())
                    containers.add(new Container(ObjType.BOX, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.BARREL.ordinal())
                    containers.add(new Container(ObjType.BARREL, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.SPIKE.ordinal())
                    spikes.add(new Spike(ObjType.SPIKE, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.ARROW_LAUNCHER_LEFT.ordinal())
                    arrowLaunchers.add(new ArrowLauncher(ObjType.ARROW_LAUNCHER_LEFT, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.ARROW_LAUNCHER_RIGHT.ordinal())
                    arrowLaunchers.add(new ArrowLauncher(ObjType.ARROW_LAUNCHER_RIGHT, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
                else if (value == ObjType.SHOP.ordinal())
                    shops.add(new Shop(ObjType.SHOP, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue())));
            }
        }
    }

    private void getLightningPos() {
        lightnings.clear();
        for (int i = 0; i < dataL1.getWidth(); i++) {
            for (int j = 0; j < dataL1.getHeight(); j++) {
                Color color = new Color(dataL1.getRGB(i, j));
                int valueG = color.getGreen();
                int valueB = color.getBlue();
                if (valueG == 100 && valueB == 101) {
                    Lightning l = new Lightning(SpellType.LIGHTNING, (int)(i*Tiles.TILES_SIZE.getValue()), (int)(j*Tiles.TILES_SIZE.getValue()));
                    lightnings.add(l);
                }
                else if (valueG == 100 && valueB == 102) {
                    Flash f = new Flash(SpellType.FLASH, (int)(i*Tiles.TILES_SIZE.getValue()), (int)((j+1)*Tiles.TILES_SIZE.getValue()));
                    flashes.add(f);
                }
            }
        }
    }

    private void init() {
        this.lvlData = Utils.getInstance().getLevelData(dataL1);
        this.decoData = Utils.getInstance().getDecoData(dataL2, false);
        this.layerData = Utils.getInstance().getDecoData(dataL2, true);
        getEnemyData();
        getObjectData();
        getLightningPos();
    }

    public void setOffset() {
        this.levelTilesWidth = dataL1.getWidth();
        this.xMaxTilesOffset = levelTilesWidth - (int)(Tiles.TILES_WIDTH.getValue());
        this.xMaxLevelOffset = xMaxTilesOffset * (int)Tiles.TILES_SIZE.getValue();
        this.levelTilesHeight = dataL1.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - (int)(Tiles.TILES_HEIGHT.getValue());
        this.yMaxLevelOffset = yMaxTilesOffset * (int)(Tiles.TILES_SIZE.getValue());
    }

    private void clear() {
        potions.clear();
        containers.clear();
        spikes.clear();
        shops.clear();
        arrowLaunchers.clear();
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

    public ArrayList<Lightning> getLightnings() {
        return lightnings;
    }

    public ArrayList<Flash> getFlashes() {
        return flashes;
    }
}
