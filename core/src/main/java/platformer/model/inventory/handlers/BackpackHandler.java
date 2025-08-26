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

    /**
     * Merges an item from a source index into a target index.
     * The source item is removed after its amount is added to the target.
     *
     * @param fromIndex The index of the item being dragged.
     * @param toIndex   The index of the item to merge into.
     */
    public void mergeStacks(int fromIndex, int toIndex) {
        if (fromIndex < 0 || fromIndex >= backpack.size() || toIndex < 0 || toIndex >= backpack.size()) return;
        InventoryItem sourceItem = backpack.get(fromIndex);
        InventoryItem targetItem = backpack.get(toIndex);
        if (sourceItem != null && targetItem != null && sourceItem.getItemId().equals(targetItem.getItemId())) {
            targetItem.addAmount(sourceItem.getAmount());
            backpack.set(fromIndex, null);
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
        Map<String, Integer> inventoryMap = getInventoryMap();
        for (Map.Entry<String, Integer> entry : resources.entrySet()) {
            int playerAmount = inventoryMap.getOrDefault(entry.getKey(), 0);
            if (playerAmount < entry.getValue()) return false;
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
        for (Map.Entry<String, Integer> requiredResource : resources.entrySet()) {
            int amountToConsume = requiredResource.getValue();
            for (int i = 0; i < backpack.size(); i++) {
                InventoryItem currentItem = backpack.get(i);
                if (currentItem != null && currentItem.getItemId().equals(requiredResource.getKey())) {
                    int amountInStack = currentItem.getAmount();
                    if (amountInStack >= amountToConsume) {
                        currentItem.setAmount(amountInStack - amountToConsume);
                        amountToConsume = 0;
                    }
                    else {
                        amountToConsume -= amountInStack;
                        currentItem.setAmount(0);
                    }
                    if (currentItem.getAmount() <= 0) backpack.set(i, null);
                    if (amountToConsume == 0) break;
                }
            }
        }
    }

    /**
     * Splits a stack of items at the given index.
     * It reduces the original stack's amount and returns a new InventoryItem with the split amount.
     *
     * @param index The index of the stack to split in the backpack.
     * @return The new InventoryItem representing the split-off stack, or null if the item cannot be split.
     */
    public InventoryItem splitStack(int index) {
        if (index < 0 || index >= backpack.size()) return null;

        InventoryItem originalItem = backpack.get(index);
        if (originalItem == null || !originalItem.getData().stackable || originalItem.getAmount() <= 1) return null;

        int originalAmount = originalItem.getAmount();
        int newStackAmount = originalAmount / 2;
        int remainingAmount = originalAmount - newStackAmount;
        originalItem.setAmount(remainingAmount);

        return new InventoryItem(originalItem.getItemId(), newStackAmount);
    }

    /**
     * Reverts a split by adding the amount of a split-off item back to its original stack.
     *
     * @param originalIndex The index of the stack from which the item was split.
     * @param splitItem     The temporary item that was being dragged.
     */
    public void revertSplit(int originalIndex, InventoryItem splitItem) {
        if (originalIndex < 0 || originalIndex >= backpack.size() || splitItem == null) return;

        InventoryItem originalItem = backpack.get(originalIndex);
        if (originalItem != null && originalItem.getItemId().equals(splitItem.getItemId())) {
            originalItem.addAmount(splitItem.getAmount());
        }
        else addItemToBackpack(splitItem);
        refreshAccountItems();
    }

    private Map<String, Integer> getInventoryMap() {
        Map<String, Integer> inventoryMap = new HashMap<>();
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem != null) {
                inventoryMap.merge(inventoryItem.getItemId(), inventoryItem.getAmount(), Integer::sum);
            }
        }
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
