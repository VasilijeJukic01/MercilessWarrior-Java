package platformer;

import platformer.animation.AssetManager;
import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.core.Framework;
import platformer.core.config.GameLaunchConfig;
import platformer.core.loading.LoadingProgressTracker;

import java.util.concurrent.CompletableFuture;

/**
 * Core entry point for the platformer game application.
 * This class manages the game initialization and startup sequence, handling the loading process and framework initialization.
 */
public class AppCore {

    public static void main(String[] args) {
        LoadingProgressTracker.getInstance().update(0.0, "Game started without launcher configuration.");
    }

    public static void startGame(GameLaunchConfig config) {
        LoadingProgressTracker tracker = LoadingProgressTracker.getInstance();
        tracker.update(0.05, "Decrypting assets...");

        try {
            AssetManager.getInstance();
            tracker.update(0.10, "Loading Engine Assets...");

            CompletableFuture<Void> audioFuture = CompletableFuture.runAsync(() -> {
                Audio.getInstance();
            });
            CompletableFuture<Void> spritesFuture = CompletableFuture.runAsync(() -> {
                SpriteManager.getInstance().loadAllAssets();
            });
            CompletableFuture.allOf(audioFuture, spritesFuture).join();

            tracker.update(0.90, "Initializing Game World...");
            Framework.getInstance().init(config);

            tracker.markLoadingComplete();
            Framework.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
            tracker.update(0.0, "Error: " + e.getMessage());
        }
    }

}
