package platformer.bridge;

import platformer.bridge.client.GameServiceClient;
import platformer.bridge.mapper.AccountMapper;
import platformer.bridge.mapper.LeaderboardMapper;
import platformer.bridge.mapper.Mapper;
import platformer.bridge.requests.*;
import platformer.core.Account;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.BoardItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 The SQLStorage class is a bridge between the game and the SQL database.
 It is responsible for executing SQL queries and operations on the database.
 */
public class BridgeImplementation implements Bridge {

    private GameServiceClient gameServiceClient;
    private Mapper<Account, AccountDataDTO> accountMapper;
    private Mapper<BoardItem, BoardItemDTO> leaderboardMapper;

    public BridgeImplementation() {
        init();
    }

    private void init() {
        this.gameServiceClient = new GameServiceClient();
        this.accountMapper = new AccountMapper();
        this.leaderboardMapper = new LeaderboardMapper();
    }

    // Operations
    @Override
    public int createAccount(String username, String password) {
        try {
            return gameServiceClient.createAccount(username, password);
        } catch (IOException e) {
            Logger.getInstance().notify("Account creation failed!", Message.ERROR);
            return 1;
        }
    }

    @Override
    public Account fetchAccountData(String user) {
        try {
            return accountMapper.toEntity().apply(gameServiceClient.fetchAccountData(user));
        } catch (IOException e) {
            Logger.getInstance().notify("Loading data from database failed. Switching to Default profile.", Message.ERROR);
            return new Account();
        }
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        try {
            return leaderboardMapper.toEntityList(gameServiceClient.loadLeaderboardData());
        } catch (IOException e) {
            Logger.getInstance().notify("Leaderboard fetch failed!", Message.ERROR);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateAccountData(Account account) {
        try {
            gameServiceClient.updateAccountData(accountMapper.toDto().apply(account));
        } catch (IOException e) {
            Logger.getInstance().notify("Failed to update account data!", Message.ERROR);
        }
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
}
