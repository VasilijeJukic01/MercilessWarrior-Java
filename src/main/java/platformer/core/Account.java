package platformer.core;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Account {

    private final transient String name;
    private final transient String password;
    private final transient int accountID, settingsID;
    private int spawn;
    private int coins, tokens;
    private int level, exp;
    private List<String> perks = new ArrayList<>();
    private List<String> items = new ArrayList<>();

    private String lastTimeSaved;
    private long playtime;
    private transient Timer timer;

    private transient boolean enableCheats;

    /**
     * No account constructor
     */
    public Account() {
        this.name = "Default";
        this.password = "";
        this.accountID = this.settingsID = -1;
        this.spawn = -1;
        this.coins = this.tokens = this.exp = 0;
        this.level = 1;
        this.playtime = 0;
    }

    /**
     * Standard constructor
     */
    public Account(String name, String password, int accountID, int settingsID, int spawn, int coins, int tokens, int level, int exp) {
        this.name = name;
        this.password = password;
        this.accountID = accountID;
        this.settingsID = settingsID;
        this.spawn = spawn;
        this.coins = coins;
        this.tokens = tokens;
        this.level = level;
        this.exp = exp;
    }

    /**
     * Prototype constructor
     * <p>
     * @param account Account to copy
     */
    public Account(Account account) {
        this.name = account.name;
        this.password = account.password;
        this.accountID = account.accountID;
        this.settingsID = account.settingsID;
        this.spawn = account.spawn;
        this.coins = account.coins;
        this.tokens = account.tokens;
        this.level = account.level;
        this.exp = account.exp;
        this.perks = account.getPerks();
        this.items = account.getItems();
        this.enableCheats = account.enableCheats;
        this.playtime = account.playtime;
    }

    public void copyFromSlot(Account slotData) {
        if (slotData == null) return;
        setPerks(slotData.getPerks());
        setItems(slotData.getItems());
        setLevel(slotData.getLevel());
        setExp(slotData.getExp());
        setCoins(slotData.getCoins());
        setSpawn(slotData.getSpawn());
        setTokens(slotData.getTokens());
        setPlaytime(slotData.getPlaytime());
    }

    // Timer
    public void startGameTimer() {
        if (timer == null) {
            timer = new Timer(1000, e -> playtime += 1);
            timer.start();
        }
    }

    public void stopGameTimer() {
        if (timer != null) timer.stop();
    }

    // Unload save file
    public void unload() {
        this.spawn = -1;
        this.level = 1;
        this.coins = this.tokens = this.exp = 0;
        this.perks = new ArrayList<>();
        this.items = new ArrayList<>();
        stopGameTimer();
        this.playtime = 0;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
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

    public List<String> getItems() {
        return items;
    }

    public int getAccountID() {
        return accountID;
    }

    public int getSettingsID() {
        return settingsID;
    }

    public String getLastTimeSaved() {
        return lastTimeSaved;
    }

    public long getPlaytime() {
        return playtime;
    }

    public void setLastTimeSaved(String lastTimeSaved) {
        this.lastTimeSaved = lastTimeSaved;
    }

    public void setPlaytime(long playtime) {
        this.playtime = playtime;
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

    public void setItems(List<String> items) {
        this.items = items;
    }

    public boolean isEnableCheats() {
        return enableCheats;
    }

    public void setEnableCheats(boolean enableCheats) {
        this.enableCheats = enableCheats;
    }
}
