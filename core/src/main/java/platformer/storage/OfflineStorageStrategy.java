package platformer.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import platformer.core.Account;
import platformer.model.BoardItem;
import platformer.model.entities.player.Player;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;
import platformer.serialization.GameSerializer;
import platformer.serialization.Serializer;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

import static platformer.constants.FilePaths.ITEMS_PATH;
import static platformer.constants.FilePaths.SHOP_INV_PATH;

/**
 * An implementation of {@link StorageStrategy} for playing the game in offline mode.
 * <p>
 * This class handles all data persistence and business logic locally, without any network communication.
 * <ul>
 *   <li>It uses a {@link platformer.serialization.Serializer} to read and write encrypted {@link Account} data to local save files.</li>
 *   <li>Master item definitions and shop inventories are loaded from JSON files located within the application's resources.</li>
 *   <li>Business logic, such as shop transactions, is handled directly by manipulating the player's state.</li>
 * </ul>
 *
 * @see StorageStrategy
 * @see OnlineStorageStrategy
 * @see GameSerializer
 */
public class OfflineStorageStrategy implements StorageStrategy {

    private final Serializer<Account, List<Account>> serializer;
    private Map<String, ItemData> masterItems;
    private List<ShopItem> shopInventory;

    public OfflineStorageStrategy() {
        this.serializer = new GameSerializer();
        loadLocalData();
    }

    private void loadLocalData() {
        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(ITEMS_PATH)))) {
            Type type = new TypeToken<Map<String, ItemData>>() {}.getType();
            this.masterItems = new Gson().fromJson(reader, type);
        } catch (Exception e) {
            this.masterItems = new HashMap<>();
        }

        try (InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream(SHOP_INV_PATH)))) {
            Type type = new TypeToken<Map<String, List<Map<String, Object>>>>() {}.getType();
            Map<String, List<Map<String, Object>>> allShops = new Gson().fromJson(reader, type);

            List<Map<String, Object>> itemsForShop = allShops.get("DEFAULT_SHOP");
            this.shopInventory = new ArrayList<>();
            if (itemsForShop != null) {
                for (Map<String, Object> itemMap : itemsForShop) {
                    String itemId = (String) itemMap.get("itemId");
                    int stock = ((Double) itemMap.get("stock")).intValue();
                    int cost = ((Double) itemMap.get("cost")).intValue();
                    shopInventory.add(new ShopItem(itemId, stock, cost));
                }
            }
        } catch (Exception e) {
            this.shopInventory = new ArrayList<>();
        }
    }

    @Override
    public boolean isOnline() {
        return false;
    }

    @Override
    public Account fetchAccountData(String username, int slot) {
        List<Account> saves = serializer.deserialize();
        if (slot >= 1 && slot <= saves.size()) {
            Account account = saves.get(slot - 1);
            return (account != null) ? account : new Account();
        }
        return new Account();
    }

    @Override
    public void updateAccountData(Account account, int slot) {
        serializer.serialize(account, slot);
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        return Collections.emptyList();
    }

    @Override
    public Map<String, ItemData> getMasterItems() {
        return this.masterItems;
    }

    @Override
    public List<ShopItem> getShopInventory(String shopId) {
        return this.shopInventory;
    }

    @Override
    public boolean buyItem(Player player, ShopItem selectedItem, int quantity) {
        int totalCost = selectedItem.getCost() * quantity;
        return player.getCoins() >= totalCost && selectedItem.getStock() >= quantity;
    }

    @Override
    public boolean sellItem(Player player, InventoryItem selectedItem, int quantity) {
        ItemData data = selectedItem.getData();
        return data != null && selectedItem.getAmount() >= quantity;
    }
}