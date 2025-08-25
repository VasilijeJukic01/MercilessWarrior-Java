package platformer.model.inventory.handlers;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.core.Framework;
import platformer.model.entities.player.Player;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;

import java.util.*;

import static platformer.constants.Constants.*;

/**
 * Handles the operations related to the backpack in the inventory system.
 */
public class BackpackHandler {

    private final List<InventoryItem> backpack;
    private final EquipmentHandler equipmentHandler;

    public BackpackHandler(EquipmentHandler equipmentHandler) {
        this.backpack = new ArrayList<>(Collections.nCopies(BACKPACK_CAPACITY, null));
        this.equipmentHandler = equipmentHandler;
    }

    public void useItem(int index, Player player) {
        if (index < 0 || index >= BACKPACK_CAPACITY || backpack.get(index) == null) return;
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
        if (item.getAmount() <= 0) backpack.set(index, null);
        refreshAccountItems();
    }

    public void dropItem(int index) {
        if (index < 0 || index >= BACKPACK_CAPACITY || backpack.get(index) == null) return;
        InventoryItem item = backpack.get(index);
        item.setAmount(item.getAmount() - 1);
        if (item.getAmount() <= 0) {
            backpack.set(index, null);
        }
        refreshAccountItems();
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
                if (existingItem != null && existingItem.getItemId().equals(itemToAdd.getItemId())) {
                    existingItem.addAmount(itemToAdd.getAmount());
                    refreshAccountItems();
                    return;
                }
            }
        }

        for (int i = 0; i < BACKPACK_CAPACITY; i++) {
            if (backpack.get(i) == null) {
                backpack.set(i, itemToAdd);
                refreshAccountItems();
                return;
            }
        }
    }

    public void swapItems(int index1, int index2) {
        if (index1 >= 0 && index1 < BACKPACK_CAPACITY && index2 >= 0 && index2 < BACKPACK_CAPACITY) {
            Collections.swap(backpack, index1, index2);
            refreshAccountItems();
        }
    }

    /**
     * Inserts an item at a specific index if valid, otherwise adds it to the end of the backpack.
     * This prevents items from being lost when dropped onto empty slots.
     *
     * @param index The target index.
     * @param item The item to add.
     */
    public void setItemAt(int index, InventoryItem item) {
        if (index >= 0 && index < BACKPACK_CAPACITY) {
            backpack.set(index, item);
            refreshAccountItems();
        }
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
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            for (int i = 0; i < backpack.size(); i++) {
                InventoryItem inventoryItem = backpack.get(i);
                if (inventoryItem != null && inventoryItem.getItemId().equals(entry.getKey())) {
                    inventoryItem.setAmount(inventoryItem.getAmount() - entry.getValue());
                    if (inventoryItem.getAmount() <= 0) {
                        backpack.set(i, null);
                    }
                    break;
                }
            }
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
        List<String> values = new ArrayList<>();
        for (int i = 0; i < backpack.size(); i++) {
            InventoryItem item = backpack.get(i);
            if (item != null) {
                values.add(item.getItemId() + "," + item.getAmount() + ",0," + i);
            }
        }
        for (int i = 0; i < equipmentHandler.getEquipped().length; i++) {
            InventoryItem item = equipmentHandler.getEquipped()[i];
            if (item != null) {
                values.add(item.getItemId() + "," + item.getAmount() + ",1," + i);
            }
        }
        return values;
    }

    public void refreshAccountItems() {
        Framework.getInstance().getAccount().setItems(getFormattedItems());
    }

    public void reset() {
        Collections.fill(backpack, null);
    }

    public List<InventoryItem> getBackpack() {
        return backpack;
    }

}
