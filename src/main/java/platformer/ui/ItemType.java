package platformer.ui;

public enum ItemType {
    HEALTH("Health", 0, "", false),
    STAMINA("Stamina", 0, "", false),
    IRON("Iron Ore", 5, "Ingredient for crafting", false),
    COPPER("Copper Ore", 2, "Ingredient for crafting", false),
    ARMOR_WARRIOR("Warrior Armor", 40, "Warrior Armor is a rugged combat attire designed \nspecifically for warriors.\n\nHealth Bonus +2%\nSpell Bonus +3%", true);

    private final String name;
    private final int sellValue;
    private final String description;
    private final boolean canEquip;

    ItemType(String name, int sellValue, String description, boolean canEquip) {
        this.name = name;
        this.sellValue = sellValue;
        this.description = description;
        this.canEquip = canEquip;
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

    public boolean canEquip() {
        return canEquip;
    }
}
