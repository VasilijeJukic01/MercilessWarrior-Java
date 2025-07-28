package platformer.model.inventory;

import platformer.bridge.requests.ItemMasterDTO;
import platformer.bridge.requests.ShopItemDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A singleton cache to hold game data fetched from the server, such as master item lists and shop inventories.
 * This prevents repeated API calls during a game session.
 */
public class GameDataCache {

    private static volatile GameDataCache instance;

    private Map<String, ItemData> itemDataCache;
    private final Map<String, List<ShopItemDTO>> shopInventoryCache = new HashMap<>();

    private GameDataCache() {}

    public static GameDataCache getInstance() {
        if (instance == null) {
            synchronized (GameDataCache.class) {
                if (instance == null) {
                    instance = new GameDataCache();
                }
            }
        }
        return instance;
    }

    public void cacheItemData(List<ItemMasterDTO> items) {
        if (this.itemDataCache == null) {
            this.itemDataCache = new HashMap<>();
        }
        this.itemDataCache.clear();
        for (ItemMasterDTO dto : items) {
            this.itemDataCache.put(dto.getItemId(), convertToItemData(dto));
        }
    }

    public void cacheShopInventory(String shopId, List<ShopItemDTO> inventory) {
        this.shopInventoryCache.put(shopId, inventory);
    }

    public Map<String, ItemData> getItemData() {
        return itemDataCache;
    }

    public List<ShopItemDTO> getShopInventory(String shopId) {
        return shopInventoryCache.get(shopId);
    }

    public boolean isItemDataCached() {
        return itemDataCache != null && !itemDataCache.isEmpty();
    }

    public boolean isShopCached(String shopId) {
        return shopInventoryCache.containsKey(shopId);
    }

    private ItemData convertToItemData(ItemMasterDTO dto) {
        ItemData data = new ItemData();
        data.name = dto.getName();
        data.description = dto.getDescription();
        data.rarity = ItemRarity.valueOf(dto.getRarity());
        data.imagePath = dto.getImagePath();
        data.sellValue = dto.getSellValue();
        data.stackable = dto.isStackable();
        if (dto.getEquip() != null) {
            ItemData.EquipmentData equipData = new ItemData.EquipmentData();
            equipData.canEquip = dto.getEquip().isCanEquip();
            equipData.slot = dto.getEquip().getSlot();
            equipData.bonuses = dto.getEquip().getBonuses();
            data.equip = equipData;
        }
        return data;
    }

}