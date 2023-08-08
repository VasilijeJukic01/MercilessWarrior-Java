package platformer.model.gameObjects.projectiles;

public enum PRType {
    ARROW(1),               // 0
    LIGHTNING_BALL(4);      // 1

    private final int sprites;

    PRType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }
}
