package platformer.bridge;

import platformer.bridge.requests.ShopItemDTO;
import platformer.bridge.requests.ShopTransactionRequest;
import platformer.bridge.requests.ShopTransactionResponse;
import platformer.core.Account;
import platformer.model.BoardItem;

import java.io.IOException;
import java.util.List;

/**
 * Interface for the connector between the game and services.
 */
public interface Bridge {

    /**
     * Creates a new account with the given username and password.
     *
     * @param username the username of the account
     * @param password the password of the account
     * @return the account id
     */
    int createAccount(String username, String password);

    /**
     * Loads the account data for the given username.
     *
     * @param username the username of the account
     * @return the account data
     */
    Account fetchAccountData(String username);

    /**
     * Loads the account data for the given account id.
     *
     * @return the account data
     */
    List<BoardItem> loadLeaderboardData();

    /**
     * Updates the account data for the given account.
     *
     * @param account the account data
     */
    void updateAccountData(Account account);

    /**
     * Fetches the inventory for a specific shop from the game service.
     *
     * @param shopId the ID of the shop
     * @return a list of shop items
     * @throws IOException if a network error occurs
     */
    List<ShopItemDTO> getShopInventory(String shopId) throws IOException;

    /**
     * Performs a buy transaction in the shop.
     *
     * @param request the shop transaction request containing details of the transaction
     * @return the transaction response with a message and the updated shop inventory
     */
    ShopTransactionResponse buyItem(ShopTransactionRequest request) throws IOException;

    /**
     * Performs a sell transaction in the shop.
     *
     * @param request the shop transaction request containing details of the transaction
     * @return the transaction response with a message and the updated shop inventory
     */
    ShopTransactionResponse sellItem(ShopTransactionRequest request) throws IOException;

}
