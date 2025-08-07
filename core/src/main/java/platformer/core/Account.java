package platformer.core;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Account {

    private final transient String name;
    private final transient long accountID, settingsID;
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
        this.accountID = this.settingsID = -1;
        this.spawn = -1;
        this.coins = this.tokens = this.exp = 0;
        this.level = 1;
        this.playtime = 0;
    }

    /**
     * Standard constructor
     */
    public Account(String name, long accountID, long settingsID, int spawn, int coins, int tokens, int level, int exp) {
        this.name = name;
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
}
