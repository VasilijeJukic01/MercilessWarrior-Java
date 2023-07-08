package platformer.core;

public class Account {

    private final String name;
    private final boolean enableCheats;

    public Account(String name, boolean enableCheats) {
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
