package platformer.core;

import platformer.controller.GameSaveController;
import platformer.controller.KeyboardController;
import platformer.database.bridge.Database;
import platformer.model.BoardItem;
import platformer.serialization.GameSerializer;
import platformer.serialization.Serializer;

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
    private Database database;
    private Serializer<Account, List<Account>> serializer;
    private Account cloud, account;
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
    public void init(String cheats, String name, String password) {
        this.launcherPrompt = new LauncherPrompt(name, password, cheats.equals("Yes"));
        this.keyboardController = new KeyboardController();
        this.database = new Database(launcherPrompt);
        this.serializer = new GameSerializer();
        initAccount();
        initLeaderboard();
        this.game = new Game();
        this.saveController = new GameSaveController(game);
    }

    private void initAccount() {
        this.cloud = database.getData();
        this.cloud.setEnableCheats(launcherPrompt.isEnableCheats());
        this.account = new Account(cloud);
    }

    private void initLeaderboard() {
        this.leaderboard = database.loadLeaderboardData();
    }

    // Save
    public void cloudSave() {
        database.updateAccountData(account);
        initAccount();
    }

    public void localSave(int slot) {
        serializer.serialize(account, slot);
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

    public GameSaveController getSaveController() {
        return saveController;
    }

    public List<BoardItem> getLeaderboard() {
        return leaderboard;
    }

}
