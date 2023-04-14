package platformer.model.levels;

import java.awt.image.BufferedImage;

public class LevelObject {

    private final int tilesetID;
    private final int objectID;
    private int xOffset, yOffset;
    private final BufferedImage objectModel;
    private final int w, h;
    private int layer;

    public LevelObject(int tilesetID, int objectID, BufferedImage objectModel, int w, int h) {
        this.tilesetID = tilesetID;
        this.objectID = objectID;
        this.objectModel = objectModel;
        this.w = w;
        this.h = h;
    }

    public BufferedImage getObjectModel() {
        return objectModel;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }
}
