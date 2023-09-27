package platformer.model.inventory;


import static platformer.constants.FilePaths.*;

public enum ItemType {
    HEALTH("Health", ItemRarity.COMMON, HEALTH_ITEM, 0, "", false),
    STAMINA("Stamina", ItemRarity.COMMON, STAMINA_ITEM, 0, "", false),
    IRON("Iron Ore", ItemRarity.UNCOMMON, IRON_ORE_ITEM, 5, "Ingredient for crafting", false),
    COPPER("Copper Ore", ItemRarity.COMMON, COPPER_ORE_ITEM, 2, "Ingredient for crafting", false),
    ARMOR_WARRIOR("Warrior Armor", ItemRarity.UNCOMMON, WARRIOR_ARMOR, 40, "Warrior Armor is a rugged combat attire designed \nspecifically for warriors.\n\nHealth Bonus +2%\nSpell Bonus +3%", true);

    private final String name;
    private final ItemRarity rarity;
    private final String img;
    private final int sellValue;
    private final String description;
    private final boolean canEquip;

    ItemType(String name, ItemRarity rarity, String img, int sellValue, String description, boolean canEquip) {
        this.name = name;
        this.rarity = rarity;
        this.img = img;
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

    public String getImg() {
        return img;
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
