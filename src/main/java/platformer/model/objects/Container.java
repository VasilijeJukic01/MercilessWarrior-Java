package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Container extends GameObject{

    public Container(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        if (objType == ObjType.BOX) {
            int hbWid = (int)(25*Tiles.SCALE.getValue())+1;
            int hbHei = (int)(18*Tiles.SCALE.getValue())+1;
            initHitBox(hbWid, hbHei);
            xOffset = (int)(7*Tiles.SCALE.getValue());
            yOffset = (int)(12*Tiles.SCALE.getValue());
        }
        else if (objType == ObjType.BARREL) {
            int hbWid = (int)(23*Tiles.SCALE.getValue())+1;
            int hbHei = (int)(25*Tiles.SCALE.getValue())+1;
            initHitBox(hbWid, hbHei);
            xOffset = (int)(8*Tiles.SCALE.getValue());
            yOffset = (int)(5*Tiles.SCALE.getValue());
        }
        hitBox.y += yOffset + (int)(6*Tiles.SCALE.getValue());
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
