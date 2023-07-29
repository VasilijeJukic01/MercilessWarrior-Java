package platformer.debug;


import platformer.utils.Utils;

public class DebugSettings {

    public static volatile DebugSettings instance = null;

    private boolean debugMode;

    private DebugSettings() {}

    public static DebugSettings getInstance() {
        if (instance == null) {
            synchronized (DebugSettings.class) {
                if (instance == null) {
                    instance = new DebugSettings();
                }
            }
        }
        return instance;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

}
