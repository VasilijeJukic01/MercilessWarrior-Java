package platformer.model.levels;

import platformer.model.Tiles;
import platformer.model.entities.enemies.Ghoul;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.objects.*;
import platformer.model.objects.Container;
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
    private ArrayList<Skeleton> skeletons;
    private ArrayList<Ghoul> ghouls;
    // Objects
    private ArrayList<Potion> potions;
    private ArrayList<Container> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<ArrowLauncher> arrowLaunchers;
    private ArrayList<Shop> shops;
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

    private void init() {
        this.lvlData = Utils.getInstance().getLevelData(dataL1);
        this.decoData = Utils.getInstance().getDecoData(dataL2, false);
        this.layerData = Utils.getInstance().getDecoData(dataL2, true);
        this.skeletons = Utils.getInstance().getSkeletonData(dataL1);
        this.ghouls = Utils.getInstance().getGhoulData(dataL1);
        loadObjectData();
    }

    public void loadObjectData() {
        this.potions = Utils.getInstance().getPotionData(dataL1);
        this.containers = Utils.getInstance().getContainerData(dataL1);
        this.spikes = Utils.getInstance().getSpikeData(dataL1);
        this.arrowLaunchers = Utils.getInstance().getArrowLauncherData(dataL1);
        this.shops = Utils.getInstance().getShopData(dataL1);
    }

    public void setOffset() {
        this.levelTilesWidth = dataL1.getWidth();
        this.xMaxTilesOffset = levelTilesWidth - (int)(Tiles.TILES_WIDTH.getValue());
        this.xMaxLevelOffset = xMaxTilesOffset * (int)Tiles.TILES_SIZE.getValue();
        this.levelTilesHeight = dataL1.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - (int)(Tiles.TILES_HEIGHT.getValue());
        this.yMaxLevelOffset = yMaxTilesOffset * (int)(Tiles.TILES_SIZE.getValue());
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
}
