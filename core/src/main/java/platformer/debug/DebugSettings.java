package platformer.debug;

public class DebugSettings {

    private static volatile DebugSettings instance = null;

    private boolean debugMode;
    private boolean roricDebugMode = true;
    private int roricFightStartOffsetMs = 90000;

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

    public boolean isRoricDebugMode() {
        return roricDebugMode;
    }

    public void setRoricDebugMode(boolean roricDebugMode) {
        this.roricDebugMode = roricDebugMode;
    }

    public int getRoricFightStartOffsetMs() {
        return roricFightStartOffsetMs;
    }

    public void setRoricFightStartOffsetMs(int roricFightStartOffsetMs) {
        this.roricFightStartOffsetMs = roricFightStartOffsetMs;
    }

}
