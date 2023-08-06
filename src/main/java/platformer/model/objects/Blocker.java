package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Blocker extends GameObject {

    public Blocker(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        initHitBox(BLOCKER_HB_WID, BLOCKER_HB_HEI);
        super.xOffset = BLOCKER_OFFSET_X;
        super.yOffset = BLOCKER_OFFSET_Y;
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
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
