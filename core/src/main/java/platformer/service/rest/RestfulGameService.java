package platformer.service.rest;

import platformer.service.OnlineService;
import platformer.service.rest.client.GameServiceClient;
import platformer.service.rest.requests.*;

import java.io.IOException;
import java.util.List;

/**
 * Concrete implementation of the OnlineService using a RESTful API.
 * This class uses GameServiceClient to handle HTTP requests.
 */
public class RestfulGameService implements OnlineService {

    private final GameServiceClient gameServiceClient;

    public RestfulGameService(GameServiceClient gameServiceClient) {
        this.gameServiceClient = gameServiceClient;
    }

    @Override
    public AccountDataDTO fetchAccountData(String username) throws IOException {
        return gameServiceClient.fetchAccountData(username);
    }

    @Override
    public List<BoardItemDTO> loadLeaderboardData() throws IOException {
        return gameServiceClient.loadLeaderboardData();
    }

    @Override
    public void updateAccountData(AccountDataDTO accountDataDTO) throws IOException {
        gameServiceClient.updateAccountData(accountDataDTO);
    }

    @Override
    public List<ShopItemDTO> getShopInventory(String shopId) throws IOException {
        return gameServiceClient.getShopInventory(shopId);
    }

    @Override
    public ShopTransactionResponse buyItem(ShopTransactionRequest request) throws IOException {
        return gameServiceClient.buyItem(request);
    }

    @Override
    public ShopTransactionResponse sellItem(ShopTransactionRequest request) throws IOException {
        return gameServiceClient.sellItem(request);
    }

    @Override
    public List<ItemMasterDTO> getMasterItems() throws IOException {
        return gameServiceClient.getMasterItems();
    }

}