package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Table extends GameObject implements Interactable {

    private boolean active;

    public Table(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    // Init
    private void generateHitBox() {
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
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void onEnter(Player player) {
        this.active = true;
    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {
        this.active = false;
    }

    @Override
    public String getInteractionPrompt() {
        return "Table";
    }

    @Override
    public void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        UIButtonRenderer.drawInteractionPrompt(g, "CRAFT", (int) (hitBox.getCenterX() - xLevelOffset), (int) (hitBox.y - yLevelOffset - 6 * SCALE));
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
