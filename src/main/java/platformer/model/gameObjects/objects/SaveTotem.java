package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class SaveTotem extends GameObject {

    private boolean active;

    public SaveTotem(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(SAVE_TOTEM_HB_WID, SAVE_TOTEM_HB_HEI);
        super.xOffset = SAVE_TOTEM_OFFSET_X;
        super.yOffset = SAVE_TOTEM_OFFSET_Y;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, SAVE_TOTEM_WID, SAVE_TOTEM_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
        renderText(g, xLevelOffset, yLevelOffset);
    }

    private void renderText(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            int infoX = (int)(hitBox.x - hitBox.width / 3 - xLevelOffset);
            int infoY = (int)(hitBox.y - yLevelOffset - 5 * SCALE);
            g.drawString("SAVE GAME", infoX, infoY);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
