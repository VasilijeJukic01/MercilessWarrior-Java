package platformer.model;

/**
 * This class represents a single item in the leaderboard.
 * It contains the name of the player, the level they are on, and the amount of experience they have.
 */
public class BoardItem {

    private final String name;
    private final int level;
    private final int exp;

    public BoardItem(String name, int level, int exp) {
        this.name = name;
        this.level = level;
        this.exp = exp;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level+"";
    }

    public String getExp() {
        return exp+"";
    }


}
