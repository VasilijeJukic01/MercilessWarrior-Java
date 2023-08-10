package platformer.model.entities.enemies;

public enum EnemyType {

    SKELETON(25, 15),       // 0
    GHOUL(40, 20),          // 1
    SPEAR_WOMAN(120, 25),   // 2
    MAX(1, 1);              // 3

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
