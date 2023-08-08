package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Spike extends GameObject {

    public Spike(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        initHitBox(SPIKE_HB_WID, SPIKE_HB_HEI);
        super.xOffset = SPIKE_OFFSET_X;
        super.yOffset = SPIKE_OFFSET_Y;
        hitBox.y += yOffset;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(12*SCALE);
        g.drawImage(animations[4], x, y, SPIKE_WID, SPIKE_HEI, null);
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
