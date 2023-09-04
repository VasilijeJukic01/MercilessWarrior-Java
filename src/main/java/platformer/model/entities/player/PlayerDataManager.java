package platformer.model.entities.player;

import platformer.core.Account;
import platformer.core.Framework;
import platformer.ui.overlays.hud.UserInterface;

import static platformer.constants.Constants.*;

public class PlayerDataManager {

    private final Account account;
    private final Player player;
    private final UserInterface userInterface;

    private int coins = 0;
    private int exp = 0;
    private int level = 1;
    private int upgradeTokens = 0;

    public PlayerDataManager(Player player) {
        this.account = Framework.getInstance().getAccount();
        this.player = player;
        this.userInterface = new UserInterface(player);
        loadPlayerData();
    }

    public void loadPlayerData() {
        this.coins = account.getCoins();
        this.upgradeTokens = account.getTokens();
        this.level = account.getLevel();
        this.exp = account.getExp();
    }

    public void savePlayerData() {
        account.setCoins(coins);
        account.setTokens(upgradeTokens);
        account.setLevel(level);
        account.setExp(exp);
    }

    public void update() {
        double currentHealth = player.getCurrentHealth();
        double currentStamina = player.getCurrentStamina();
        double maxHealth = PLAYER_MAX_HP + PlayerBonus.getInstance().getBonusHealth();
        double maxStamina = PLAYER_MAX_ST + PlayerBonus.getInstance().getBonusPower();
        userInterface.update(currentHealth, maxHealth, currentStamina, maxStamina, exp, 1000*level);
    }

    public void changeExp(double value) {
        exp += value+PlayerBonus.getInstance().getBonusExp();
        exp = Math.max(Math.min(exp, XP_CAP), 0);
        if (exp > 1000*level) {
            exp = exp % (1000*level);
            level++;
            if (level % 2 == 0) changeUpgradeTokens(1);
        }
    }

    public void changeCoins(int value) {
        coins += value;
        coins = Math.max(coins, 0);
    }

    public void changeUpgradeTokens(int value) {
        upgradeTokens += value;
        upgradeTokens = Math.max(upgradeTokens, 0);
    }

    public int getCoins() {
        return coins;
    }

    public int getUpgradeTokens() {
        return upgradeTokens;
    }

    public int getLevel() {
        return level;
    }

    public UserInterface getUserInterface() {
        return userInterface;
    }
}
