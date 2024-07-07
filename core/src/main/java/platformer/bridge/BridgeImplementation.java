package platformer.bridge;

import platformer.bridge.client.AuthServiceClient;
import platformer.bridge.client.GameServiceClient;
import platformer.core.Account;
import platformer.model.BoardItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 The SQLStorage class is a bridge between the game and the SQL database.
 It is responsible for executing SQL queries and operations on the database.
 */
// TODO: Refactor
public class BridgeImplementation implements Bridge {

    private AuthServiceClient authServiceClient;
    private GameServiceClient gameServiceClient;

    public BridgeImplementation() {
        init();
    }

    private void init() {
        this.authServiceClient = new AuthServiceClient();
        this.gameServiceClient = new GameServiceClient();
    }

    // Operations
    @Override
    public int createAccount(String username, String password) {
        try {
            return authServiceClient.createAccount(username, password);
        } catch (IOException e) {
            return 1;
        }
    }

    @Override
    public Account loadAccountData(String user, String password) {
        Mapper accountMapper = new Mapper();
        try {
            return accountMapper.mapToAccount(authServiceClient.loadAccountData(user, password));
        } catch (IOException e) {
            return new Account();
        }
    }

    @Override
    public List<BoardItem> loadLeaderboardData() {
        Mapper boardItemMapper = new Mapper();
        try {
            return boardItemMapper.mapToBoardItem(gameServiceClient.loadLeaderboardData());
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateAccountData(Account account) {
        Mapper accountMapper = new Mapper();
        try {
            // Called update
            gameServiceClient.updateAccountData(accountMapper.mapToAccountDataDTO(account));
        } catch (IOException e) {
            return;
        }
    }
}
