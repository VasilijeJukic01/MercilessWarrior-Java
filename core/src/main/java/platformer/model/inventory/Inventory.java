package platformer.model.inventory;

import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;
import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.handlers.BackpackHandler;
import platformer.model.inventory.handlers.EquipmentHandler;

import java.util.List;
import java.util.Map;

/**
 * Class that represents the player's inventory.
 * It provides methods to manage items in the inventory.
 */
public class Inventory {

    private final EquipmentHandler equipmentHandler;
    private final BackpackHandler backpackHandler;

    public Inventory() {
        this.equipmentHandler = new EquipmentHandler();
        this.backpackHandler = new BackpackHandler(equipmentHandler);
    }

    /**
     * Uses an item from the backpack.
     *
     * @param index the index of the item in the backpack
     */
    public void useItem(int index) {
        backpackHandler.useItem(index);
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

    /**
     * Unequips an item from the equipment.
     *
     * @param index the index of the item in the equipment
     */
    public void unequipItem(int index) {
        equipmentHandler.unequipItem(index, backpackHandler);
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
    }

    public List<InventoryItem> getBackpack() {
        return backpackHandler.getBackpack();
    }

    public InventoryItem[] getEquipped() {
        return equipmentHandler.getEquipped();
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

}
