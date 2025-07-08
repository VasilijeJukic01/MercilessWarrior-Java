package platformer.model.inventory.handlers;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.Framework;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles the operations related to the backpack in the inventory system.
 */
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
        dropItem(index, false);
    }

    public void dropItem(int index, boolean isEquipAction) {
        if (index >= backpack.size()) return;
        if (isEquipAction) backpack.remove(index);
        else {
            backpack.get(index).removeAmount(-1);
            if (backpack.get(index).getAmount() <= 0) {
                backpack.remove(index);
            }
        }
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
        Audio.getInstance().getAudioPlayer().playSound(Sound.CRAFTING);
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

    /**
     * Uses the resources required to craft an item.
     * It iterates over the resources map, and for each entry, it retrieves the corresponding item from the inventory.
     * If the item exists and its amount is greater than or equal to the required amount, it reduces the item's amount.
     * If the item's amount drops to zero or less, it removes the item from the backpack.
     *
     * @param resources A map where the key is the ItemType of the resource and the value is the amount required.
     */
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

    /**
     * Formats the items in the backpack and equipped items for saving to the account.
     * It creates a list of strings where each string represents an item in the format "item,0" for backpack items
     * and "item,1" for equipped items.
     *
     * @return A list of strings representing the formatted items.
     */
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
