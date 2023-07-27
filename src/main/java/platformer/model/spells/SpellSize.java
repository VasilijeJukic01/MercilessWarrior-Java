package platformer.model.spells;


import static platformer.constants.Constants.TILES_SIZE;

public enum SpellSize {

    LIGHTNING_DEFAULT_WIDTH((int)(1.5*TILES_SIZE)),
    LIGHTNING_DEFAULT_HEIGHT((int)(8*TILES_SIZE)),
    LIGHTNING_WIDTH(LIGHTNING_DEFAULT_WIDTH.getValue()),
    LIGHTNING_HEIGHT(LIGHTNING_DEFAULT_HEIGHT.getValue()),

    FLASH_DEFAULT_WIDTH((int)(1.5*TILES_SIZE)),
    FLASH_DEFAULT_HEIGHT((int)(6*TILES_SIZE)),
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
