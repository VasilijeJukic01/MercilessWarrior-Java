package platformer.core;

import lombok.Getter;
import lombok.Setter;

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
    private List<String> worldFlags = new ArrayList<>();
    private List<String> quests = new ArrayList<>();
    private String explorationData;

    private String lastTimeSaved;
    private volatile long playtime;

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
        this.perks = account.getPerks() != null ? new ArrayList<>(account.getPerks()) : new ArrayList<>();
        this.items = account.getItems() != null ? new ArrayList<>(account.getItems()) : new ArrayList<>();
        this.worldFlags = account.getWorldFlags() != null ? new ArrayList<>(account.getWorldFlags()) : new ArrayList<>();
        this.quests = account.getQuests() != null ? new ArrayList<>(account.getQuests()) : new ArrayList<>();
        this.explorationData = account.explorationData;
        this.enableCheats = account.enableCheats;
        this.playtime = account.playtime;
    }

    public void copyFromSlot(Account slotData) {
        if (slotData == null) return;
        setPerks(slotData.getPerks() != null ? new ArrayList<>(slotData.getPerks()) : new ArrayList<>());
        setItems(slotData.getItems() != null ? new ArrayList<>(slotData.getItems()) : new ArrayList<>());
        setLevel(slotData.getLevel());
        setExp(slotData.getExp());
        setCoins(slotData.getCoins());
        setSpawn(slotData.getSpawn());
        setTokens(slotData.getTokens());
        setPlaytime(slotData.getPlaytime());
        setExplorationData(slotData.getExplorationData());
        setWorldFlags(slotData.getWorldFlags() != null ? new ArrayList<>(slotData.getWorldFlags()) : new ArrayList<>());
        setQuests(slotData.getQuests() != null ? new ArrayList<>(slotData.getQuests()) : new ArrayList<>());
    }

    public void incrementPlaytime() {
        this.playtime += 1;
    }

    // Unload save file
    public void unload() {
        this.spawn = -1;
        this.level = 1;
        this.coins = this.tokens = this.exp = 0;
        this.perks = new ArrayList<>();
        this.items = new ArrayList<>();
        this.worldFlags = new ArrayList<>();
        this.quests = new ArrayList<>();
        this.explorationData = null;
        this.playtime = 0;
    }
}
