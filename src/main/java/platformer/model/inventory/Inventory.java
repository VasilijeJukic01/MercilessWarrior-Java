package platformer.model.inventory;

import platformer.model.gameObjects.objects.Loot;
import platformer.model.inventory.handlers.BackpackHandler;
import platformer.model.inventory.handlers.EquipmentHandler;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;
import java.util.*;

public class Inventory {

    private final EquipmentHandler equipmentHandler;
    private final BackpackHandler backpackHandler;

    public Inventory() {
        this.equipmentHandler = new EquipmentHandler();
        this.backpackHandler = new BackpackHandler(equipmentHandler);
    }

    public void useItem(int index) {
        backpackHandler.useItem(index);
    }

    public void equipItem(int index) {
        equipmentHandler.equipItem(backpackHandler, index);
    }

    public void dropItem(int index) {
        backpackHandler.dropItem(index);
    }

    public void unequipItem(int index) {
        equipmentHandler.unequipItem(index, backpackHandler);
    }

    public void addItemToBackpack(InventoryItem item) {
        backpackHandler.addItemToBackpack(item);
    }

    public void addAllItemsFromLoot(Loot loot) {
        loot.getItems().forEach(this::addItemToBackpack);
    }

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

    private ItemType findItemTypeByName(String name) {
        ItemType[] itemTypes = ItemType.values();
        Optional<ItemType> optional = Arrays.stream(itemTypes)
                .filter(item -> item.getName().equalsIgnoreCase(name))
                .findFirst();
        return optional.orElse(null);
    }

}
