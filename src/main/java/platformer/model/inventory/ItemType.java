package platformer.model.inventory;


import platformer.constants.FilePaths;

import static platformer.constants.FilePaths.*;

/**
 * Enum for different types of items in the game.
 * Each item type has properties:
 * - `name`:        A string representing the name of the item.
 * - `rarity`:      An instance of the ItemRarity enum, indicating the rarity of the item.
 * - `img`:         A string representing the path to the image file for the item.
 * - `sellValue`:   An integer representing the value of the item when sold.
 * - `description`: A string providing a description of the item.
 * - `canEquip`:    A boolean indicating whether the item can be equipped by the player.
 */
public enum ItemType {
    HEALTH("Health", ItemRarity.COMMON, HEALTH_ITEM, 0, "", false),
    STAMINA("Stamina", ItemRarity.COMMON, STAMINA_ITEM, 0, "", false),
    // Ores
    COPPER("Copper Ore", ItemRarity.COMMON, COPPER_ORE_ITEM, 2, "Ingredient for crafting", false),
    IRON("Iron Ore", ItemRarity.UNCOMMON, IRON_ORE_ITEM, 5, "Ingredient for crafting", false),
    SILVER("Silver Ore", ItemRarity.UNCOMMON, SILVER_ORE_ITEM, 7, "Ingredient for crafting", false),
    AMETHYST("Amethyst Ore", ItemRarity.RARE, AMETHYST_ORE_ITEM, 10, "Ingredient for crafting", false),
    SONIC_QUARTZ("Sonic Quartz Ore", ItemRarity.RARE, SONIC_QUARTZ_ORE_ITEM, 15, "Ingredient for crafting", false),
    MAGMA("Magma Ore", ItemRarity.RARE, MAGMA_ORE_ITEM, 20, "Ingredient for crafting", false),
    ELECTRICITE("Electricite Ore", ItemRarity.LEGENDARY, ELECTRICITE_ORE_ITEM, 80, "Ingredient for crafting", false),
    ROSALLIUM("Rosallium Ore", ItemRarity.LEGENDARY, ROSALLIUM_ORE_ITEM, 100, "Ingredient for crafting", false),
    AZURELITE("Azurelite Ore", ItemRarity.EPIC, AZURELITE_ORE_ITEM, 50, "Ingredient for crafting", false),
    // Materials
    WRAITH_ESSENCE("Wraith Essence", ItemRarity.RARE, WRAITH_ESSENCE_ITEM, 18, "Essence of a wraith", false),
    // Equipment
    HELMET_WARRIOR("Warrior Helmet", ItemRarity.UNCOMMON, WARRIOR_HELMET, 30, "Helmet designed specifically for warriors \nwhich provides solid protection during combat. \n\nHealth Bonus +3%\nDefense Bonus +5%", true),
    ARMOR_WARRIOR("Warrior Armor", ItemRarity.UNCOMMON, WARRIOR_ARMOR, 40, "Warrior Armor is a rugged combat attire \ndesigned specifically for warriors.\n\nHealth Bonus +5%\nSpell Bonus +5%", true),
    BRACELETS_WARRIOR("Warrior Bracelets", ItemRarity.UNCOMMON, WARRIOR_BRACELETS, 20, "These Warrior Bracelets are wristbands\ntailored for warriors, enhancing their \nprotection in battle. \n\nAttack Bonus +5%\nCritical Bonus +3%", true),
    TROUSERS_WARRIOR("Warrior Trousers", ItemRarity.UNCOMMON, WARRIOR_TROUSERS, 30, "Warrior Trousers are specialized pants \ncrafted to provide warriors with both \ncomfort and protection. \n\nDefense Bonus +3%\nStamina Bonus +5%", true),
    BOOTS_WARRIOR("Warrior Boots", ItemRarity.UNCOMMON, WARRIOR_BOOTS, 20, "These boots are customized to meet the \ndemands of warriors. \n\nStamina Bonus +3%\nCooldown Bonus +3%", true),
    ARMOR_GUARDIAN("Guardian Armor", ItemRarity.RARE, GUARDIAN_ARMOR, 80, "Powerful ancient armor.\n\nHealth Bonus +10%\nAttack Bonus +5%", true),
    RING_AMETHYST("Amethyst Ring", ItemRarity.EPIC, AMETHYST_RING, 120, "This ring is a very rare piece of equipment.\n\nStamina Bonus +8%\nSpell Bonus +8%\nCooldown Bonus +5%", true),
    CHARM_THUNDERBOLT("Thunderbolt Charm", ItemRarity.MYTHIC, FilePaths.THUNDERBOLT_CHARM, 500, "This charm is a very rare piece of equipment.\n\nHealth Bonus +15%\nAttack Bonus +10%\nCritical Bonus +10%", true);

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
