package platformer.model.objects;

import platformer.model.Tiles;

import java.awt.*;

public class Potion extends GameObject{

    private double floatOffset;
    private final int maxFloatOffset;
    private int floatDir = 1;

    public Potion(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        super.animate = true;
        int hbWid = (int)(7*Tiles.SCALE.getValue());
        int hbHei = (int)(14*Tiles.SCALE.getValue());
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(3*Tiles.SCALE.getValue());
        super.yOffset = (int)(2*Tiles.SCALE.getValue());
        this.maxFloatOffset = (int)(5*Tiles.SCALE.getValue());
    }

    private void updateFloating() {
        floatOffset += (0.065 * Tiles.SCALE.getValue() * floatDir);
        if (floatOffset >= maxFloatOffset) floatDir = -1;
        else if (floatOffset < 0) floatDir = 1;
        hitBox.y = yPos+floatOffset;
    }

    public void update() {
        updateAnimation();
        updateFloating();
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
       renderHitBox(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
