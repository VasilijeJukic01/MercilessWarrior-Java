package platformer.ui;

public enum ItemType {
    HEALTH(0, ""),
    STAMINA(0, ""),
    IRON(5, "Ingredient for crafting"),
    COPPER(2, "Ingredient for crafting");

    private final int sellValue;
    private final String description;

    ItemType(int sellValue, String description) {
        this.sellValue = sellValue;
        this.description = description;
    }

    public int getSellValue() {
        return sellValue;
    }

    public String getDescription() {
        return description;
    }
}
