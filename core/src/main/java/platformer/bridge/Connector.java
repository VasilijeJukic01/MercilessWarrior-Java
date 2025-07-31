package platformer.bridge;

import platformer.bridge.requests.ShopTransactionRequest;
import platformer.bridge.requests.ShopTransactionResponse;
import platformer.core.Account;
import platformer.model.BoardItem;

import java.io.IOException;
import java.util.List;

public class Connector {

    private final String username;
    private final Bridge bridge;

    public Connector(String username) {
        this.username = username;
        this.bridge = new BridgeImplementation();
    }

    // Connector methods
    public int createAccount(String username, String password) {
        return bridge.createAccount(username, password);
    }

    public Account getData() {
        return bridge.fetchAccountData(username);
    }

    public List<BoardItem> loadLeaderboardData() {
        return bridge.loadLeaderboardData();
    }

    public void updateAccountData(Account account) {
        bridge.updateAccountData(account);
    }

    public ShopTransactionResponse buyItem(ShopTransactionRequest request) throws IOException {
        return bridge.buyItem(request);
    }

    public ShopTransactionResponse sellItem(ShopTransactionRequest request) throws IOException {
        return bridge.sellItem(request);
    }
}
