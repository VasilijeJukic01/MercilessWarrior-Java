package platformer.model.gameObjects.objects;

import platformer.model.entities.enemies.EnemyType;
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

    public Loot(ObjType objType, int xPos, int yPos, EnemyType enemyType) {
        super(objType, xPos, yPos);
        generateHitBox();
        randomizeItems(enemyType);
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(LOOT_HB_WID, LOOT_HB_HEI);
        super.xOffset = LOOT_OFFSET_X;
        super.yOffset = LOOT_OFFSET_Y;
    }

    private void randomizeItems(EnemyType enemyType) {
        if (enemyType == EnemyType.SPEAR_WOMAN) generateBossLoot();
        else generateStandardLoot();

    }

    private void generateStandardLoot() {
        Random rand = new Random();
        for (ItemType itemType : validLoot) {
            int chance = rand.nextInt(100);
            if (chance < 70) {
                int amount = 0;
                switch (itemType.getRarity()) {
                    case COMMON: amount = rand.nextInt(8); break;
                    case UNCOMMON: amount = rand.nextInt(4); break;
                    case RARE: amount = rand.nextInt(2); break;
                    default: break;
                }
                if (amount < 1) continue;
                BufferedImage img = Utils.getInstance().importImage(itemType.getImg(), -1, -1);
                items.add(new InventoryItem(itemType, img, amount));
            }
        }
    }

    private void generateBossLoot() {
        BufferedImage img = Utils.getInstance().importImage(ItemType.CHARM_THUNDERBOLT.getImg(), -1, -1);
        items.add(new InventoryItem(ItemType.CHARM_THUNDERBOLT, img, 1));
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
