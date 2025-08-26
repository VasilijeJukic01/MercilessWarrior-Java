package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.item.InventoryItem;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static platformer.constants.Constants.*;

public class Loot extends GameObject implements Interactable {

    private boolean active;
    private final List<InventoryItem> items = new ArrayList<>();

    public Loot(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(LOOT_HB_WID, LOOT_HB_HEI);
        super.xOffset = LOOT_OFFSET_X;
        super.yOffset = LOOT_OFFSET_Y;
    }

    @Override
    public void update() {
        if (alive && items.isEmpty()) alive = false;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int) hitBox.x - xOffset - xLevelOffset;
        int y = (int) hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, LOOT_WID, LOOT_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
        renderText(g, xLevelOffset, yLevelOffset);
    }

    private void renderText(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            int infoX = (int)(hitBox.x - xLevelOffset + 2 * SCALE);
            int infoY = (int)(hitBox.y - yLevelOffset);
            g.drawString("LOOT", infoX, infoY);
        }
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
        return "Loot";
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<InventoryItem> getItems() {
        return items;
    }

}
