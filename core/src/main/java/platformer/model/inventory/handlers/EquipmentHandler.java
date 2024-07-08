package platformer.model.inventory.handlers;

import platformer.model.inventory.InventoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles the equipment in the inventory system.
 * It provides methods to equip and unequip items, among other functionalities.
 */
public class EquipmentHandler {

    private final InventoryItem[] equipped = new InventoryItem[6];
    private final Map<String, Integer> equipment;

    private final BonusHandler bonusHandler;

    public EquipmentHandler() {
        this.equipment = new HashMap<>();
        this.bonusHandler = new BonusHandler();
        initEquipment();
    }

    /**
     * Initializes the equipment map with predefined equipment slots.
     */
    private void initEquipment() {
        equipment.put("Helmet",     0);
        equipment.put("Armor",      2);
        equipment.put("Bracelets",  4);
        equipment.put("Trousers",   1);
        equipment.put("Ring",       3);
        equipment.put("Charm",      3);
        equipment.put("Boots",      5);
    }

    /**
     * Equips an item from the backpack to the equipment.
     * If the item can be equipped, it is added to the equipment and removed from the backpack.
     * The bonus associated with the item type is also applied.
     *
     * @param backpackHandler The BackpackHandler instance used to manage backpack-related operations.
     * @param index The index of the item in the backpack to be equipped.
     */
    public void equipItem(BackpackHandler backpackHandler, int index) {
        List<InventoryItem> backpack = backpackHandler.getBackpack();
        if (index >= backpack.size()) return;
        if (!backpack.get(index).getItemType().canEquip()) return;
        addToEquipment(backpack.get(index), equipment);
        bonusHandler.applyBonus(backpack.get(index).getItemType());
        backpackHandler.dropItem(index);
    }

    /**
     * Unequips an item from the equipment and adds it back to the backpack.
     * The bonus associated with the item type is also removed.
     *
     * @param index The index of the item in the equipment to be unequipped.
     * @param backpack The BackpackHandler instance used to manage backpack-related operations.
     */
    public void unequipItem(int index, BackpackHandler backpack) {
        if (index >= equipped.length) return;
        if (equipped[index] == null) return;
        bonusHandler.removeBonus(equipped[index].getItemType());
        equipped[index].addAmount(1);
        backpack.addItemAmountToBackpack(equipped[index], 1);
        equipped[index] = null;
    }

    /**
     * Adds an item to the equipment.
     * The item is added to the slot that matches its type.
     *
     * @param item The item to be added to the equipment.
     * @param equipment The map of equipment slots.
     */
    private void addToEquipment(InventoryItem item, Map<String, Integer> equipment) {
        String name = item.getItemType().getName();
        equipment.keySet().stream()
                .filter(name::contains)
                .findFirst()
                .map(equipment::get).ifPresent(index -> equipped[index] = item);
    }

    public void reset() {
        for (int i = 0; i < equipped.length; i++) {
            if (equipped[i] != null) {
                bonusHandler.removeBonus(equipped[i].getItemType());
                equipped[i] = null;
            }
        }
    }

    public InventoryItem[] getEquipped() {
        return equipped;
    }

}
