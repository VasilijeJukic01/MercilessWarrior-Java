package platformer.core;

/**
 * Represents the prompt that the launcher will propagate to the game core.
 */
public class LauncherPrompt {

    private final String name;
    private final String password;
    private final boolean enableCheats;
    private final boolean fullScreen;

    public LauncherPrompt(String name, String password, boolean enableCheats, boolean fullScreen) {
        this.name = name;
        this.password = password;
        this.enableCheats = enableCheats;
        this.fullScreen = fullScreen;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean isEnableCheats() {
        return enableCheats;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }
}
