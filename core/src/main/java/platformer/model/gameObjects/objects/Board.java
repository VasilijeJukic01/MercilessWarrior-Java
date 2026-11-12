package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;


public class Board extends GameObject implements Interactable {

    private boolean active;

    public Board(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(BOARD_HB_WID, BOARD_HB_HEI);
        super.xOffset = BOARD_OFFSET_X;
        super.yOffset = BOARD_OFFSET_Y;
    }

    // Core
    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int) hitBox.x - xOffset - xLevelOffset;
        int y = (int) hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, BOARD_WID, BOARD_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
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
        return "Board";
    }

    @Override
    public void renderInteractionUI(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        int centerX = (int) (hitBox.getCenterX() - xLevelOffset);
        int promptY = (int) (hitBox.y - yLevelOffset - 6 * SCALE);
        UIButtonRenderer.drawInteractionPrompt(g, "READ", centerX, promptY);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
