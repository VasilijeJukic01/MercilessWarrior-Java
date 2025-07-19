package platformer.model.gameObjects.projectiles;

public enum PRType {
    ARROW(1),               // 0
    LIGHTNING_BALL(4),      // 1
    FIREBALL(4),            // 2
    RORIC_ARROW(1),         // 3
    RORIC_ANGLED_ARROW(1),	// 4
    CELESTIAL_ORB(4);		// 5

    private final int sprites;

    PRType(int sprites) {
        this.sprites = sprites;
    }

    public int getSprites() {
        return sprites;
    }
}
