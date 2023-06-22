package platformer.model.spells;

import platformer.model.Tiles;

public enum SpellSize {

    LIGHTNING_DEFAULT_WIDTH((int)(1.5*Tiles.TILES_SIZE.getValue())),
    LIGHTNING_DEFAULT_HEIGHT((int)(8*Tiles.TILES_SIZE.getValue())),
    LIGHTNING_WIDTH(LIGHTNING_DEFAULT_WIDTH.getValue()),
    LIGHTNING_HEIGHT(LIGHTNING_DEFAULT_HEIGHT.getValue()),

    FLASH_DEFAULT_WIDTH((int)(1.5*Tiles.TILES_SIZE.getValue())),
    FLASH_DEFAULT_HEIGHT((int)(6*Tiles.TILES_SIZE.getValue())),
    FLASH_WIDTH(FLASH_DEFAULT_WIDTH.getValue()),
    FLASH_HEIGHT(FLASH_DEFAULT_HEIGHT.getValue());

    private final int value;

    SpellSize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
