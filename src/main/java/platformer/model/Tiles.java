package platformer.model;

public enum Tiles {
    TILES_DEFAULT_SIZE(32),
    SCALE(2f),
    TILES_WIDTH(26),
    TILES_HEIGHT(14),
    TILES_SIZE(Tiles.TILES_DEFAULT_SIZE.getValue()*Tiles.SCALE.getValue()),
    GAME_WIDTH(Tiles.TILES_SIZE.getValue()*Tiles.TILES_WIDTH.getValue()),
    GAME_HEIGHT(Tiles.TILES_SIZE.getValue()*Tiles.TILES_HEIGHT.getValue());

    private final float value;

    Tiles(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
