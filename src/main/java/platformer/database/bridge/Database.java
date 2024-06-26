package platformer.database.bridge;

import platformer.core.Account;
import platformer.core.LauncherPrompt;
import platformer.model.BoardItem;
import platformer.database.CredentialsLoader;
import platformer.database.DBSettings;
import platformer.database.Settings;

import java.util.List;

/**
 * Managing the database operations in the game.
 * It uses the Storage interface to perform these operations.
 * <p>
 * The class uses a LauncherPrompt object to get the launcher's input, a CredentialsLoader object to load the database credentials,
 * a Settings object to store these credentials, and a Storage object to perform the database operations.
 */
public class Database {

    private final LauncherPrompt launcherPrompt;
    private Settings settings;
    private final CredentialsLoader credentialsLoader;
    // Bridge
    private final Storage storage;

    public Database(LauncherPrompt launcherPrompt) {
        this.launcherPrompt = launcherPrompt;
        this.credentialsLoader = new CredentialsLoader();
        initSettings();
        this.storage = new SQLStorage(settings);
    }

    private void initSettings() {
        this.settings = new DBSettings();
        settings.addParameter("IP", credentialsLoader.getDatabaseIP());
        settings.addParameter("DATABASE", credentialsLoader.getDatabaseName());
        settings.addParameter("USERNAME", credentialsLoader.getDatabaseUsername());
        settings.addParameter("PASSWORD", credentialsLoader.getDatabasePassword());
    }

    public int createAccount(String username, String password) {
        return storage.createAccount(username, password);
    }

    public Account getData() {
        return storage.loadAccountData(launcherPrompt.getName(), launcherPrompt.getPassword());
    }

    public List<BoardItem> loadLeaderboardData() {
        return storage.loadLeaderboardData();
    }

    public void updateAccountData(Account account) {
        storage.updateAccountData(account);
    }

}
