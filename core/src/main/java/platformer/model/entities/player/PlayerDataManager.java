package platformer.model.entities.player;

import platformer.core.Account;
import platformer.core.Framework;
import platformer.core.config.BalanceManager;
import platformer.model.effects.TimeCycleManager;
import platformer.model.levels.Spawn;
import platformer.model.minimap.MinimapManager;
import platformer.model.perks.PerksBonus;
import platformer.ui.overlays.hud.UserInterface;

import java.awt.*;

import static platformer.constants.Constants.*;

/**
 * Manages the player's data (health, stamina, experience, coins, and tokens).
 * Handles loading player data from the account and updating the user interface.
 */
public class PlayerDataManager {

    private final Player player;
    private final UserInterface userInterface;

    private int coins = 0;
    private int exp = 0;
    private int level = 1;
    private int upgradeTokens = 0;

    public PlayerDataManager(Player player, MinimapManager minimapManager, TimeCycleManager timeCycleManager) {
        this.player = player;
        this.userInterface = new UserInterface(player, minimapManager, timeCycleManager);
        loadPlayerData();
    }

    public void loadPlayerData() {
        Account account = Framework.getInstance().getAccount();
        this.coins = account.getCoins();
        this.upgradeTokens = account.getTokens();
        this.level = account.getLevel();
        this.exp = account.getExp();
        loadSpawnPoint(account);
    }

    /**
     * Refreshes the player's live data from the provided Account object.
     * This is a "soft refresh" used after online transactions to avoid a full level reload.
     *
     * @param account The updated account data from the server.
     */
    public void refreshFromAccount(Account account) {
        this.coins = account.getCoins();
        this.upgradeTokens = account.getTokens();
        this.level = account.getLevel();
        this.exp = account.getExp();
        player.getInventory().fillItems(account.getItems());
    }

    // Core
    public void update(boolean isMinimapVisible) {
        double currentHealth = player.getCurrentHealth();
        double maxHealth = player.getMaxHealth();
        double currentStamina = player.getCurrentStamina();
        double maxStamina = player.getMaxStamina();
        int xpMult = BalanceManager.getInstance().getPlayerBalance().getXpPerLevelMultiplier();

        userInterface.setMinimapVisible(isMinimapVisible);
        userInterface.update(currentHealth, maxHealth, currentStamina, maxStamina, exp, xpMult * level);
    }

    // Actions
    private void loadSpawnPoint(Account account) {
        int spawnId = account.getSpawn();

        for (Spawn spawn : Spawn.values()) {
            if (spawn.getId() == spawnId) {
                setPlayerCoordinates(spawn.getX(), spawn.getY());
                return;
            }
        }

        setPlayerCoordinates(Spawn.INITIAL.getX(), Spawn.INITIAL.getY());
    }

    private void setPlayerCoordinates(int x, int y) {
        player.setSpawn(new Point(x * TILES_SIZE, y * TILES_SIZE));
    }

    public void changeExp(double value) {
        var pb = BalanceManager.getInstance().getPlayerBalance();
        int xpCapForLevel = pb.getXpPerLevelMultiplier() * level;

        double bonusPct = PerksBonus.getInstance().getBonusExp() / 100.0;
        exp += (int) Math.round(value * (1.0 + bonusPct));
        exp = Math.max(Math.min(exp, XP_CAP), 0);

        while (exp >= xpCapForLevel) {
            exp -= xpCapForLevel;
            level++;
            if (level % Math.max(1, pb.getLevelsPerToken()) == 0) {
                changeUpgradeTokens(pb.getTokensPerLevel());
            }
            xpCapForLevel = pb.getXpPerLevelMultiplier() * level;
            Framework.getInstance().getAccount().setLevel(level);
        }
        Framework.getInstance().getAccount().setExp(exp);
    }

    public void changeCoins(int value) {
        coins += value;
        coins = Math.max(coins, 0);
        Framework.getInstance().getAccount().setCoins(coins);
    }

    public void changeUpgradeTokens(int value) {
        upgradeTokens += value;
        upgradeTokens = Math.max(upgradeTokens, 0);
        Framework.getInstance().getAccount().setTokens(upgradeTokens);
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
