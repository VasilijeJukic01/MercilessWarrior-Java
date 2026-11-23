package platformer.model.inventory.item;

import java.util.Map;

/**
 * Represents the data class for an item in the game inventory.
 */
public class ItemData {

    public String name;
    public String description;
    public ItemRarity rarity;
    public String imagePath;
    public int sellValue;
    public boolean stackable;
    public boolean questItem;
    public EquipmentData equip;

    public static class EquipmentData {
        public boolean canEquip;
        public String slot;
        public String setName;
        public Map<String, Double> bonuses;
    }

}
