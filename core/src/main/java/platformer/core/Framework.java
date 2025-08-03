package platformer.core;

import platformer.animation.SpriteManager;
import platformer.storage.StorageStrategy;
import platformer.controller.GameSaveController;
import platformer.controller.KeyboardController;
import platformer.core.config.GameLaunchConfig;
import platformer.core.initializer.GameDataInitializer;
import platformer.model.BoardItem;
import platformer.serialization.GameSerializer;
import platformer.serialization.Serializer;
import platformer.state.types.GameState;

import java.util.List;

/**
 * Singleton that serves as the main entry point for the game.
 * It initializes and manages the game's core components such as the Game, Account, and GameSaveController.
 * <p>
 * The class interacts with the Database, Serializer and Leaderboard.
 */
public class Framework {

    private Game game;
    private GameLaunchConfig launchConfig;
    private KeyboardController keyboardController;
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
    public void init(GameLaunchConfig config) {
        SpriteManager.getInstance().loadAllAssets();
        this.launchConfig = config;
        this.keyboardController = new KeyboardController();
        this.serializer = new GameSerializer();
        GameDataInitializer dataInitializer = new GameDataInitializer();
        this.storageStrategy = dataInitializer.initialize(config);
        initAccount();
        initLeaderboard();
        initGame();
        this.saveController = new GameSaveController(game);
    }

    private void initAccount() {
        this.cloud = storageStrategy.fetchAccountData(launchConfig.username(), 0);
        this.cloud.setEnableCheats(launchConfig.cheatsEnabled());
        this.account = new Account(cloud);
    }

    private void initLeaderboard() {
        this.leaderboard = storageStrategy.loadLeaderboardData();
    }

    private void initGame() {
        this.game = new Game();
        if (launchConfig.fullScreen()) this.game.toggleFullScreen();
    }

    // Save
    public void cloudSave() {
        storageStrategy.updateAccountData(account, 0);
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
