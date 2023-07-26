package platformer.core;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private final String name;
    private final int spawn;
    private final int coins, tokens;
    private final int level, exp;
    private final List<String> perks = new ArrayList<>();

    private boolean enableCheats;

    public Account() {
        this.name = "Default";
        this.spawn = 1;
        this.coins = this.tokens = this.exp = 0;
        this.level = 1;
    }

    public Account(String name, int spawn, int coins, int tokens, int level, int exp) {
        this.name = name;
        this.spawn = spawn;
        this.coins = coins;
        this.tokens = tokens;
        this.level = level;
        this.exp = exp;
    }

    public String getName() {
        return name;
    }

    public int getSpawn() {
        return spawn;
    }

    public int getCoins() {
        return coins;
    }

    public int getTokens() {
        return tokens;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public List<String> getPerks() {
        return perks;
    }

    public boolean isEnableCheats() {
        return enableCheats;
    }

    public void setEnableCheats(boolean enableCheats) {
        this.enableCheats = enableCheats;
    }
}
