package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Container extends GameObject{

    public Container(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        if (obj == Obj.BOX) {
            int hbWid = (int)(25*SCALE)+1;
            int hbHei = (int)(18*SCALE)+1;
            initHitBox(hbWid, hbHei);
            xOffset = (int)(7*SCALE);
            yOffset = (int)(12*SCALE);
        }
        else if (obj == Obj.BARREL) {
            int hbWid = (int)(23*SCALE)+1;
            int hbHei = (int)(25*SCALE)+1;
            initHitBox(hbWid, hbHei);
            xOffset = (int)(8*SCALE);
            yOffset = (int)(5*SCALE);
        }
        hitBox.y += yOffset + (int)(6*SCALE);
        hitBox.x += xOffset/2.0;
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
