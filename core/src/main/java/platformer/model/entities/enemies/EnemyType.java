package platformer.model.entities.enemies;

import platformer.core.config.BalanceManager;

public enum EnemyType {

    SKELETON(25, 15),           // 0
    GHOUL(40, 20),              // 1
    LANCER(120, 25),            // 2
    KNIGHT(50, 30),             // 3
    WRAITH(50, 40),             // 4
    RORIC(999, 30),             // 5
    CORRUPTED_KNIGHT(80, 32),   // 6
    GOBLIN(35, 16),             // 7
    GOBLIN_SOLDIER(52, 18),     // 8
    GOBLIN_ASSASSIN(40, 20),    // 9
    GOBLIN_SPEARMAN(38, 18),    // 10
    GOBLIN_MAGE(38, 22),        // 11
    BRIAR_HUSK(58, 22),         // 12
    BLIGHT_BEAR(95, 32),        // 13
    WIDOW_SPIDER(42, 18),       // 14
    DEATH_RIDER(110, 34),       // 15
    ROOT_WEAVER(78, 26),        // 16
    CRIMSON_SKELETON(55, 24),   // 17
    ASHEN_GHOUL(68, 28),        // 18
    MAX(1, 1);                  // 19

    private final int defaultHealth;
    private final int defaultDamage;

    EnemyType(int defaultHealth, int defaultDamage) {
        this.defaultHealth = defaultHealth;
        this.defaultDamage = defaultDamage;
    }

    public int getHealth() {
        if (this == MAX) return defaultHealth;
        return BalanceManager.getInstance().getEnemyBalance(this).getHealth();
    }

    public int getDamage() {
        if (this == MAX) return defaultDamage;
        return BalanceManager.getInstance().getEnemyBalance(this).getDamage();
    }
}