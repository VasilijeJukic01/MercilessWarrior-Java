package platformer.model.levels;

public enum Spawn {
    INITIAL(0, 5, 10),
    SPAWN_1(1, 14, 16);

    private final int id;
    private final int x;
    private final int y;

    Spawn(int id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

}
