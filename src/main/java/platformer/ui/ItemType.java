package platformer.ui;

public enum ItemType {
    HEALTH("Health", 0, ""),
    STAMINA("Stamina", 0, ""),
    IRON("Iron Ore", 5, "Ingredient for crafting"),
    COPPER("Copper Ore", 2, "Ingredient for crafting");

    private final String name;
    private final int sellValue;
    private final String description;

    ItemType(String name, int sellValue, String description) {
        this.name = name;
        this.sellValue = sellValue;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getSellValue() {
        return sellValue;
    }

    public String getDescription() {
        return description;
    }
}
