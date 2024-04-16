package platformer.core;

/**
 * Represents the prompt that the launcher will propagate to the game core.
 */
public class LauncherPrompt {

    private final String name;
    private final String password;
    private final boolean enableCheats;

    public LauncherPrompt(String name, String password, boolean enableCheats) {
        this.name = name;
        this.password = password;
        this.enableCheats = enableCheats;
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
}
