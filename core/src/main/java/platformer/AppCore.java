package platformer;

import platformer.core.Framework;
import platformer.core.config.GameLaunchConfig;
import platformer.utils.loading.ClassLoadingTracker;
import platformer.utils.loading.LoadingProgressTracker;

/**
 * Main class of the game.
 */
public class AppCore {

    public static void main(String[] args) {
        LoadingProgressTracker.getInstance().update(0.0, "Game started without launcher configuration.");
    }

    public static void startGame(GameLaunchConfig config) {
        LoadingProgressTracker.getInstance().update(0.05, "Starting game loading");
        try {
            LoadingProgressTracker.getInstance().update(0.1, "Initializing game");
            Framework.getInstance().init(config);
            ClassLoadingTracker.markLoadingComplete();
            Framework.getInstance().start();
        } catch (Exception e) {
            LoadingProgressTracker.getInstance().update(0.0, "Error: " + e.getMessage());
        }
    }

}
