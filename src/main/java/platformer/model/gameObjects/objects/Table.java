package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Table extends GameObject {

    private boolean active;

    public Table(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    // Init
    private void generateHitBox() {
        super.animate = true;
        initHitBox(TABLE_HB_WID, TABLE_HB_HEI);
        super.xOffset = TABLE_OFFSET_X;
        super.yOffset = TABLE_OFFSET_Y;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(1*SCALE);
        g.drawImage(animations[animIndex], x, y, TABLE_WID, TABLE_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
        render(g, xLevelOffset, yLevelOffset);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            int infoX = (int)(hitBox.x - xLevelOffset + hitBox.width / 8);
            int infoY = (int)(hitBox.y - yLevelOffset - 2 * SCALE);
            g.drawString("CRAFT", infoX, infoY);
        }
    }


    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
