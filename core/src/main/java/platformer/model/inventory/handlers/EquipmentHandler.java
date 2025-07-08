package platformer.model.inventory.handlers;

import platformer.model.inventory.InventoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        InventoryItem itemToEquip = backpack.get(index);
        if (!itemToEquip.getItemType().canEquip()) return;

        Optional<Integer> slotIndexOptional = getSlotIndexForItem(itemToEquip);
        if (slotIndexOptional.isEmpty()) return;
        int slotIndex = slotIndexOptional.get();

        // If slot is already occupied, we should unequip the existing item
        if (equipped[slotIndex] != null) {
            unequipItem(slotIndex, backpackHandler, false);
        }

        equipped[slotIndex] = itemToEquip;
        bonusHandler.applyBonus(itemToEquip.getItemType());
        backpackHandler.dropItem(index, true);
    }

    public void unequipItem(int index, BackpackHandler backpackHandler) {
        unequipItem(index, backpackHandler, true);
    }

    /**
     * Unequips an item from the equipment.
     * The item is removed from the equipment and added back to the backpack.
     * If dropFromBackpack is true, the item is dropped from the backpack.
     *
     * @param index The index of the item in the equipment to be unequipped.
     * @param backpackHandler The BackpackHandler instance used to manage backpack-related operations.
     * @param dropFromBackpack Whether to drop the item from the backpack or not.
     */
    private void unequipItem(int index, BackpackHandler backpackHandler, boolean dropFromBackpack) {
        if (index >= equipped.length || equipped[index] == null) return;

        InventoryItem itemToUnequip = equipped[index];
        bonusHandler.removeBonus(itemToUnequip.getItemType());
        backpackHandler.addItemAmountToBackpack(itemToUnequip, 1);
        equipped[index] = null;
    }

    /**
     * Retrieves the index of the equipment slot for a given item.
     * The index is determined based on the item's type name.
     *
     * @param item The InventoryItem for which to find the slot index.
     * @return An Optional containing the slot index if found, otherwise an empty Optional.
     */
    private Optional<Integer> getSlotIndexForItem(InventoryItem item) {
        String name = item.getItemType().getName();
        return equipment.keySet().stream()
                .filter(name::contains)
                .map(equipment::get)
                .findFirst();
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
