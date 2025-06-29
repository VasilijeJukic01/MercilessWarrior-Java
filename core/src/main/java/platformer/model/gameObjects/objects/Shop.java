package platformer.model.gameObjects.objects;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemType;
import platformer.model.inventory.ShopItem;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestManager;
import platformer.model.quests.QuestObjectiveType;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static platformer.constants.Constants.*;

@SuppressWarnings("unchecked")
public class Shop extends GameObject implements Publisher {

    private static final List<Subscriber> subscribers = new ArrayList<>();

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

    private void addShopItem(ItemType type, int minQuantity, int maxQuantity, int cost) {
        BufferedImage itemImg = Utils.getInstance().importImage(type.getImg(), -1, -1);
        int randomQuantity = new Random().nextInt(maxQuantity - minQuantity + 1) + minQuantity;
        shopItems.add(new ShopItem(type, itemImg, randomQuantity, cost));
    }

    private void initItems() {
        addShopItem(ItemType.HEALTH,            1, 10, HEALTH_COST);
        addShopItem(ItemType.STAMINA,           1, 6, STAMINA_COST);
        addShopItem(ItemType.IRON,              16, 25, IRON_COST);
        addShopItem(ItemType.SILVER,            16, 25, SILVER_COST);
        addShopItem(ItemType.COPPER,            16, 25, COPPER_COST);
        addShopItem(ItemType.AMETHYST,          1, 5, AMETHYST_COST);
        addShopItem(ItemType.SONIC_QUARTZ,      1, 3, SONIC_QUARTZ_COST);
        addShopItem(ItemType.MAGMA,             1, 4, MAGMA_COST);
        addShopItem(ItemType.AZURELITE,         1, 2, AZURELITE_COST);
        addShopItem(ItemType.ELECTRICITE,       1, 1, ELECTRICITE_COST);
        addShopItem(ItemType.ROSALLIUM,         1, 1, ROSALLIUM_COST);

        addShopItem(ItemType.HELMET_WARRIOR,    1, 1, HELMET_WARRIOR_COST);
        addShopItem(ItemType.ARMOR_WARRIOR,     1, 1, ARMOR_WARRIOR_COST);
        addShopItem(ItemType.BRACELETS_WARRIOR, 1, 1, BRACELETS_WARRIOR_COST);
        addShopItem(ItemType.TROUSERS_WARRIOR,  1, 1, TROUSERS_WARRIOR_COST);
        addShopItem(ItemType.BOOTS_WARRIOR,     1, 1, BOOTS_WARRIOR_COST);
    }

    // Actions
    public void buyItem(Player player, int slot) {
        if (slot >= shopItems.size()) return;
        ShopItem item = shopItems.get(slot);
        if (player.getCoins() >= item.getCost()) {
            player.changeCoins(-item.getCost());
            item.setAmount(item.getAmount()-1);
            if (item.getAmount() == 0) shopItems.remove(item);
            Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
            switch(item.getItemType()) {
                case HEALTH: player.changeHealth(HEALTH_VAL); break;
                case STAMINA: player.changeStamina(STAMINA_VAL); break;
                default: addToInventory(player, item.getItemType()); break;
            }
        }
    }

    public void sellItem(Player player, int slot) {
        Inventory inventory = player.getInventory();
        if (slot >= inventory.getBackpack().size()) return;
        InventoryItem item = inventory.getBackpack().get(slot);
        if (item.getAmount() > 0) {
            player.changeCoins(item.getItemType().getSellValue());
            item.setAmount(item.getAmount()-1);
            if (item.getAmount() == 0) inventory.getBackpack().remove(item);
            Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
            addToShop(item);
        }
    }

    private void addToInventory(Player player, ItemType item) {
        Inventory inventory = player.getInventory();

        Optional<InventoryItem> existingItem = inventory.getBackpack().stream()
                .filter(inventoryItem -> inventoryItem.getItemType() == item)
                .findFirst();

        if (item == ItemType.ARMOR_WARRIOR) notify(QuestObjectiveType.COLLECT, ObjectiveTarget.BUY_ARMOR);

        if (existingItem.isPresent()) existingItem.get().addAmount(1);
        else inventory.getBackpack().add(new InventoryItem(item, getImageModel(item), 1));
    }

    private void addToShop(InventoryItem inventoryItem) {
        Optional<ShopItem> existingItem = shopItems.stream()
                .filter(shopItem -> shopItem.getItemType() == inventoryItem.getItemType())
                .findFirst();

        if (existingItem.isPresent()) existingItem.get().addAmount(1);
        else shopItems.add(new ShopItem(inventoryItem.getItemType(), inventoryItem.getModel(), 1, inventoryItem.getItemType().getSellValue()));
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
        return Utils.getInstance().importImage(type.getImg(), -1, -1);
    }

    @Override
    public void addSubscriber(Subscriber s) {
        subscribers.add(s);
    }

    @Override
    public void removeSubscriber(Subscriber s) {
        subscribers.remove(s);
    }

    @Override
    public <T> void notify(T... o) {
        subscribers.stream()
                .filter(s -> s instanceof QuestManager)
                .findFirst()
                .ifPresent(s -> s.update(o));
    }
}
