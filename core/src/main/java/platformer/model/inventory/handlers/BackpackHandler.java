package platformer.model.inventory.handlers;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.Framework;
import platformer.model.entities.player.Player;
import platformer.model.inventory.InventoryItem;
import platformer.model.inventory.ItemData;

import java.util.*;
import java.util.stream.Collectors;

import static platformer.constants.Constants.HEAL_POTION_VAL;
import static platformer.constants.Constants.STAMINA_POTION_VAL;

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

    public void useItem(int index, Player player) {
        if (index >= backpack.size()) return;
        InventoryItem item = backpack.get(index);

        switch (item.getItemId()) {
            case "HEALTH_POTION":
                player.changeHealth(HEAL_POTION_VAL);
                consumeItem(index);
                break;
            case "STAMINA_POTION":
                player.changeStamina(STAMINA_POTION_VAL);
                consumeItem(index);
                break;
            default: break;
        }
    }

    private void consumeItem(int index) {
        InventoryItem item = backpack.get(index);
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) backpack.remove(index);
        refreshAccountItems();
    }

    public void dropItem(int index) {
        dropItem(index, false);
    }

    public void dropItem(int index, boolean isEquipAction) {
        if (index >= backpack.size()) return;
        if (isEquipAction) backpack.remove(index);
        else {
            backpack.get(index).setAmount(backpack.get(index).getAmount() - 1);
            if (backpack.get(index).getAmount() <= 0) backpack.remove(index);
        }
        refreshAccountItems();
    }

    public void addItemToBackpack(InventoryItem itemToAdd) {
        ItemData data = itemToAdd.getData();
        if (data == null) return;
        if (data.stackable) {
            for (InventoryItem existingItem : backpack) {
                if (existingItem.getItemId().equals(itemToAdd.getItemId())) {
                    existingItem.addAmount(itemToAdd.getAmount());
                    refreshAccountItems();
                    return;
                }
            }
        }
        backpack.add(itemToAdd);
        refreshAccountItems();
    }

    public void craftItem(InventoryItem item, Map<String, Integer> resources) {
        if (!hasEnoughResources(resources)) return;
        Audio.getInstance().getAudioPlayer().playSound(Sound.CRAFTING);
        useResources(resources);
        addItemToBackpack(item);
    }

    private boolean hasEnoughResources(Map<String, Integer> resources) {
        Map<String, InventoryItem> inventoryMap = getInventoryMap();
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
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
    private void useResources(Map<String, Integer> resources) {
        Map<String, InventoryItem> inventoryMap = getInventoryMap();
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            InventoryItem inventoryItem = inventoryMap.get(entry.getKey());
            inventoryItem.setAmount(inventoryItem.getAmount() - entry.getValue());
            if (inventoryItem.getAmount() <= 0) backpack.remove(inventoryItem);
        }
    }

    private Map<String, InventoryItem> getInventoryMap() {
        Map<String, InventoryItem> inventoryMap = new HashMap<>();
        backpack.forEach(inventoryItem -> inventoryMap.put(inventoryItem.getItemId(), inventoryItem));
        return inventoryMap;
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
                .map(item -> item.getItemId() + "," + item.getAmount() + ",0")
                .collect(Collectors.toList());
        values.addAll(Arrays.stream(equipmentHandler.getEquipped())
                .filter(Objects::nonNull)
                .map(item -> item.getItemId() + "," + item.getAmount() + ",1")
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
