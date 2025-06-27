package platformer.model.gameObjects;

public enum ObjType {
    STAMINA_POTION(7),              // 0
    HEAL_POTION(7),                 // 1
    BOX(8),                         // 2
    BARREL(8),                      // 3
    SPIKE_UP(1),                       // 4
    ARROW_TRAP_LEFT(16),            // 5
    ARROW_TRAP_RIGHT(16),           // 6
    COIN(4),                        // 7
    SHOP(6),                        // 8
    BLOCKER(10),                    // 9
    BLACKSMITH(8),                  // 10
    DOG(8),                         // 11
    SAVE_TOTEM(1),                  // 12
    SMASH_TRAP(14),                 // 13
    CANDLE(1),                      // 14
    LOOT(1),                        // 15
    TABLE(1),                       // 16
    BOARD(1),                       // 17
    NPC(4),                         // 18
    LAVA(16),                       // 19
    BRICK(8),                       // 20
    JUMP_PAD(20),                	// 21
    SPIKE_DOWN(1),                  // 22
    SPIKE_RIGHT(1),					// 23
    SPIKE_LEFT(1),                  // 24
    MAX(0);                         // 25

    private final int sprites;

    ObjType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }

}
