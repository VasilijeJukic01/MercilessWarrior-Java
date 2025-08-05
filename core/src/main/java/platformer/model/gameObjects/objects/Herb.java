package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Represents a harvestable herb object in the game.
 */
public class Herb extends GameObject implements Interactable {

    private boolean active;

    public Herb(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        initHitBox(HERB_HB_WID, HERB_HB_HEI);
        super.xOffset = HERB_OFFSET_X;
        super.yOffset = HERB_OFFSET_Y;
    }

    @Override
    public void update() { }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int drawX = (int) (hitBox.x - xOffset - xLevelOffset);
        int drawY = (int) (hitBox.y - yOffset - yLevelOffset);

        g.drawImage(animations[0], drawX, drawY, HERB_WID, HERB_HEI, null);
        renderText(g, xLevelOffset, yLevelOffset);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
    }

    private void renderText(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            int infoX = (int)(hitBox.x - xLevelOffset - 8 * SCALE);
            int infoY = (int)(hitBox.y - yLevelOffset - 5 * SCALE);
            g.drawString("HARVEST", infoX, infoY);
        }
    }

    public void setActive(boolean active) {
        this.active = active;
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
        return "Herb";
    }
}
