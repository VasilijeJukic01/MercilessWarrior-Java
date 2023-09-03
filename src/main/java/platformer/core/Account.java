package platformer.core;

import java.util.ArrayList;
import java.util.List;

public class Account {

    private final transient String name;
    private final transient int accountID, settingsID;
    private int spawn;
    private int coins, tokens;
    private int level, exp;
    private List<String> perks = new ArrayList<>();

    private transient boolean enableCheats;

    public Account() {
        this.name = "Default";
        this.accountID = this.settingsID = -1;
        this.spawn = 1;
        this.coins = this.tokens = this.exp = 0;
        this.level = 1;
    }

    public Account(String name, int accountID, int settingsID, int spawn, int coins, int tokens, int level, int exp) {
        this.name = name;
        this.accountID = accountID;
        this.settingsID = settingsID;
        this.spawn = spawn;
        this.coins = coins;
        this.tokens = tokens;
        this.level = level;
        this.exp = exp;
    }

    public Account(Account account) {
        this.name = account.name;
        this.accountID = account.accountID;
        this.settingsID = account.settingsID;
        this.spawn = account.spawn;
        this.coins = account.coins;
        this.tokens = account.tokens;
        this.level = account.level;
        this.exp = account.exp;
        this.perks = account.getPerks();
        this.enableCheats = account.enableCheats;
    }

    // Unload save file
    public void unload() {
        this.spawn = 1;
        this.level = 1;
        this.coins = this.tokens = this.exp = 0;
        this.perks = new ArrayList<>();
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

    public int getAccountID() {
        return accountID;
    }

    public int getSettingsID() {
        return settingsID;
    }

    public void setSpawn(int spawn) {
        this.spawn = spawn;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setTokens(int tokens) {
        this.tokens = tokens;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public void setPerks(List<String> perks) {
        this.perks = perks;
    }

    public boolean isEnableCheats() {
        return enableCheats;
    }

    public void setEnableCheats(boolean enableCheats) {
        this.enableCheats = enableCheats;
    }
}
