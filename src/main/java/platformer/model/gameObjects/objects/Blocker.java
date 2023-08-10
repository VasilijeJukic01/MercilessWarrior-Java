package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

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

    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x  - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(12*SCALE);
        g.drawImage(animations[animIndex], x, y, BLOCKER_WID, BLOCKER_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
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
