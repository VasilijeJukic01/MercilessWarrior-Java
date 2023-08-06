package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Coin extends GameObject{

    public Coin(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(COIN_HB_SIZE, COIN_HB_SIZE);
        super.xOffset = COIN_OFFSET;
        super.yOffset = COIN_OFFSET;
    }

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
