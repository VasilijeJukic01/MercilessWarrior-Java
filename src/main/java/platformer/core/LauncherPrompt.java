package platformer.core;

public class LauncherPrompt {

    private final String name;
    private final boolean enableCheats;

    public LauncherPrompt(String name, boolean enableCheats) {
        this.name = name;
        this.enableCheats = enableCheats;
    }

    public String getName() {
        return name;
    }

    public boolean isEnableCheats() {
        return enableCheats;
    }
}
