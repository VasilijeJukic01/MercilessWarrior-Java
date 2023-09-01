package platformer.database.bridge;

import platformer.core.Account;
import platformer.core.LauncherPrompt;
import platformer.database.BoardDatum;
import platformer.database.CredentialsLoader;
import platformer.database.DBSettings;
import platformer.database.Settings;

import java.util.List;

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

    public Account getData() {
        return storage.loadData(launcherPrompt.getName());
    }

    public List<BoardDatum> loadLeaderboardData() {
        return storage.loadLeaderboardData();
    }

    public void updateData(Account account) {
        storage.updateData(account);
    }

}
