package platformer.model.spells;

public enum SpellType {
    FLAME_1(14),            // 0
    LIGHTNING(8),           // 1
    RORIC_BEAM(5),          // 2
    ARROW_RAIN(13),         // 3
    RORIC_SKY_BEAM(8),      // 4
    DASH_FIRE_TRAIL(1);     // 5

    private final int sprites;

    SpellType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }
}
