package platformer.launcher.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import platformer.AppCore;
import platformer.core.mode.GameMode;
import platformer.service.rest.client.GameServiceClient;
import platformer.core.TokenStorage;
import platformer.core.config.GameLaunchConfig;
import platformer.launcher.view.LoadingView;
import platformer.core.loading.LoadingProgressTracker;

import javax.swing.*;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;

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
        LoadingProgressTracker.getInstance().update(0.0, "Authenticating...");

        Thread gameThread = new Thread(() -> {
            try {
                GameServiceClient client = new GameServiceClient();
                boolean loggedIn = client.loginAndStoreToken(playerName, password);
                String authToken = loggedIn ? TokenStorage.getInstance().getToken() : null;

                final GameMode[] selectedMode = {GameMode.SINGLE_PLAYER};
                final Optional<String>[] sessionId = new Optional[]{Optional.empty()};

                if (loggedIn) {
                    CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        try {
                            Object[] options = {"Single Player", "Host Multiplayer", "Join Multiplayer"};
                            int choice = JOptionPane.showOptionDialog(null, "Select Game Mode", "Merciless Warrior",
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

                            switch (choice) {
                                case 1:
                                    selectedMode[0] = GameMode.MULTIPLAYER_HOST;
                                    break;
                                case 2:
                                    selectedMode[0] = GameMode.MULTIPLAYER_CLIENT;
                                    String id = JOptionPane.showInputDialog("Enter Session ID to Join:");
                                    sessionId[0] = Optional.ofNullable(id);
                                    break;
                                default:
                                    selectedMode[0] = GameMode.SINGLE_PLAYER;
                                    break;
                            }
                        } finally {
                            latch.countDown();
                        }
                    });
                    latch.await();
                }

                float scale = Float.parseFloat(System.getProperty("game.scale", "1.5"));
                GameLaunchConfig config = new GameLaunchConfig(
                        playerName,
                        authToken,
                        selectedMode[0],
                        sessionId[0],
                        enableCheats,
                        fullScreen,
                        scale
                );

                LoadingProgressTracker.getInstance().update(0.1, "Initializing game engine");
                AppCore.startGame(config);

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
