package platformer.core.initializer;

import platformer.service.rest.client.GameServiceClient;
import platformer.service.rest.mapper.AccountMapper;
import platformer.service.rest.mapper.LeaderboardMapper;
import platformer.service.rest.requests.ItemMasterDTO;
import platformer.service.rest.requests.ShopItemDTO;
import platformer.storage.OfflineStorageStrategy;
import platformer.storage.OnlineStorageStrategy;
import platformer.storage.StorageStrategy;
import platformer.service.OnlineService;
import platformer.service.rest.RestfulGameService;
import platformer.core.config.GameLaunchConfig;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.inventory.database.GameDataCache;

import java.io.IOException;
import java.util.List;

/**
 * Initializes game data based on the launch configuration.
 * If an auth token is present, it fetches data online; otherwise, it initializes offline storage.
 */
public class GameDataInitializer {

    public StorageStrategy initialize(GameLaunchConfig config) {
        if (config.authToken() != null) return initializeOnline();
        else return initializeOffline();
    }

    private StorageStrategy initializeOnline() {
        try {
            GameServiceClient client = new GameServiceClient();
            OnlineService service = new RestfulGameService(client);

            List<ItemMasterDTO> masterItems = service.getMasterItems();
            List<ShopItemDTO> shopInventory = service.getShopInventory("DEFAULT_SHOP");
            GameDataCache.getInstance().cacheItemData(masterItems);
            GameDataCache.getInstance().cacheShopInventory("DEFAULT_SHOP", shopInventory);
            AccountMapper accountMapper = new AccountMapper();
            LeaderboardMapper leaderboardMapper = new LeaderboardMapper();

            Logger.getInstance().notify("Online data loaded successfully.", Message.INFORMATION);

            return new OnlineStorageStrategy(service, accountMapper, leaderboardMapper);
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to fetch online game data: " + e.getMessage() + ". Switching to offline mode.", Message.WARNING);
            return initializeOffline();
        }
    }

    private StorageStrategy initializeOffline() {
        Logger.getInstance().notify("Initializing in offline mode.", Message.INFORMATION);
        OfflineStorageStrategy offlineStrategy = new OfflineStorageStrategy();
        GameDataCache.getInstance().cacheItemDataFromMap(offlineStrategy.getMasterItems());
        GameDataCache.getInstance().cacheShopInventoryFromList("DEFAULT_SHOP", offlineStrategy.getShopInventory("DEFAULT_SHOP"));
        return offlineStrategy;
    }

}