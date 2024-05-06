package platformer.model.inventory.handlers;

import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.ItemBonus;
import platformer.model.inventory.ItemType;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the bonuses related to different item types in the inventory system.
 */
public class BonusHandler {

    private final Map<ItemType, ItemBonus> bonuses;

    public BonusHandler() {
        this.bonuses = new HashMap<>();
        initBonuses();
    }

    private void initBonuses() {
        bonuses.put(ItemType.HELMET_WARRIOR,    ItemBonus.HELMET_WARRIOR);
        bonuses.put(ItemType.ARMOR_WARRIOR,     ItemBonus.ARMOR_WARRIOR);
        bonuses.put(ItemType.BRACELETS_WARRIOR, ItemBonus.BRACELETS_WARRIOR);
        bonuses.put(ItemType.TROUSERS_WARRIOR,  ItemBonus.TROUSERS_WARRIOR);
        bonuses.put(ItemType.BOOTS_WARRIOR,     ItemBonus.BOOTS_WARRIOR);
        bonuses.put(ItemType.ARMOR_GUARDIAN,    ItemBonus.ARMOR_GUARDIAN);
        bonuses.put(ItemType.RING_AMETHYST,     ItemBonus.RING_AMETHYST);
        bonuses.put(ItemType.CHARM_THUNDERBOLT, ItemBonus.RING_THUNDERBOLT_CHARM);
    }

    /**
     * Applies the bonus associated with the given item type.
     * If a bonus exists for the item type, it is applied to the inventory bonus.
     *
     * @param itemType The type of the item for which to apply the bonus.
     */
    public void applyBonus(ItemType itemType) {
        ItemBonus bonus = bonuses.get(itemType);
        if (bonus != null) {
            InventoryBonus.getInstance().applyBonus(bonus);
        }
    }

    /**
     * Removes the bonus associated with the given item type.
     * If a bonus exists for the item type, it is removed from the inventory bonus.
     *
     * @param itemType The type of the item for which to remove the bonus.
     */
    public void removeBonus(ItemType itemType) {
        ItemBonus bonus = bonuses.get(itemType);
        if (bonus != null) {
            InventoryBonus.getInstance().removeBonus(bonus);
        }
    }

}
