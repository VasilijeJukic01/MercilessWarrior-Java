package platformer.model.gameObjects.objects;

import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryItem;
import platformer.ui.ItemType;
import platformer.ui.ShopItem;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class Shop extends GameObject {

    private boolean active;
    private final ArrayList<ShopItem> shopItems;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.shopItems = new ArrayList<>();
        generateHitBox();
        initItems();
    }

    // Init
    private void generateHitBox() {
        super.animate = true;
        initHitBox(SHOP_HB_WID, SHOP_HB_HEI);
        super.xOffset = SHOP_OFFSET_X;
        super.yOffset = SHOP_OFFSET_Y;
    }

    private void addShopItem(ItemType type, String imagePath, int minQuantity, int maxQuantity, int cost) {
        int slot = shopItems.size();
        BufferedImage itemImg = Utils.getInstance().importImage(imagePath, -1, -1);
        int randomQuantity = new Random().nextInt(maxQuantity - minQuantity + 1) + minQuantity;
        shopItems.add(new ShopItem(type, itemImg, slot, randomQuantity, cost));
    }

    private void initItems() {
        addShopItem(ItemType.HEALTH,        HEALTH_ITEM, 1, 10, HEALTH_COST);
        addShopItem(ItemType.STAMINA,       STAMINA_ITEM, 1, 6, STAMINA_COST);
        addShopItem(ItemType.IRON,          IRON_ORE_ITEM, 16, 25, IRON_COST);
        addShopItem(ItemType.COPPER,        COPPER_ORE_ITEM, 16, 25, COPPER_COST);
        addShopItem(ItemType.ARMOR_WARRIOR, ARMOR_WARRIOR, 1, 1, ARMOR_WARRIOR_COST);
    }

    // Actions
    public void buyItem(Player player, int slot) {
        for (ShopItem item : shopItems) {
            if (item.getAmount() > 0 && item.getSlot() == slot) {
                if (player.getCoins() >= item.getCost()) {
                    player.changeCoins(-item.getCost());
                    item.setAmount(item.getAmount()-1);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
                    switch(item.getItemType()) {
                        case HEALTH: player.changeHealth(HEALTH_VAL); break;
                        case STAMINA: player.changeStamina(STAMINA_VAL); break;
                        default: addToInventory(player, item.getItemType()); break;
                    }
                }
            }
        }
    }

    private void addToInventory(Player player, ItemType item) {
        Inventory inventory = player.getInventory();

        Optional<InventoryItem> existingItem = inventory.getBackpack().stream()
                .filter(inventoryItem -> inventoryItem.getItemType() == item)
                .findFirst();

        if (existingItem.isPresent()) existingItem.get().addAmount(1);
        else inventory.getBackpack().add(new InventoryItem(item, getImageModel(item), 1));
    }

    // Core
    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset + (int)(1*SCALE);
        g.drawImage(animations[animIndex], x, y, SHOP_WID, SHOP_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.ORANGE);
        render(g, xLevelOffset, yLevelOffset);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, FONT_BIG));
            int infoX = (int)(hitBox.x + hitBox.width / 3 - xLevelOffset);
            int infoY = (int)(hitBox.y - yLevelOffset + 25 * SCALE);
            g.drawString("SHOP", infoX, infoY);
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

    public ArrayList<ShopItem> getShopItems() {
        return shopItems;
    }

    private BufferedImage getImageModel(ItemType type) {
        return shopItems.stream()
                .filter(shopItem -> shopItem.getItemType() == type)
                .map(ShopItem::getItemImage)
                .findFirst()
                .orElse(null);
    }

}
