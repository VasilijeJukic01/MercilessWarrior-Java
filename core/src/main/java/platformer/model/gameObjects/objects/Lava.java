package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Lava extends GameObject {

    public Lava(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        super.animSpeed = 14;
        super.animIndex = new Random().nextInt(15);
        initHitBox(LAVA_HB_WID, LAVA_HB_HEI);
        super.xOffset = LAVA_OFFSET_X;
        super.yOffset = LAVA_OFFSET_Y;
        hitBox.y += yOffset;
    }

    @Override
    public void update() {
        updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(3 * SCALE);
        g.drawImage(animations[animIndex], x, y, LAVA_WID, LAVA_HEI, null);
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

