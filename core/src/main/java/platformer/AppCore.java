package platformer;

import platformer.core.Framework;
import platformer.core.config.GameLaunchConfig;
import platformer.core.loading.LoadingProgressTracker;

/**
 * Core entry point for the platformer game application.
 * This class manages the game initialization and startup sequence, handling the loading process and framework initialization.
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
            LoadingProgressTracker.getInstance().markLoadingComplete();
            Framework.getInstance().start();
        } catch (Exception e) {
            LoadingProgressTracker.getInstance().update(0.0, "Error: " + e.getMessage());
        }
    }

}
