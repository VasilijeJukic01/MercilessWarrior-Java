package platformer.model.gameObjects.objects;

import platformer.model.entities.enemies.EnemyType;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.database.ItemDatabase;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Loot extends GameObject {

    private boolean active;
    private final List<InventoryItem> items = new ArrayList<>();
    private static final Map<EnemyType, List<String>> lootMap = Map.of(
            EnemyType.SKELETON, List.of("COPPER_ORE", "IRON_ORE"),
            EnemyType.GHOUL, List.of("COPPER_ORE", "SILVER_ORE", "WRAITH_ESSENCE"),
            EnemyType.KNIGHT, List.of("SONIC_QUARTZ_ORE", "IRON_ORE"),
            EnemyType.WRAITH, List.of("COPPER_ORE", "SILVER_ORE", "WRAITH_ESSENCE")
    );

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
        if (enemyType == EnemyType.LANCER) generateBossLoot();
        else generateStandardLoot(enemyType);

    }

    private void generateStandardLoot(EnemyType enemyType) {
        Random rand = new Random();
        List<String> possibleLoot = lootMap.get(enemyType);
        if (possibleLoot == null) return;

        for (String itemId : possibleLoot) {
            int chance = rand.nextInt(100);
            if (chance < 70) {
                ItemData itemData = ItemDatabase.getInstance().getItemData(itemId);
                if (itemData == null) continue;
                int amount = 0;
                switch (itemData.rarity) {
                    case COMMON: amount = rand.nextInt(8); break;
                    case UNCOMMON: amount = rand.nextInt(4); break;
                    case RARE: amount = rand.nextInt(2); break;
                    default: break;
                }
                if (amount >= 1) items.add(new InventoryItem(itemId, amount));
            }
        }
    }

    private void generateBossLoot() {
        items.add(new InventoryItem("CHARM_THUNDERBOLT", 1));
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public List<InventoryItem> getItems() {
        return items;
    }

}
