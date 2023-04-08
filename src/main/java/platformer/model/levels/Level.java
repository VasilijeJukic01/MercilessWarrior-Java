package platformer.model.levels;

import platformer.model.Tiles;
import platformer.model.entities.enemies.Skeleton;
import platformer.model.objects.ArrowLauncher;
import platformer.model.objects.Container;
import platformer.model.objects.Potion;
import platformer.model.objects.Spike;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

@SuppressWarnings("FieldCanBeLocal")
public class Level {

    private final BufferedImage data;
    private int[][] lvlData;
    private ArrayList<Skeleton> skeletons;
    private ArrayList<Potion> potions;
    private ArrayList<Container> containers;
    private ArrayList<Spike> spikes;
    private ArrayList<ArrowLauncher> arrowLaunchers;
    private int levelTilesWidth;
    private int xMaxTilesOffset;
    private int xMaxLevelOffset;
    private int levelTilesHeight;
    private int yMaxTilesOffset;
    private int yMaxLevelOffset;
    private final Point playerSpawn;

    public Level(BufferedImage data) {
        this.data = data;
        init();
        setOffset();
        this.playerSpawn = Utils.getInstance().getPlayerSpawn(data);
    }

    private void init() {
        this.lvlData = Utils.getInstance().getLevelData(data);
        this.skeletons = Utils.getInstance().getSkeletonData(data);
        this.spikes = Utils.getInstance().getSpikeData(data);
        this.arrowLaunchers = Utils.getInstance().getArrowLauncherData(data);
        loadObjectData();
    }

    public void loadObjectData() {
        this.potions = Utils.getInstance().getPotionData(data);
        this.containers = Utils.getInstance().getContainerData(data);
    }

    public void setOffset() {
        this.levelTilesWidth = data.getWidth();
        this.xMaxTilesOffset = levelTilesWidth - (int)(Tiles.TILES_WIDTH.getValue());
        this.xMaxLevelOffset = xMaxTilesOffset * (int)Tiles.TILES_SIZE.getValue();
        this.levelTilesHeight = data.getHeight();
        this.yMaxTilesOffset = levelTilesHeight - (int)(Tiles.TILES_HEIGHT.getValue());
        this.yMaxLevelOffset = yMaxTilesOffset * (int)(Tiles.TILES_SIZE.getValue());
    }

    public int getSpriteIndex(int x, int y) {
        return lvlData[x][y];
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
}
