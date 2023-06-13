package platformer.model.spells;

import platformer.model.Tiles;

public enum SpellSize {

    LIGHTNING_DEFAULT_WIDTH((int)(1*Tiles.TILES_SIZE.getValue())),
    LIGHTNING_DEFAULT_HEIGHT((int)(4*Tiles.TILES_SIZE.getValue())),
    LIGHTNING_WIDTH((int)(LIGHTNING_DEFAULT_WIDTH.getValue() * Tiles.SCALE.getValue())),
    LIGHTNING_HEIGHT((int)( LIGHTNING_DEFAULT_HEIGHT.getValue() * Tiles.SCALE.getValue()));

    private final int value;

    SpellSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
