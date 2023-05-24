package platformer.model.entities.enemies;

import platformer.model.Tiles;

public enum EnemySize {

    SKELETON_DEFAULT_WIDTH(100),
    SKELETON_DEFAULT_HEIGHT(90),
    SKELETON_WIDTH((int)(SKELETON_DEFAULT_WIDTH.getValue() * Tiles.SCALE.getValue())),
    SKELETON_HEIGHT((int)(SKELETON_DEFAULT_HEIGHT.getValue() * Tiles.SCALE.getValue())),
    SKELETON_X_OFFSET((int)(40*Tiles.SCALE.getValue())),
    SKELETON_Y_OFFSET((int)(24*Tiles.SCALE.getValue())),

    GHOUL_DEFAULT_WIDTH(120),
    GHOUL_DEFAULT_HEIGHT(80),
    GHOUL_WIDTH((int)(GHOUL_DEFAULT_WIDTH.getValue() * Tiles.SCALE.getValue())),
    GHOUL_HEIGHT((int)(GHOUL_DEFAULT_HEIGHT.getValue() * Tiles.SCALE.getValue())),
    GHOUL_X_OFFSET((int)(50*Tiles.SCALE.getValue())),
    GHOUL_Y_OFFSET((int)(18*Tiles.SCALE.getValue()));

    private final int value;

    EnemySize(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
