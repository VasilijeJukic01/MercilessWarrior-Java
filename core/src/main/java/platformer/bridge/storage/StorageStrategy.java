package platformer.bridge.storage;

import platformer.core.Account;
import platformer.model.BoardItem;
import platformer.model.entities.player.Player;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;
import platformer.model.inventory.ShopItem;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for data storage and game logic operations.
 * Implementations will handle either online or offline modes.
 */
public interface StorageStrategy {

    boolean isOnline();

    Account fetchAccountData(String username, int slot);

    void updateAccountData(Account account, int slot);

    List<BoardItem> loadLeaderboardData();

    Map<String, ItemData> getMasterItems();

    List<ShopItem> getShopInventory(String shopId);

    boolean buyItem(Player player, ShopItem selectedItem, int quantity);

    boolean sellItem(Player player, InventoryItem selectedItem, int quantity);
}