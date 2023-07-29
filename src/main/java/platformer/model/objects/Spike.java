package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Spike extends GameObject{

    public Spike(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        int hbWid = (int)(32*SCALE);
        int hbHei = (int)(15*SCALE);
        initHitBox(hbWid, hbHei);
        xOffset = 0;
        yOffset = (int)(22*SCALE);
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
