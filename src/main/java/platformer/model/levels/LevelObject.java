package platformer.model.levels;

import java.awt.image.BufferedImage;

public class LevelObject {

    private int xOffset, yOffset;
    private final BufferedImage objectModel;
    private final int w, h;

    public LevelObject(BufferedImage objectModel, int w, int h) {
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
}
