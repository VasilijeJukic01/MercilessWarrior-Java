package platformer.core.initializer;

import platformer.bridge.Connector;
import platformer.bridge.client.GameServiceClient;
import platformer.bridge.requests.ItemMasterDTO;
import platformer.bridge.requests.ShopItemDTO;
import platformer.bridge.storage.OfflineStorageStrategy;
import platformer.bridge.storage.OnlineStorageStrategy;
import platformer.bridge.storage.StorageStrategy;
import platformer.core.config.GameLaunchConfig;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.inventory.GameDataCache;

import java.io.IOException;
import java.util.List;

/**
 * Initializes game data based on the launch configuration.
 * If an auth token is present, it fetches data online; otherwise, it initializes offline storage.
 */
public class GameDataInitializer {

    public StorageStrategy initialize(GameLaunchConfig config, Connector connector) {
        if (config.authToken() != null) return initializeOnline(connector);
        else return initializeOffline();
    }

    private StorageStrategy initializeOnline(Connector connector) {
        try {
            GameServiceClient client = new GameServiceClient();
            List<ItemMasterDTO> masterItems = client.getMasterItems();
            List<ShopItemDTO> shopInventory = client.getShopInventory("DEFAULT_SHOP");

            GameDataCache.getInstance().cacheItemData(masterItems);
            GameDataCache.getInstance().cacheShopInventory("DEFAULT_SHOP", shopInventory);

            Logger.getInstance().notify("Online data loaded successfully.", Message.INFORMATION);
            return new OnlineStorageStrategy(connector);
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to fetch online game data. Switching to offline mode.", Message.WARNING);
            return initializeOffline();
        }
    }

    private StorageStrategy initializeOffline() {
        Logger.getInstance().notify("Initializing in offline mode.", Message.INFORMATION);
        return new OfflineStorageStrategy();
    }

}