package platformer.model.inventory;

import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.handlers.BackpackHandler;
import platformer.model.inventory.handlers.EquipmentHandler;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;
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
    public void craftItem(InventoryItem item, Map<ItemType, Integer> resources) {
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
     * @param values the values used to fill the inventory
     */
    public void fillItems(List<String> values) {
        reset();
        for (String value : values) {
            String[] indexes = value.split(",");
            ItemType type = findItemTypeByName(indexes[0]);
            if (type == null) continue;
            BufferedImage img = Utils.getInstance().importImage(type.getImg(), -1, -1);
            InventoryItem item = new InventoryItem(type, img, Integer.parseInt(indexes[1]));
            getBackpack().add(item);
            if (indexes[2].equals("1")) {
                backpackHandler.addItemAmountToBackpack(item, 1);
                equipItem(getBackpack().size()-1);
            }
        }
    }

    public void completeQuestFill(Map<ItemType, Integer> itemRewards) {
        for (Map.Entry<ItemType, Integer> entry : itemRewards.entrySet()) {
            ItemType type = entry.getKey();
            BufferedImage img = Utils.getInstance().importImage(type.getImg(), -1, -1);
            InventoryItem item = new InventoryItem(type, img, entry.getValue());
            backpackHandler.addItemToBackpack(item);
        }

    }

    private ItemType findItemTypeByName(String name) {
        ItemType[] itemTypes = ItemType.values();
        Optional<ItemType> optional = Arrays.stream(itemTypes)
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst();
        return optional.orElse(null);
    }

}
