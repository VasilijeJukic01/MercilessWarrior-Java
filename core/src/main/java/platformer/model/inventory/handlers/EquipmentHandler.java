package platformer.model.inventory.handlers;

import platformer.model.inventory.InventoryBonus;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;

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
    private final Map<String, Integer> equipmentSlots;
    private final Map<Integer, String> slotIndexToName;

    public EquipmentHandler() {
        this.equipmentSlots = new HashMap<>();
        this.slotIndexToName = new HashMap<>();
        initEquipmentSlots();
    }

    private void initEquipmentSlots() {
        equipmentSlots.put("Helmet", 0);
        equipmentSlots.put("Trousers", 1);
        equipmentSlots.put("Armor", 2);
        equipmentSlots.put("Ring", 3);
        equipmentSlots.put("Charm", 3);
        equipmentSlots.put("Bracelets", 4);
        equipmentSlots.put("Boots", 5);

        for (Map.Entry<String, Integer> entry : equipmentSlots.entrySet()) {
            if (!slotIndexToName.containsKey(entry.getValue())) {
                slotIndexToName.put(entry.getValue(), entry.getKey());
            }
        }
        slotIndexToName.put(3, "Ring");
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
        if (index >= backpack.size() || index == -1) return;
        InventoryItem itemToEquip = backpack.get(index);
        ItemData data = itemToEquip.getData();
        if (data == null || data.equip == null || !data.equip.canEquip) return;
        Optional<Integer> slotIndexOptional = getSlotIndexForItem(data.equip.slot);
        if (slotIndexOptional.isEmpty()) return;
        int slotIndex = slotIndexOptional.get();

        // If slot is already occupied, we should unequip the existing item
        if (equipped[slotIndex] != null) {
            InventoryItem previouslyEquipped = unequipItem(slotIndex);
            if (previouslyEquipped != null) {
                backpackHandler.addItemToBackpack(previouslyEquipped);
            }
        }

        equipped[slotIndex] = itemToEquip;
        applyBonus(itemToEquip);
        backpackHandler.dropItem(index, true);
    }

    /**
     * Unequips an item from the equipment.
     * The item is removed from the equipment and added back to the backpack.
     * If dropFromBackpack is true, the item is dropped from the backpack.
     *
     * @param index The index of the item in the equipment to be unequipped.
     * @return The unequipped InventoryItem, or null if the index is invalid or the slot is empty.
     */
    public InventoryItem unequipItem(int index) {
        if (index >= equipped.length || equipped[index] == null) return null;
        InventoryItem itemToUnequip = equipped[index];
        removeBonus(itemToUnequip);
        equipped[index] = null;
        return itemToUnequip;
    }

    public boolean swapItems(int index1, int index2) {
        if (index1 < 0 || index1 >= equipped.length || index2 < 0 || index2 >= equipped.length) return false;

        InventoryItem item1 = equipped[index1];
        InventoryItem item2 = equipped[index2];
        String targetSlotType1 = slotIndexToName.get(index1);
        String targetSlotType2 = slotIndexToName.get(index2);
        if (targetSlotType1.equals("Charm")) targetSlotType1 = "Ring";
        if (targetSlotType2.equals("Charm")) targetSlotType2 = "Ring";

        if (item1 != null) {
            ItemData data = item1.getData();
            if (data == null || data.equip == null) return false;
            String itemSlotType = data.equip.slot;
            if (itemSlotType.equals("Charm")) itemSlotType = "Ring";
            if (!itemSlotType.equals(targetSlotType2)) {
                return false;
            }
        }

        if (item2 != null) {
            ItemData data = item2.getData();
            if (data == null || data.equip == null) return false;
            String itemSlotType = data.equip.slot;
            if (itemSlotType.equals("Charm")) itemSlotType = "Ring";
            if (!itemSlotType.equals(targetSlotType1)) {
                return false;
            }
        }
        equipped[index1] = item2;
        equipped[index2] = item1;

        return true;
    }

    public void applyBonus(InventoryItem item) {
        ItemData.EquipmentData equipData = item.getData().equip;
        if (equipData != null && equipData.bonuses != null) {
            equipData.bonuses.forEach(InventoryBonus.getInstance()::addBonus);
        }
    }

    private void removeBonus(InventoryItem item) {
        ItemData.EquipmentData equipData = item.getData().equip;
        if (equipData != null && equipData.bonuses != null) {
            equipData.bonuses.forEach(InventoryBonus.getInstance()::removeBonus);
        }
    }

    public boolean canEquipItem(InventoryItem item, int targetSlotIndex) {
        ItemData data = item.getData();
        if (data == null || data.equip == null || !data.equip.canEquip) return false;

        String targetSlotType = slotIndexToName.get(targetSlotIndex);
        if (targetSlotType == null) return false;

        String itemSlotType = data.equip.slot;
        if (targetSlotType.equals("Charm")) targetSlotType = "Ring";
        if (itemSlotType.equals("Charm")) itemSlotType = "Ring";

        return targetSlotType.equals(itemSlotType);
    }

    /**
     * Retrieves the index of the equipment slot for a given slot name.
     * The index is determined by looking up the slot name in the equipmentSlots.
     *
     * @param slotName The name of the slot (e.g., "Helmet", "Armor").
     * @return An Optional containing the slot index if found, otherwise an empty Optional.
     */
    private Optional<Integer> getSlotIndexForItem(String slotName) {
        return Optional.ofNullable(equipmentSlots.get(slotName));
    }

    public void reset() {
        for (int i = 0; i < equipped.length; i++) {
            if (equipped[i] != null) {
                removeBonus(equipped[i]);
                equipped[i] = null;
            }
        }
    }

    public InventoryItem[] getEquipped() {
        return equipped;
    }

}
