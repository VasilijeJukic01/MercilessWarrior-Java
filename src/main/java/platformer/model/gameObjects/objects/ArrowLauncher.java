package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class ArrowLauncher extends GameObject {

    private final int yTile;

    public ArrowLauncher(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.yTile = yPos/TILES_SIZE;
        initHitBox(ARROW_TRAP_HB_SIZE, ARROW_TRAP_HB_SIZE);
        if (objType == ObjType.ARROW_TRAP_LEFT) hitBox.x -= ARROW_TRAP_OFFSET;
        else if (objType == ObjType.ARROW_TRAP_RIGHT) hitBox.x += ARROW_TRAP_OFFSET;
        hitBox.y += (int)(2 * SCALE);
    }

    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int fS = 1, fC = 0;
        int sideOffset = 32;
        if (objType == ObjType.ARROW_TRAP_RIGHT) {
            fS = -1;
            fC = ARROW_TRAP_WID;
            sideOffset = 34;
        }
        int x = (int)hitBox.x - xOffset - xLevelOffset + fC - (int)(sideOffset * SCALE);
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(1*SCALE);
        g.drawImage(animations[animIndex], x, y, fS* ARROW_TRAP_WID, ARROW_TRAP_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }

    public int getYTile() {
        return yTile;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
