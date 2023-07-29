package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Dog  extends GameObject {

    public Dog(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(32 * SCALE);
        int hbHei = (int)(32 * SCALE);
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(18 * SCALE);
        super.yOffset = (int)(12 * SCALE);
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
