package platformer.model.inventory;


import static platformer.constants.FilePaths.*;

public enum ItemType {
    HEALTH("Health", ItemRarity.COMMON, HEALTH_ITEM, 0, "", false),
    STAMINA("Stamina", ItemRarity.COMMON, STAMINA_ITEM, 0, "", false),
    IRON("Iron Ore", ItemRarity.UNCOMMON, IRON_ORE_ITEM, 5, "Ingredient for crafting", false),
    COPPER("Copper Ore", ItemRarity.COMMON, COPPER_ORE_ITEM, 2, "Ingredient for crafting", false),
    AMETHYST("Amethyst Ore", ItemRarity.RARE, AMETHYST_ORE_ITEM, 10, "Ingredient for crafting", false),
    SONIC_QUARTZ("Sonic Quartz Ore", ItemRarity.RARE, SONIC_QUARTZ_ORE_ITEM, 15, "Ingredient for crafting", false),
    ELECTRICITE("Electricite Ore", ItemRarity.LEGENDARY, ELECTRICITE_ORE_ITEM, 80, "Ingredient for crafting", false),
    HELMET_WARRIOR("Warrior Helmet", ItemRarity.UNCOMMON, WARRIOR_HELMET, 30, "Helmet designed specifically for warriors \nwhich provides solid protection during combat. \n\nHealth Bonus +3%\nDefense Bonus +5%", true),
    ARMOR_WARRIOR("Warrior Armor", ItemRarity.UNCOMMON, WARRIOR_ARMOR, 40, "Warrior Armor is a rugged combat attire \ndesigned specifically for warriors.\n\nHealth Bonus +5%\nSpell Bonus +5%", true),
    BRACELETS_WARRIOR("Warrior Bracelets", ItemRarity.UNCOMMON, WARRIOR_BRACELETS, 20, "These Warrior Bracelets are wristbands\ntailored for warriors, enhancing their \nprotection in battle. \n\nAttack Bonus +5%\nCritical Bonus +3%", true),
    TROUSERS_WARRIOR("Warrior Trousers", ItemRarity.UNCOMMON, WARRIOR_TROUSERS, 30, "Warrior Trousers are specialized pants \ncrafted to provide warriors with both \ncomfort and protection. \n\nDefense Bonus +3%\nStamina Bonus +5%", true),
    BOOTS_WARRIOR("Warrior Boots", ItemRarity.UNCOMMON, WARRIOR_BOOTS, 20, "These boots are customized to meet the \ndemands of warriors. \n\nStamina Bonus +3%\nCooldown Bonus +3%", true),
    ARMOR_GUARDIAN("Guardian Armor", ItemRarity.RARE, GUARDIAN_ARMOR, 80, "Powerful ancient armor.\n\nHealth Bonus +10%\nAttack Bonus +5%", true),
    RING_AMETHYST("Amethyst Ring", ItemRarity.EPIC, AMETHYST_RING, 120, "This ring is a very rare piece of equipment.\n\nStamina Bonus +8%\nSpell Bonus +8%\nCooldown Bonus +5%", true);

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
