package platformer.model.inventory;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.handlers.BackpackHandler;
import platformer.model.inventory.handlers.EquipmentHandler;
import platformer.model.inventory.item.InventoryItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class that represents the player's inventory.
 * It provides methods to manage items in the inventory.
 */
public class Inventory {

    private final EquipmentHandler equipmentHandler;
    private final BackpackHandler backpackHandler;
    private final String[] quickUseSlots = new String[4];

    public Inventory() {
        this.equipmentHandler = new EquipmentHandler();
        this.backpackHandler = new BackpackHandler(equipmentHandler);
        Arrays.fill(quickUseSlots, null);
    }

    /**
     * Uses an item from the backpack.
     *
     * @param index the index of the item in the backpack
     */
    public void useItem(int index, Player player) {
        backpackHandler.useItem(index, player);
        checkQuickSlots();
    }

    /**
     * Uses an item from a quick-use slot.
     *
     * @param slotIndex The index of the quick-use slot (0-3).
     * @param player The player using the item.
     */
    public void useQuickSlotItem(int slotIndex, Player player) {
        if (slotIndex < 0 || slotIndex >= quickUseSlots.length) return;

        String itemId = quickUseSlots[slotIndex];
        if (itemId == null) return;
        Optional<InventoryItem> itemToUse = backpackHandler.getBackpack().stream()
                .filter(item -> item.getItemId().equals(itemId))
                .findFirst();

        itemToUse.ifPresent(item -> useItem(backpackHandler.getBackpack().indexOf(item), player));
    }

    /**
     * Assigns an item from the backpack to a quick-use slot.
     *
     * @param backpackIndex The index of the item in the backpack.
     * @param quickSlotIndex The index of the quick-use slot (0-3).
     */
    public void assignToQuickSlot(int backpackIndex, int quickSlotIndex) {
        if (backpackIndex >= backpackHandler.getBackpack().size() || quickSlotIndex >= quickUseSlots.length) return;
        InventoryItem item = backpackHandler.getBackpack().get(backpackIndex);
        if (item.getItemId().contains("POTION")) {
            quickUseSlots[quickSlotIndex] = item.getItemId();
        }
    }

    /**
     * Equips an item from the backpack.
     *
     * @param index the index of the item in the backpack
     */
    public void equipItem(int index) {
        equipmentHandler.equipItem(backpackHandler, index);
    }

    /**
     * Drops an item from the backpack.
     *
     * @param index the index of the item in the backpack
     */
    public void dropItem(int index) {
        backpackHandler.dropItem(index);
    }

    public void swapBackpackItems(int index1, int index2) {
        backpackHandler.swapItems(index1, index2);
    }

    public void moveEquipToBackpack(int equipIndex, int backpackIndex) {
        InventoryItem item = equipmentHandler.unequipItem(equipIndex);
        if (item != null) {
            backpackHandler.insertOrAddItem(backpackIndex, item);
        }
    }

    public void swapEquipmentItems(int index1, int index2) {
        if (equipmentHandler.swapItems(index1, index2)) {
            backpackHandler.refreshAccountItems();
        }
    }

    /**
     * Unequips an item from the equipment.
     *
     * @param index the index of the item in the equipment
     */
    public void unequipItem(int index) {
        InventoryItem unequippedItem = equipmentHandler.unequipItem(index);
        if (unequippedItem != null) {
            backpackHandler.addItemToBackpack(unequippedItem);
        }
    }

    /**
     * Adds an item to the backpack.
     *
     * @param item the item to be added
     */
    public void addItemToBackpack(InventoryItem item) {
        backpackHandler.addItemToBackpack(item);
    }

    /**
     * Adds all items from a loot to the backpack.
     *
     * @param loot the loot whose items are to be added
     */
    public void addAllItemsFromLoot(Loot loot) {
        loot.getItems().forEach(this::addItemToBackpack);
    }

    /**
     * Crafts an item and adds it to the backpack.
     *
     * @param item the item to be crafted
     * @param resources the resources required to craft the item
     */
    public void craftItem(InventoryItem item, Map<String, Integer> resources) {
        backpackHandler.craftItem(item, resources);
    }

    private void reset() {
        equipmentHandler.reset();
        backpackHandler.reset();
        Arrays.fill(quickUseSlots, null);
    }

    public List<InventoryItem> getBackpack() {
        return backpackHandler.getBackpack();
    }

    public InventoryItem[] getEquipped() {
        return equipmentHandler.getEquipped();
    }

    public String[] getQuickUseSlots() {
        return quickUseSlots;
    }

    /**
     * Fills the inventory with items.
     * Used to load the inventory from a save file.
     *
     * @param savedItems the list of saved items in string format
     */
    public void fillItems(List<String> savedItems) {
        reset();
        if (savedItems == null) return;

        for (String itemString : savedItems) {
            try {
                String[] parts = itemString.split(",");
                if (parts.length != 3) continue;
                String itemId = parts[0];
                int amount = Integer.parseInt(parts[1]);
                boolean isEquipped = parts[2].equals("1");
                InventoryItem item = new InventoryItem(itemId, amount);
                addItemToBackpack(item);
                if (isEquipped) {
                    int lastItemIndex = getBackpack().size() - 1;
                    equipItem(lastItemIndex);
                }
            } catch (Exception e) {
                Logger.getInstance().notify("Reading items from save file failed!", Message.ERROR);
            }
        }
    }

    public void completeQuestFill(Map<String, Integer> itemRewards) {
        for (Map.Entry<String, Integer> entry : itemRewards.entrySet()) {
            InventoryItem item = new InventoryItem(entry.getKey(), entry.getValue());
            addItemToBackpack(item);
        }
    }

    /**
     * Checks if any quick-use slots reference items that no longer exist in the backpack and clears them.
     */
    private void checkQuickSlots() {
        for (int i = 0; i < quickUseSlots.length; i++) {
            String itemId = quickUseSlots[i];
            if (itemId != null) {
                boolean itemExists = backpackHandler.getBackpack().stream().anyMatch(item -> item.getItemId().equals(itemId));
                if (!itemExists) quickUseSlots[i] = null;
            }
        }
    }

}
