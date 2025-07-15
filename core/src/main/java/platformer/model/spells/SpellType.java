package platformer.model.spells;

public enum SpellType {
    FLAME_1(14),        // 0
    LIGHTNING(8),       // 1
    FLASH(16),          // 2
    RORIC_BEAM(5);          // 3

    private final int sprites;

    SpellType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }
}
