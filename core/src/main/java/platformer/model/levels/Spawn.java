package platformer.model.levels;

/**
 * Enum representing the spawn points of the player.
 */
public enum Spawn {
    // FORMAT: ID, LevelI, LevelJ, TileX, TileY
    INITIAL(0, 0, 0, 5, 10),
    WORKSHOP(1, 0, 2, 14, 16),
    WILDERNESS(2, 1, 3, 11, 25);

    private final int id;
    private final int levelI, levelJ;
    private final int x, y;

    Spawn(int id, int levelI, int levelJ, int x, int y) {
        this.id = id;
        this.levelI = levelI;
        this.levelJ = levelJ;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getLevelI() {
        return levelI;
    }

    public int getLevelJ() {
        return levelJ;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
