package platformer.service;

import platformer.service.rest.requests.*;

import java.io.IOException;
import java.util.List;

/**
 * Defines the contract for an online game service.
 * This interface decouples the application from the specific implementation of the online service (e.g., RESTful API, gRPC).
 */
public interface OnlineService {

    AccountDataDTO fetchAccountData(String username) throws IOException;

    List<BoardItemDTO> loadLeaderboardData() throws IOException;

    void updateAccountData(AccountDataDTO accountDataDTO) throws IOException;

    List<ShopItemDTO> getShopInventory(String shopId) throws IOException;

    ShopTransactionResponse buyItem(ShopTransactionRequest request) throws IOException;

    ShopTransactionResponse sellItem(ShopTransactionRequest request) throws IOException;

    List<ItemMasterDTO> getMasterItems() throws IOException;

}