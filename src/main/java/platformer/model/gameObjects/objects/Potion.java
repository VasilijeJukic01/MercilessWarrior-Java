package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Potion extends GameObject {

    private double floatOffset;
    private final int maxFloatOffset;
    private int floatDir = 1;

    public Potion(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        super.animate = true;
        initHitBox(POTION_HB_WID, POTION_HB_HEI);
        super.xOffset = POTION_OFFSET_X;
        super.yOffset = POTION_OFFSET_Y;
        this.maxFloatOffset = (int)(5*SCALE);
    }

    private void updateFloating() {
        floatOffset += (0.065 * SCALE * floatDir);
        if (floatOffset >= maxFloatOffset) floatDir = -1;
        else if (floatOffset < 0) floatDir = 1;
        hitBox.y = yPos+floatOffset;
    }

    @Override
    public void update() {
        updateAnimation();
        updateFloating();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, POTION_WID, POTION_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
       renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
