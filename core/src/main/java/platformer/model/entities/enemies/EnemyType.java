package platformer.model.entities.enemies;

import platformer.core.config.BalanceManager;

public enum EnemyType {

    SKELETON(25, 15),       // 0
    GHOUL(40, 20),          // 1
    LANCER(120, 25),        // 2
    KNIGHT(50, 30),         // 3
    WRAITH(50, 40),         // 4
    RORIC(999, 30),         // 5
    MAX(1, 1);              // 6

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
