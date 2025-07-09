package platformer.model.gameObjects.objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import platformer.model.inventory.*;
import platformer.model.quests.ObjectiveTarget;
import platformer.model.quests.QuestManager;
import platformer.model.quests.QuestObjectiveType;
import platformer.observer.Publisher;
import platformer.observer.Subscriber;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SHOP_INV_PATH;

@SuppressWarnings("unchecked")
public class Shop extends GameObject implements Publisher {

    private static final List<Subscriber> subscribers = new ArrayList<>();

    private boolean active;
    private final ArrayList<ShopItem> shopItems;

    public Shop(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        this.shopItems = new ArrayList<>();
        generateHitBox();
        loadShopInventory("DEFAULT_SHOP");
    }

    // Init
    private void generateHitBox() {
        super.animate = true;
        initHitBox(SHOP_HB_WID, SHOP_HB_HEI);
        super.xOffset = SHOP_OFFSET_X;
        super.yOffset = SHOP_OFFSET_Y;
    }

    private void loadShopInventory(String shopId) {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(SHOP_INV_PATH)))) {
            Type type = new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType();
            Map<String, List<Map<String, Object>>> allShops = new Gson().fromJson(reader, type);

            List<Map<String, Object>> itemsForShop = allShops.get(shopId);
            if (itemsForShop != null) {
                for (Map<String, Object> itemMap : itemsForShop) {
                    String itemId = (String) itemMap.get("itemId");
                    int stock = ((Double) itemMap.get("stock")).intValue();
                    int cost = ((Double) itemMap.get("cost")).intValue();
                    shopItems.add(new ShopItem(itemId, stock, cost));
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Error loading shop inventory!", Message.ERROR);
        }
    }

    // Actions
    public void buyItem(Player player, int slot) {
        if (slot >= shopItems.size()) return;
        ShopItem item = shopItems.get(slot);
        if (player.getCoins() >= item.getCost()) {
            player.changeCoins(-item.getCost());
            item.setStock(item.getStock() - 1);
            if (item.getStock() == 0) shopItems.remove(item);
            Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);

            // TODO: Change healing logic
            if (item.getItemId().equals("HEALTH")) player.changeHealth(HEALTH_VAL);
            else if (item.getItemId().equals("STAMINA")) player.changeStamina(STAMINA_VAL);
            else addToInventory(player, item.getItemId());
        }
    }

    public void sellItem(Player player, int slot) {
        Inventory inventory = player.getInventory();
        if (slot >= inventory.getBackpack().size()) return;
        InventoryItem itemToSell = inventory.getBackpack().get(slot);

        if (itemToSell.getAmount() > 0) {
            ItemData data = itemToSell.getData();
            if (data == null) return;
            player.changeCoins(data.sellValue);
            itemToSell.setAmount(itemToSell.getAmount() - 1);
            if (itemToSell.getAmount() == 0) inventory.getBackpack().remove(itemToSell);
            Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
            addToShop(itemToSell);
        }
    }

    private void addToInventory(Player player, String itemId) {
        Inventory inventory = player.getInventory();
        Optional<InventoryItem> existingItem = inventory.getBackpack().stream()
                .filter(invItem -> invItem.getItemId().equals(itemId))
                .findFirst();

        if (itemId.equals("ARMOR_WARRIOR")) notify(QuestObjectiveType.COLLECT, ObjectiveTarget.BUY_ARMOR);
        if (existingItem.isPresent()) existingItem.get().addAmount(1);
        else inventory.getBackpack().add(new InventoryItem(itemId, 1));
    }

    private void addToShop(InventoryItem inventoryItem) {
        Optional<ShopItem> existingItem = shopItems.stream()
                .filter(shopItem -> shopItem.getItemId().equals(inventoryItem.getItemId()))
                .findFirst();

        ItemData data = inventoryItem.getData();
        if (data == null) return;
        if (existingItem.isPresent()) existingItem.get().addStock(1);
        else shopItems.add(new ShopItem(inventoryItem.getItemId(), 1, data.sellValue));
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
