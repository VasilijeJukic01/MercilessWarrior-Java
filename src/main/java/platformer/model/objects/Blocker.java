package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Blocker extends GameObject {

    public Blocker(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        int hbWid = (int)(32*SCALE);
        int hbHei = (int)(32*3.5*SCALE);
        initHitBox(hbWid, hbHei);
        xOffset = (int)(32*SCALE);
        yOffset = (int)(22*SCALE);
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
