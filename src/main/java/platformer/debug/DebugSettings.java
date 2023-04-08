package platformer.debug;


public class DebugSettings {

    public static DebugSettings instance = null;

    private boolean debugMode;

    private DebugSettings() {}

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public static DebugSettings getInstance() {
        if (instance == null) {
            instance = new DebugSettings();
        }
        return instance;
    }

}
