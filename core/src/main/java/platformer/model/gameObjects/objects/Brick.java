package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Brick extends GameObject {

    public Brick(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos - (int)(BRICK_HEI/2.2));
        initHitBox(BRICK_HB_WID, BRICK_HB_HEI);
        super.xOffset = BRICK_OFFSET_X;
        super.yOffset = BRICK_OFFSET_Y;
        hitBox.y += yOffset;
    }

    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(BRICK_HEI/4.7);
        g.drawImage(animations[animIndex], x, y, BRICK_WID, BRICK_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
