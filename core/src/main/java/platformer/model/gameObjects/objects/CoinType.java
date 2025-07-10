package platformer.model.gameObjects.objects;

public enum CoinType {
    BRONZE(1, 0),
    SILVER(2, 1),
    GOLD(5, 2);

    private final int value;
    private final int animationRow;

    CoinType(int value, int animationRow) {
        this.value = value;
        this.animationRow = animationRow;
    }

    public int getValue() {
        return value;
    }

    public int getAnimationRow() {
        return animationRow;
    }
}