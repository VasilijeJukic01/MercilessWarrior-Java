package platformer.bridge.storage;

import platformer.bridge.Connector;
import platformer.bridge.requests.ShopTransactionRequest;
import platformer.bridge.requests.ShopTransactionResponse;
import platformer.core.Account;
import platformer.core.Framework;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.BoardItem;
import platformer.model.entities.player.Player;
import platformer.model.inventory.GameDataCache;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;
import platformer.model.inventory.ShopItem;
import platformer.state.GameState;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OnlineStorageStrategy implements StorageStrategy {

    private final Connector connector;

    public OnlineStorageStrategy(Connector connector) {
        this.connector = connector;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Account fetchAccountData(String username, int slot) {
        return connector.getData();
    }

    @Override
    public void updateAccountData(Account account, int slot) {
        connector.updateAccountData(account);
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        return connector.loadLeaderboardData();
    }

    @Override
    public Map<String, ItemData> getMasterItems() {
        return GameDataCache.getInstance().getItemData();
    }

    @Override
    public List<ShopItem> getShopInventory(String shopId) {
        return GameDataCache.getInstance().getShopInventory(shopId);
    }

    @Override
    public boolean buyItem(Player player, ShopItem selectedItem, int quantity) {
        ShopTransactionRequest request = new ShopTransactionRequest(
                Framework.getInstance().getAccount().getAccountID(),
                Framework.getInstance().getAccount().getName(),
                selectedItem.getItemId(),
                quantity
        );
        try {
            ShopTransactionResponse response = connector.buyItem(request);
            if (response != null) {
                Framework.getInstance().refreshAccountData();
                List<ShopItem> shopItems = response.getUpdatedShopInventory().stream()
                        .map(dto -> new ShopItem(dto.getItemId(), dto.getStock(), dto.getCost()))
                        .collect(Collectors.toList());
                GameDataCache.getInstance().cacheShopInventoryFromList("DEFAULT_SHOP", shopItems);
                if (Framework.getInstance().getGame().getCurrentState() instanceof GameState gameState) {
                    gameState.getObjectManager().refreshShopData();
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to send buy request!", Message.ERROR);
            return false;
        }
    }

    @Override
    public boolean sellItem(Player player, InventoryItem selectedItem, int quantity) {
        ShopTransactionRequest request = new ShopTransactionRequest(
                Framework.getInstance().getAccount().getAccountID(),
                Framework.getInstance().getAccount().getName(),
                selectedItem.getItemId(),
                quantity
        );
        try {
            ShopTransactionResponse response = connector.sellItem(request);
            if (response != null) {
                Framework.getInstance().refreshAccountData();
                List<ShopItem> shopItems = response.getUpdatedShopInventory().stream()
                        .map(dto -> new ShopItem(dto.getItemId(), dto.getStock(), dto.getCost()))
                        .collect(Collectors.toList());
                GameDataCache.getInstance().cacheShopInventoryFromList("DEFAULT_SHOP", shopItems);
                if (Framework.getInstance().getGame().getCurrentState() instanceof GameState gameState) {
                    gameState.getObjectManager().refreshShopData();
                }
                return true;
            }
            return false;
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to send sell request!", Message.ERROR);
            return false;
        }
    }
}