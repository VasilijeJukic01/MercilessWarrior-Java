package platformer.launcher.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import platformer.AppCore;
import platformer.bridge.client.GameServiceClient;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.launcher.view.LoadingView;
import platformer.model.inventory.GameDataCache;
import platformer.utils.loading.LoadingProgressTracker;

/**
 * Controller for handling loading processes and game initialization logic.
 * This class separates the business logic from the view according to MVC pattern.
 */
public class LoadingController {

    private final LoadingView view;
    private final String playerName;
    private final String password;
    private final boolean enableCheats;
    private final boolean fullScreen;

    public LoadingController(LoadingView view, String playerName, String password, boolean enableCheats, boolean fullScreen) {
        this.view = view;
        this.playerName = playerName;
        this.password = password;
        this.enableCheats = enableCheats;
        this.fullScreen = fullScreen;
    }

    /**
     * Starts the game loading process in a background thread
     */
    public void startLoadingProcess() {
        LoadingProgressTracker.getInstance().update(0.0, "Starting launch process");

        Thread gameThread = new Thread(() -> {
            try {
                GameServiceClient client = new GameServiceClient();
                boolean loggedIn = client.loginAndStoreToken(playerName, password);

                if (loggedIn) {
                    LoadingProgressTracker.getInstance().update(0.05, "Fetching game data from server...");
                    try {
                        GameDataCache.getInstance().cacheItemData(client.getMasterItems());
                        GameDataCache.getInstance().cacheShopInventory("DEFAULT_SHOP", client.getShopInventory("DEFAULT_SHOP"));
                        Logger.getInstance().notify("Successfully loaded master data from server.", Message.INFORMATION);
                    } catch (Exception e) {
                        Logger.getInstance().notify("Could not fetch server data.", Message.WARNING);
                    }
                }
            } catch (Exception e) {
                Logger.getInstance().notify("An unexpected error occurred during online data fetch. Forcing offline mode.", Message.ERROR);
            }

            try {
                LoadingProgressTracker.getInstance().update(0.1, "Initializing game engine");
                String[] gameArgs = new String[]{
                        playerName,
                        password,
                        enableCheats ? "Yes" : "No",
                        fullScreen ? "Yes" : "No"
                };

                AppCore.main(gameArgs);
                monitorLaunchCompletion();
            } catch (Exception e) {
                Platform.runLater(() -> {
                    LoadingProgressTracker.getInstance().update(0.0, "Error starting game: " + e.getMessage());
                });
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    /**
     * Monitors the game loading progress and closes the loading screen when complete
     */
    private void monitorLaunchCompletion() {
        Task<Void> monitorTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // We will wait for the progress to reach 100% or timeout after 30 seconds
                long startTime = System.currentTimeMillis();
                long timeout = 30000;

                while (LoadingProgressTracker.getInstance().progressProperty().get() < 1.0) {
                    if (System.currentTimeMillis() - startTime > timeout) break;
                    Thread.sleep(100);
                }

                Thread.sleep(500);
                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(view::closeView);
            }
        };

        new Thread(monitorTask).start();
    }

}
