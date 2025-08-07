package platformer.core.loading;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A utility class to track loading progress across the application.
 * This allows the core application to report its loading progress back to the loading screen.
 */
public class LoadingProgressTracker {

    private static LoadingProgressTracker instance;

    private final DoubleProperty progress = new SimpleDoubleProperty(0);
    private final StringProperty status = new SimpleStringProperty("Starting");

    private LoadingProgressTracker() { }

    /**
     * Get the singleton instance of the LoadingProgressTracker.
     *
     * @return The instance of LoadingProgressTracker
     */
    public static synchronized LoadingProgressTracker getInstance() {
        if (instance == null) instance = new LoadingProgressTracker();
        return instance;
    }

    /**
     * Update the loading progress.
     *
     * @param progressValue A value between 0.0 and 1.0 representing the loading progress
     */
    public void updateProgress(double progressValue) {
        if (Platform.isFxApplicationThread()) progress.set(progressValue);
        else Platform.runLater(() -> progress.set(progressValue));
    }

    /**
     * Update the loading status message.
     *
     * @param statusMessage The current loading status message
     */
    public void updateStatus(String statusMessage) {
        if (Platform.isFxApplicationThread()) status.set(statusMessage);
        else Platform.runLater(() -> status.set(statusMessage));
    }

    /**
     * Update both the progress and status at once.
     *
     * @param progressValue A value between 0.0 and 1.0 representing the loading progress
     * @param statusMessage The current loading status message
     */
    public void update(double progressValue, String statusMessage) {
        if (Platform.isFxApplicationThread()) {
            progress.set(progressValue);
            status.set(statusMessage);
        }
        else {
            Platform.runLater(() -> {
                progress.set(progressValue);
                status.set(statusMessage);
            });
        }
    }

    public void markLoadingComplete() {
        update(1.0, "Game loaded successfully!");
    }

    // Getters
    public DoubleProperty progressProperty() {
        return progress;
    }

    public StringProperty statusProperty() {
        return status;
    }
}
