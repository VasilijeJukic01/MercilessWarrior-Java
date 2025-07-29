package platformer.core;

import platformer.bridge.storage.OfflineStorageStrategy;
import platformer.bridge.storage.OnlineStorageStrategy;
import platformer.bridge.storage.StorageStrategy;
import platformer.controller.GameSaveController;
import platformer.controller.KeyboardController;
import platformer.bridge.Connector;
import platformer.model.BoardItem;
import platformer.model.inventory.GameDataCache;
import platformer.serialization.GameSerializer;
import platformer.serialization.Serializer;
import platformer.state.GameState;

import java.util.List;

/**
 * Singleton that serves as the main entry point for the game.
 * It initializes and manages the game's core components such as the Game, Account, and GameSaveController.
 * <p>
 * The class interacts with the Database, Serializer and Leaderboard.
 */
public class Framework {

    private Game game;
    private LauncherPrompt launcherPrompt;
    private KeyboardController keyboardController;
    private Connector connector;
    private Serializer<Account, List<Account>> serializer;
    private Account cloud, account;
    private StorageStrategy storageStrategy;
    private GameSaveController saveController;
    private List<BoardItem> leaderboard;

    private static volatile Framework instance = null;

    private Framework() {}

    public static Framework getInstance() {
        if (instance == null) {
            synchronized (Framework.class) {
                if (instance == null) {
                    instance = new Framework();
                }
            }
        }
        return instance;
    }

    // Init
    public void init(String cheats, String name, String password, String fullScreen) {
        this.launcherPrompt = new LauncherPrompt(name, password, cheats.equals("Yes"), fullScreen.equals("Yes"));
        this.keyboardController = new KeyboardController();
        this.connector = new Connector(launcherPrompt);
        this.serializer = new GameSerializer();

        if (TokenStorage.getInstance().getToken() != null) {
            this.storageStrategy = new OnlineStorageStrategy(new Connector(launcherPrompt));
        }
        else this.storageStrategy = new OfflineStorageStrategy();

        GameDataCache.getInstance().cacheItemDataFromMap(storageStrategy.getMasterItems());
        GameDataCache.getInstance().cacheShopInventoryFromList("DEFAULT_SHOP", storageStrategy.getShopInventory("DEFAULT_SHOP"));

        initAccount();
        initLeaderboard();
        initGame();
        this.saveController = new GameSaveController(game);
    }

    private void initAccount() {
        // this.cloud = connector.getData();
        this.cloud = storageStrategy.fetchAccountData(launcherPrompt.getName(), 0);
        this.cloud.setEnableCheats(launcherPrompt.isEnableCheats());
        this.account = new Account(cloud);
    }

    private void initLeaderboard() {
        this.leaderboard = connector.loadLeaderboardData();
    }

    private void initGame() {
        this.game = new Game();
        if (launcherPrompt.isFullScreen()) this.game.toggleFullScreen();
    }

    // Save
    public void cloudSave() {
        connector.updateAccountData(account);
        initAccount();
    }

    public void refreshAccountData() {
        if (!storageStrategy.isOnline()) return;
        Account refreshedAccount = storageStrategy.fetchAccountData(account.getName(), 0);
        this.account.copyFromSlot(refreshedAccount);
        if (game.getCurrentState() instanceof GameState) {
            ((GameState) game.getCurrentState()).refreshPlayerDataFromAccount();
        }
    }

    public void localSave(int slot) {
        serializer.serialize(account, slot);
    }

    public void localDelete(int slot) {
        serializer.delete(slot);
    }

    public List<Account> getAllSaves() {
        return serializer.deserialize();
    }

    // Start
    public void start() {
        game.start();
    }

    // Getters
    public Game getGame() {
        return game;
    }

    public KeyboardController getKeyboardController() {
        return keyboardController;
    }

    public Account getCloud() {
        return cloud;
    }

    public Account getAccount() {
        return account;
    }

    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }

    public GameSaveController getSaveController() {
        return saveController;
    }

    public List<BoardItem> getLeaderboard() {
        return leaderboard;
    }

}
