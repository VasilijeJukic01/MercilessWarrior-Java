package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Coin extends GameObject{

    public Coin(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(10 * Tiles.SCALE.getValue());
        int hbHei = (int)(10 * Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(3 * Tiles.SCALE.getValue());
        super.yOffset = (int)(3 * Tiles.SCALE.getValue());
    }

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
