package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.InventoryItem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;

public class Container extends GameObject {

    private final List<InventoryItem> items = new ArrayList<>();

    public Container(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        if (objType == ObjType.BOX) {
            initHitBox(BOX_HB_WID, BOX_HB_HEI);
            super.xOffset = BOX_OFFSET_X;
            super.yOffset = BOX_OFFSET_Y;
        }
        else if (objType == ObjType.BARREL) {
            initHitBox(BARREL_HB_WID, BARREL_HB_HEI);
            super.xOffset = BARREL_OFFSET_X;
            super.yOffset = BARREL_OFFSET_Y;
        }
        hitBox.y += yOffset + (int)(6 * SCALE);
        hitBox.x += xOffset / 2.0;
    }

    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, CONTAINER_WID, CONTAINER_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public List<InventoryItem> getItems() {
        return items;
    }
}
