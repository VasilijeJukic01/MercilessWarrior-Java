package platformer.model.gameObjects.objects;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemType;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Loot extends GameObject {

    private boolean active;
    private final List<InventoryItem> items = new ArrayList<>();
    private final List<ItemType> validLoot = List.of(ItemType.COPPER, ItemType.IRON);

    public Loot(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        generateHitBox();
        randomizeItems();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(LOOT_HB_WID, LOOT_HB_HEI);
        super.xOffset = LOOT_OFFSET_X;
        super.yOffset = LOOT_OFFSET_Y;
    }

    private void randomizeItems() {
        Random rand = new Random();
        for (ItemType itemType : validLoot) {
            int chance = rand.nextInt(100);
            if (chance < 50) {
                int amount = rand.nextInt(10);
                if (amount < 1) continue;
                BufferedImage img = Utils.getInstance().importImage(itemType.getImg(), -1, -1);
                items.add(new InventoryItem(itemType, img, amount));
            }
        }
    }

    @Override
    public void update() {

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
            int infoX = (int)(hitBox.x - xLevelOffset);
            int infoY = (int)(hitBox.y - yLevelOffset - 5 * SCALE);
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<InventoryItem> getItems() {
        return items;
    }

}
