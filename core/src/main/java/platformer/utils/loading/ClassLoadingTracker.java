package platformer.utils.loading;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A utility class that tracks class loading progress during application startup.
 * This provides more accurate loading progress information based on actual class loading.
 */
public class ClassLoadingTracker {

    private static final String[] CORE_PACKAGES = {
        "platformer.animation",
        "platformer.audio",
        "platformer.bridge",
        "platformer.constants",
        "platformer.controller",
        "platformer.core",
        "platformer.debug",
        "platformer.launcher",
        "platformer.model",
        "platformer.observer",
        "platformer.serialization",
        "platformer.state",
        "platformer.ui",
        "platformer.utils",
        "platformer.view"
    };

    private static final Set<String> loadedClasses = new HashSet<>();
    private static final AtomicInteger loadedClassCount = new AtomicInteger(0);
    private static int estimatedTotalClasses = 100;

    /**
     * Registers a loaded class and updates the loading progress.
     *
     * @param className The name of the loaded class
     */
    public static synchronized void registerLoadedClass(String className) {
        if (!isTrackedPackage(className)) return;

        if (loadedClasses.add(className)) {
            int current = loadedClassCount.incrementAndGet();
            updateLoadingProgress(current);
        }
    }

    /**
     * Updates the loading progress based on the number of classes loaded.
     *
     * @param loadedCount The current count of loaded classes
     */
    private static void updateLoadingProgress(int loadedCount) {
        double progress = Math.min(0.95, (double) loadedCount / estimatedTotalClasses);
        LoadingProgressTracker.getInstance().update(progress, "Loading game components... (" + loadedCount + " classes loaded)");
    }

    /**
     * Checks if a class belongs to one of our tracked core packages.
     */
    private static boolean isTrackedPackage(String className) {
        for (String pkg : CORE_PACKAGES) {
            if (className.startsWith(pkg)) return true;
        }
        return false;
    }

    /**
     * Marks the loading as complete.
     */
    public static void markLoadingComplete() {
        LoadingProgressTracker.getInstance().update(1.0, "Game loaded successfully!");
    }
}
