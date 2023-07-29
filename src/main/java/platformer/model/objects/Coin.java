package platformer.model.objects;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Coin extends GameObject{

    public Coin(Obj obj, int xPos, int yPos) {
        super(obj, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        int hbWid = (int)(10 * SCALE);
        int hbHei = (int)(10 * SCALE);
        initHitBox(hbWid, hbHei);
        super.xOffset = (int)(3 * SCALE);
        super.yOffset = (int)(3 * SCALE);
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
