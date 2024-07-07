package platformer.bridge;

import platformer.core.Account;
import platformer.core.LauncherPrompt;
import platformer.model.BoardItem;

import java.util.List;

public class Connector {

    private final LauncherPrompt launcherPrompt;
    private final Bridge bridge;

    public Connector(LauncherPrompt launcherPrompt) {
        this.launcherPrompt = launcherPrompt;
        this.bridge = new BridgeImplementation();
    }

    public int createAccount(String username, String password) {
        return bridge.createAccount(username, password);
    }

    public Account getData() {
        return bridge.loadAccountData(launcherPrompt.getName(), launcherPrompt.getPassword());
    }

    public List<BoardItem> loadLeaderboardData() {
        return bridge.loadLeaderboardData();
    }

    public void updateAccountData(Account account) {
        bridge.updateAccountData(account);
    }
}
