package platformer.model.entities.enemies;

public enum EnemyType {

    SKELETON(25, 15),       // 0
    GHOUL(40, 20),          // 1
    LANCER(120, 25),   		// 2
    KNIGHT(50, 30),         // 3
    WRAITH(50, 40),         // 4
    RORIC(999, 30),         // 5
    MAX(1, 1);              // 6

    private final int health;
    private final int damage;

    EnemyType(int health, int damage) {
        this.health = health;
        this.damage = damage;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

}
