package platformer.storage;

import platformer.core.Account;
import platformer.model.BoardItem;
import platformer.model.entities.player.Player;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;

import java.util.List;
import java.util.Map;

/**
 * Defines the contract for all game data persistence and business logic operations, implementing the Strategy design pattern.
 * <p>
 * This interface provides a way for the game to interact with data sources, abstracting away the specifics of whether the data is stored in local files or on a remote server.
 */
public interface StorageStrategy {

    /**
     * Checks if the current storage strategy is operating in online mode.
     *
     * @return {@code true} if the strategy involves network communication, {@code false} otherwise.
     */
    boolean isOnline();

    /**
     * Fetches account data. The implementation will determine the source (e.g., local file from a specific slot or an online server).
     *
     * @param username The username for online fetching (can be ignored by offline strategies).
     * @param slot     The local save slot number (can be ignored by online strategies).
     * @return The loaded {@link Account} object, or a default/empty Account if not found or an error occurs.
     */
    Account fetchAccountData(String username, int slot);

    /**
     * Persists the given account data.
     *
     * @param account The {@link Account} object to save.
     * @param slot    The local save slot number to use (can be ignored by online strategies).
     */
    void updateAccountData(Account account, int slot);

    /**
     * Loads the leaderboard data.
     *
     * @return A list of {@link BoardItem}. Returns an empty list if in offline mode or if an error occurs.
     */
    List<BoardItem> loadLeaderboardData();

    /**
     * Retrieves the master list of all item definitions for the game.
     *
     * @return A map of item IDs to their corresponding {@link ItemData}.
     */
    Map<String, ItemData> getMasterItems();

    /**
     * Retrieves the current inventory for a specific shop.
     *
     * @param shopId The unique identifier for the shop.
     * @return A list of {@link ShopItem} available in the shop.
     */
    List<ShopItem> getShopInventory(String shopId);

    /**
     * Executes the business logic for a player purchasing an item.
     * This may involve checking funds, updating player inventory, and communicating with a backend.
     *
     * @param player       The player initiating the purchase.
     * @param selectedItem The {@link ShopItem} being purchased.
     * @param quantity     The number of items to purchase.
     * @return {@code true} if the transaction was successful, {@code false} otherwise.
     */
    boolean buyItem(Player player, ShopItem selectedItem, int quantity);

    /**
     * Executes the business logic for a player selling an item.
     * This may involve updating player inventory and funds, and communicating with a backend.
     *
     * @param player       The player initiating the sale.
     * @param selectedItem The {@link InventoryItem} being sold from the player's backpack.
     * @param quantity     The number of items to sell.
     * @return {@code true} if the transaction was successful, {@code false} otherwise.
     */
    boolean sellItem(Player player, InventoryItem selectedItem, int quantity);
}