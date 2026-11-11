package platformer.core.config;

import com.google.gson.Gson;
import lombok.Getter;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.enemies.EnemyType;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import static platformer.constants.FilePaths.BALANCE_PATH;

/**
 * Loads and provides data-driven gameplay balance, combat, and economy parameters from balance.json.
 */
public class BalanceManager {

    private static volatile BalanceManager instance;
    private BalanceConfig config;

    @Getter
    public static class BalanceConfig {
        private PlayerBalance player = new PlayerBalance();
        private ConsumableBalance consumables = new ConsumableBalance();
        private CombatBalance combat = new CombatBalance();
        private Map<String, EnemyBalance> enemies = Collections.emptyMap();
    }

    @Getter
    public static class PlayerBalance {
        private int baseHealth = 100;
        private int baseStamina = 100;
        private double initialStamina = 15;
        private int baseAttackDamage = 5;
        private int transformedAttackDamage = 8;
        private int baseCritChance = 10;
        private double baseCritMultiplier = 2.0;
        private int meleeHitStaminaMin = 1;
        private int meleeHitStaminaMax = 3;
        private double parryStaminaRestore = 10;
        private double dashStaminaCost = 3.0;
        private double fireballStaminaCost = 15.0;
        private double flameStaminaDrainPerTick = 0.20;
        private double transformStaminaDrainPerTick = 0.025;
        private double attackCooldown = 0.75;
        private double blockCooldown = 1.20;
        private double dashCooldown = 1.75;
        private double spellCooldown = 2.50;
        private int xpPerLevelMultiplier = 850;
        private int tokensPerLevel = 1;
        private int levelsPerToken = 1;
    }

    @Getter
    public static class ConsumableBalance {
        private int healthPotionRestore = 25;
        private int staminaPotionRestore = 20;
    }

    @Getter
    public static class CombatBalance {
        private double playerFireballDamage = 15;
        private double flameTickDamage = 0.10;
        private double flameUndeadMultiplier = 1.6;
        private double enemyProjectileToPlayerDamage = 10;
        private double trapProjectileToEnemyDamage = 8;
        private double lavaTickDamage = 0.5;
        private double trapInstantKillDamage = 500;
        private int roricTrapDamage = 10;
        private int roricTrapHealth = 10;
        private double lightningSpellDamage = 20;
        private double flashSpellDamage = 10;
        private double arrowRainSpellDamage = 10;
        private double roricBeamSpellDamage = 15;
        private double roricSkyBeamSpellDamage = 15;
    }

    @Getter
    public static class EnemyBalance {
        private int health = 25;
        private int damage = 15;
        private int minCoins = 5;
        private int maxCoins = 10;
        private int minExp = 80;
        private int maxExp = 100;
        private int tokenReward = 0;
    }

    private BalanceManager() {
        loadBalance();
    }

    public static BalanceManager getInstance() {
        if (instance == null) {
            synchronized (BalanceManager.class) {
                if (instance == null) {
                    instance = new BalanceManager();
                }
            }
        }
        return instance;
    }

    private void loadBalance() {
        try (InputStream is = getClass().getResourceAsStream(BALANCE_PATH)) {
            if (is != null) {
                try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
                    this.config = new Gson().fromJson(reader, BalanceConfig.class);
                }
            }
        } catch (Exception e) {
            Logger.getInstance().notify("Failed to load balance.json: " + e.getMessage(), Message.ERROR);
        }
        if (this.config == null) {
            this.config = new BalanceConfig();
        }
    }

    public PlayerBalance getPlayerBalance() {
        return config.getPlayer();
    }

    public ConsumableBalance getConsumables() {
        return config.getConsumables();
    }

    public CombatBalance getCombat() {
        return config.getCombat();
    }

    public EnemyBalance getEnemyBalance(EnemyType type) {
        if (type == null || config.getEnemies() == null) return new EnemyBalance();
        return config.getEnemies().getOrDefault(type.name(), new EnemyBalance());
    }
}