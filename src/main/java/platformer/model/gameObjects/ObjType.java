package platformer.model.gameObjects;

public enum ObjType {
    STAMINA_POTION(7),              // 0
    HEAL_POTION(7),                 // 1
    BOX(8),                         // 2
    BARREL(8),                      // 3
    SPIKE(1),                       // 4
    ARROW_TRAP_LEFT(16),            // 5
    ARROW_TRAP_RIGHT(16),           // 6
    COIN(4),                        // 7
    SHOP(6),                        // 8
    BLOCKER(10),                    // 9
    BLACKSMITH(8),                  // 10
    DOG(8),                         // 11
    MAX(0);                         // 12

    private final int sprites;

    ObjType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }

}