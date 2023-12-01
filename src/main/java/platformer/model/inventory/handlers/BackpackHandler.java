package platformer.model.inventory.handlers;

import platformer.core.Framework;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemType;

import java.util.*;
import java.util.stream.Collectors;

public class BackpackHandler {

    private final List<InventoryItem> backpack;
    private final EquipmentHandler equipmentHandler;

    public BackpackHandler(EquipmentHandler equipmentHandler) {
        this.backpack = new ArrayList<>();
        this.equipmentHandler = equipmentHandler;
    }

    public void useItem(int index) {
        // TODO: Implement use item logic
    }

    public void dropItem(int index) {
        if (index >= backpack.size()) return;
        backpack.get(index).removeAmount(-1);
        if (backpack.get(index).getAmount() <= 0)
            backpack.remove(index);
        refreshAccountItems();
    }

    public void addItemToBackpack(InventoryItem item) {
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem.getItemType() == item.getItemType()) {
                inventoryItem.addAmount(item.getAmount());
                refreshAccountItems();
                return;
            }
        }
        backpack.add(item);
        refreshAccountItems();
    }

    public void craftItem(InventoryItem item, Map<ItemType, Integer> resources) {
        if (!hasEnoughResources(resources)) return;
        useResources(resources);
        addItemToBackpack(item);
    }

    private boolean hasEnoughResources(Map<ItemType, Integer> resources) {
        Map<ItemType, InventoryItem> inventoryMap = getInventoryMap();
        for (Map.Entry<ItemType, Integer> entry : resources.entrySet()) {
            InventoryItem inventoryItem = inventoryMap.get(entry.getKey());
            if (inventoryItem == null || inventoryItem.getAmount() < entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    private void useResources(Map<ItemType, Integer> resources) {
        Map<ItemType, InventoryItem> inventoryMap = getInventoryMap();
        for (Map.Entry<ItemType, Integer> entry : resources.entrySet()) {
            InventoryItem inventoryItem = inventoryMap.get(entry.getKey());
            inventoryItem.removeAmount(-entry.getValue());
            if (inventoryItem.getAmount() <= 0) backpack.remove(inventoryItem);
        }
    }

    private Map<ItemType, InventoryItem> getInventoryMap() {
        Map<ItemType, InventoryItem> inventoryMap = new HashMap<>();
        backpack.forEach(inventoryItem -> inventoryMap.put(inventoryItem.getItemType(), inventoryItem));
        return inventoryMap;
    }

    public void addItemAmountToBackpack(InventoryItem item, int amount) {
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem.getItemType() == item.getItemType()) {
                inventoryItem.addAmount(amount);
                refreshAccountItems();
                return;
            }
        }
        backpack.add(item);
        refreshAccountItems();
    }

    private List<String> getFormattedItems() {
        List<String> values = backpack.stream()
                .map(item -> item + ",0")
                .collect(Collectors.toList());
        values.addAll(Arrays.stream(equipmentHandler.getEquipped())
                .filter(Objects::nonNull)
                .map(item -> item + ",1")
                .collect(Collectors.toList()));
        return values;
    }

    private void refreshAccountItems() {
        Framework.getInstance().getAccount().setItems(getFormattedItems());
    }

    public void reset() {
        backpack.clear();
    }

    public List<InventoryItem> getBackpack() {
        return backpack;
    }

}
