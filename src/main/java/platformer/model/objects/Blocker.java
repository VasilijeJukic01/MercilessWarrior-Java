package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Blocker extends GameObject {

    public Blocker(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        int hbWid = (int)(32* Tiles.SCALE.getValue());
        int hbHei = (int)(32*3.5*Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        xOffset = (int)(32*Tiles.SCALE.getValue());
        yOffset = (int)(22*Tiles.SCALE.getValue());
        hitBox.y += yOffset;
        hitBox.x += xOffset;
    }

    public void update() {
        if (animate) updateAnimation();
    }

    public void stop() {
        animIndex = 1;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
