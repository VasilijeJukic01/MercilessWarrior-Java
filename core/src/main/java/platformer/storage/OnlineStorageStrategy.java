package platformer.storage;

import platformer.service.rest.mapper.AccountMapper;
import platformer.service.rest.mapper.LeaderboardMapper;
import platformer.service.rest.requests.ShopTransactionRequest;
import platformer.service.rest.requests.ShopTransactionResponse;
import platformer.service.OnlineService;
import platformer.core.Account;
import platformer.core.Framework;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.BoardItem;
import platformer.model.entities.player.Player;
import platformer.model.inventory.database.GameDataCache;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;
import platformer.state.types.GameState;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An implementation of {@link StorageStrategy} for online mode.
 * <p>
 * This class orchestrates online data operations. It relies on an abstract {@link OnlineService} to handle the actual network communication.
 * Its primary responsibilities are:
 * <ul>
 *   <li>Calling the online service to fetch or send data (as DTOs).</li>
 *   <li>Using mappers to convert between network DTOs and the game's internal models.</li>
 *   <li>Handling responses and updating caches or game state as needed.</li>
 * </ul>
 *
 * @see StorageStrategy
 * @see OnlineService
 * @see AccountMapper
 */
public class OnlineStorageStrategy implements StorageStrategy {

    private final OnlineService onlineService;
    private final AccountMapper accountMapper;
    private final LeaderboardMapper leaderboardMapper;

    public OnlineStorageStrategy(OnlineService onlineService, AccountMapper accountMapper, LeaderboardMapper leaderboardMapper) {
        this.onlineService = onlineService;
        this.accountMapper = accountMapper;
        this.leaderboardMapper = leaderboardMapper;
    }

    @Override
    public boolean isOnline() {
        return true;
    }

    @Override
    public Account fetchAccountData(String username, int slot) {
        try {
            return accountMapper.toEntity().apply(onlineService.fetchAccountData(username));
        } catch (IOException e) {
            Logger.getInstance().notify("Loading data from database failed. Switching to Default profile.", Message.ERROR);
            return new Account();
        }
    }

    @Override
    public void updateAccountData(Account account, int slot) {
        try {
            onlineService.updateAccountData(accountMapper.toDto().apply(account));
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to update account data!", Message.ERROR);
        }
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        try {
            return leaderboardMapper.toEntityList(onlineService.loadLeaderboardData());
        } catch (IOException e) {
            Logger.getInstance().notify("Leaderboard fetch failed!", Message.ERROR);
            return new ArrayList<>();
        }
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
            ShopTransactionResponse response = onlineService.buyItem(request);
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
            ShopTransactionResponse response = onlineService.sellItem(request);
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