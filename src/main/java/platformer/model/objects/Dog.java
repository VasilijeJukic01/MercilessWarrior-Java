package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Dog  extends GameObject {

    public Dog(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(DOG_HB_WID, DOG_HB_HEI);
        super.xOffset = DOG_OFFSET_X;
        super.yOffset = DOG_OFFSET_Y;
    }

    // Core
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
