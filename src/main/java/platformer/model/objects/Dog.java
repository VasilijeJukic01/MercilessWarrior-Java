package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Dog  extends GameObject {

    public Dog(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(32 * Tiles.SCALE.getValue());
        int hbHei = (int)(32 * Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(18 * Tiles.SCALE.getValue());
        super.yOffset = (int)(12 * Tiles.SCALE.getValue());
    }

    // Core
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
