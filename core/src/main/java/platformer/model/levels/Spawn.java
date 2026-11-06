package platformer.model.levels;

/**
 * Enum representing the save totem spawn points of the player across the world graph.
 */
// TODO: Do something with this later
public enum Spawn {
    // FORMAT: ID, LevelID, TileX, TileY
    INITIAL(0, "begin", 5, 10),
    WORKSHOP(1, "forest02", 14, 16),
    WILDERNESS(2, "forest13", 11, 25),
    DEEPMOSS(3, "deepmoss02", 25, 9);

    private final int id;
    private final String levelId;
    private final int x, y;

    Spawn(int id, String levelId, int x, int y) {
        this.id = id;
        this.levelId = levelId;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public String getLevelId() {
        return levelId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
