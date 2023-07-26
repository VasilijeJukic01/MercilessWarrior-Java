package platformer.database.bridge;

import platformer.core.Account;
import platformer.core.LauncherPrompt;
import platformer.database.Credentials;
import platformer.database.DBSettings;
import platformer.database.Settings;

public class Database {

    private final LauncherPrompt launcherPrompt;
    private Settings settings;
    private final Storage storage;

    public Database(LauncherPrompt launcherPrompt) {
        this.launcherPrompt = launcherPrompt;
        initSettings();
        this.storage = new SQLStorage(settings);
    }

    private void initSettings() {
        this.settings = new DBSettings();
        settings.addParameter("IP", Credentials.MYSQL_IP.getValue());
        settings.addParameter("DATABASE", Credentials.MYSQL_DATABASE.getValue());
        settings.addParameter("USERNAME", Credentials.MYSQL_USERNAME.getValue());
        settings.addParameter("PASSWORD", Credentials.MYSQL_PASSWORD.getValue());
    }

    public Account getData() {
        return storage.loadData(launcherPrompt.getName());
    }

}
