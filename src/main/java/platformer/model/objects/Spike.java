package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Spike extends GameObject{

    public Spike(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        initHitBox(SPIKE_HB_WID, SPIKE_HB_HEI);
        super.xOffset = SPIKE_OFFSET_X;
        super.yOffset = SPIKE_OFFSET_Y;
        hitBox.y += yOffset;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
