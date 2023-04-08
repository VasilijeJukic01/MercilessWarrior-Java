package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Spike extends GameObject{

    public Spike(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        int hbWid = (int)(32*Tiles.SCALE.getValue());
        int hbHei = (int)(15*Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        xOffset = 0;
        yOffset = (int)(22*Tiles.SCALE.getValue());
        hitBox.y += yOffset;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
