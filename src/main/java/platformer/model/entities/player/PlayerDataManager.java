package platformer.model.entities.player;

import platformer.core.Account;
import platformer.core.Framework;
import platformer.model.inventory.InventoryBonus;
import platformer.model.levels.Spawn;
import platformer.model.perks.PerksBonus;
import platformer.ui.overlays.hud.UserInterface;

import java.awt.*;

import static platformer.constants.Constants.*;

public class PlayerDataManager {

    private final Player player;
    private final UserInterface userInterface;

    private int coins = 0;
    private int exp = 0;
    private int level = 1;
    private int upgradeTokens = 0;

    public PlayerDataManager(Player player) {
        this.player = player;
        this.userInterface = new UserInterface(player);
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

    private void loadSpawnPoint(Account account) {
        if (account.getSpawn() == -1) {
            setPlayerCoordinates(Spawn.INITIAL.getX(), Spawn.INITIAL.getY());
            return;
        }

        for (Spawn spawn : Spawn.values()) {
            if (spawn.getId() == account.getSpawn()) {
                setPlayerCoordinates(spawn.getX(), spawn.getY());
                break;
            }
        }
    }

    private void setPlayerCoordinates(int x, int y) {
        player.setSpawn(new Point(x * TILES_SIZE, y * TILES_SIZE));
    }

    public void update() {
        double currentHealth = player.getCurrentHealth();
        double currentStamina = player.getCurrentStamina();
        double maxHealth = PLAYER_MAX_HP + PerksBonus.getInstance().getBonusHealth();
        double equipmentBonus = InventoryBonus.getInstance().getHealth() * maxHealth;
        maxHealth += equipmentBonus;
        double maxStamina = PLAYER_MAX_ST + PerksBonus.getInstance().getBonusPower();
        userInterface.update(currentHealth, maxHealth, currentStamina, maxStamina, exp, 1000*level);
    }

    public void changeExp(double value) {
        exp += (int) (value + PerksBonus.getInstance().getBonusExp());
        exp = Math.max(Math.min(exp, XP_CAP), 0);
        if (exp > 1000*level) {
            exp = exp % (1000*level);
            level++;
            if (level % 2 == 0) changeUpgradeTokens(1);
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
