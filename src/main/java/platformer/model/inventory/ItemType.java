package platformer.model.inventory;

public enum ItemType {
    HEALTH("Health", ItemRarity.COMMON, 0, "", false),
    STAMINA("Stamina", ItemRarity.COMMON, 0, "", false),
    IRON("Iron Ore", ItemRarity.UNCOMMON, 5, "Ingredient for crafting", false),
    COPPER("Copper Ore", ItemRarity.COMMON, 2, "Ingredient for crafting", false),
    ARMOR_WARRIOR("Warrior Armor", ItemRarity.UNCOMMON, 40, "Warrior Armor is a rugged combat attire designed \nspecifically for warriors.\n\nHealth Bonus +2%\nSpell Bonus +3%", true);

    private final String name;
    private final ItemRarity rarity;
    private final int sellValue;
    private final String description;
    private final boolean canEquip;

    ItemType(String name, ItemRarity rarity, int sellValue, String description, boolean canEquip) {
        this.name = name;
        this.rarity = rarity;
        this.sellValue = sellValue;
        this.description = description;
        this.canEquip = canEquip;
    }

    public String getName() {
        return name;
    }

    public ItemRarity getRarity() {
        return rarity;
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
