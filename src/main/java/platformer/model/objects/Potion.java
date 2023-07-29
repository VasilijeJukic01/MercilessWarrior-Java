package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Potion extends GameObject{

    private double floatOffset;
    private final int maxFloatOffset;
    private int floatDir = 1;

    public Potion(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        super.animate = true;
        int hbWid = (int)(7*SCALE);
        int hbHei = (int)(14*SCALE);
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(3*SCALE);
        super.yOffset = (int)(2*SCALE);
        this.maxFloatOffset = (int)(5*SCALE);
    }

    private void updateFloating() {
        floatOffset += (0.065 * SCALE * floatDir);
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
