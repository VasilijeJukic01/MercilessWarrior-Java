package platformer.model;

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
