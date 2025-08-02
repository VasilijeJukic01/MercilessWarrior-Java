package platformer.core.loading;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.nio.file.Paths;

/**
 * Manages all external file paths for user-generated data like saves and configs.
 * Creates a dedicated "MercilessWarrior" folder in the user's Documents/My Games directory.
 */
public class PathManager {

    private static final String CORE_FOLDER = "My Games";
    private static final String GAME_FOLDER = "MercilessWarrior";
    private static String gameDataPath;

    private static void initialize() {
        if (gameDataPath == null) {
            try {
                File documentsDir = FileSystemView.getFileSystemView().getDefaultDirectory();
                File myGamesDir = new File(documentsDir, CORE_FOLDER);
                File gameDir = new File(myGamesDir, GAME_FOLDER);
                if (!gameDir.exists()) {
                    if (!gameDir.mkdirs()) throw new Exception("Could not create game data directories.");
                }
                gameDataPath = gameDir.getAbsolutePath();

            } catch (Exception e) {
                Logger.getInstance().notify("Could not create directory in Documents.", Message.WARNING);
                String userHome = System.getProperty("user.home");
                gameDataPath = Paths.get(userHome, GAME_FOLDER).toString();
                new File(gameDataPath).mkdirs();
            }
        }
    }

    public static String getGameDataPath() {
        initialize();
        return gameDataPath;
    }

    public static String getSavesPath() {
        File savesDir = new File(getGameDataPath(), "saves");
        savesDir.mkdirs();
        return savesDir.getAbsolutePath();
    }

    public static String getConfigPath() {
        return Paths.get(getGameDataPath(), "keyboard.config").toString();
    }
}
