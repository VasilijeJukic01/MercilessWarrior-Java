package platformer.model.inventory;

import platformer.model.gameObjects.objects.Loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Inventory {

    private final List<InventoryItem> backpack = new ArrayList<>();
    private final InventoryItem[] equipped = new InventoryItem[6];

    public Inventory() {

    }

    public void useItem(int index) {
        // TODO: Use item
    }

    public void equipItem(int index) {
        if (index >= backpack.size()) return;
        if (!backpack.get(index).getItemType().canEquip()) return;
        addToEquipment(backpack.get(index));
        applyBonus(backpack.get(index).getItemType());
        dropItem(index);
    }

    public void dropItem(int index) {
        if (index >= backpack.size()) return;
        backpack.get(index).removeAmount(-1);
        if (backpack.get(index).getAmount() <= 0)
            backpack.remove(index);
    }

    public void unequipItem(int index) {
        if (index >= equipped.length) return;
        if (equipped[index] == null) return;
        removeBonus(equipped[index].getItemType());
        equipped[index].addAmount(1);
        addItemAmountToBackpack(equipped[index], 1);
        equipped[index] = null;
    }

    private void addToEquipment(InventoryItem item) {
        if (item.getItemType().getName().contains("Helmet")) equipped[0] = item;
        if (item.getItemType().getName().contains("Armor")) equipped[2] = item;
        else if (item.getItemType().getName().contains("Bracelets")) equipped[4] = item;
        else if (item.getItemType().getName().contains("Trousers")) equipped[1] = item;
        else if (item.getItemType().getName().contains("Ring")) equipped[3] = item;
        else if (item.getItemType().getName().contains("Boots")) equipped[5] = item;
    }

    private void applyBonus(ItemType itemType) {
        if (itemType == ItemType.HELMET_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.HELMET_WARRIOR);
        else if (itemType == ItemType.ARMOR_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.ARMOR_WARRIOR);
        else if (itemType == ItemType.BRACELETS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.BRACELETS_WARRIOR);
        else if (itemType == ItemType.TROUSERS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.TROUSERS_WARRIOR);
        else if (itemType == ItemType.BOOTS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.BOOTS_WARRIOR);
        else if (itemType == ItemType.ARMOR_GUARDIAN) InventoryBonus.getInstance().applyBonus(ItemBonus.ARMOR_GUARDIAN);
        else if (itemType == ItemType.RING_AMETHYST) InventoryBonus.getInstance().applyBonus(ItemBonus.RING_AMETHYST);
    }

    private void removeBonus(ItemType itemType) {
        if (itemType == ItemType.HELMET_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.HELMET_WARRIOR);
        else if (itemType == ItemType.ARMOR_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.ARMOR_WARRIOR);
        else if (itemType == ItemType.BRACELETS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.BRACELETS_WARRIOR);
        else if (itemType == ItemType.TROUSERS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.TROUSERS_WARRIOR);
        else if (itemType == ItemType.BOOTS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.BOOTS_WARRIOR);
        else if (itemType == ItemType.ARMOR_GUARDIAN) InventoryBonus.getInstance().removeBonus(ItemBonus.ARMOR_GUARDIAN);
        else if (itemType == ItemType.RING_AMETHYST) InventoryBonus.getInstance().removeBonus(ItemBonus.RING_AMETHYST);
    }

    public void addItemToBackpack(InventoryItem item) {
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem.getItemType() == item.getItemType()) {
                inventoryItem.addAmount(item.getAmount());
                return;
            }
        }
        backpack.add(item);
    }

    private void addItemAmountToBackpack(InventoryItem item, int amount) {
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem.getItemType() == item.getItemType()) {
                inventoryItem.addAmount(amount);
                return;
            }
        }
        backpack.add(item);
    }

    public void addAllItemsFromLoot(Loot loot) {
        loot.getItems().forEach(this::addItemToBackpack);
    }

    public void craftItem(InventoryItem item, Map<ItemType, Integer> resources) {
        for (Map.Entry<ItemType, Integer> entry : resources.entrySet()) {
            boolean found = false;
            for (InventoryItem inventoryItem : backpack) {
                if (inventoryItem.getItemType() == entry.getKey()) {
                    if (inventoryItem.getAmount() < entry.getValue()) return;
                    inventoryItem.addAmount(-entry.getValue());
                    if (inventoryItem.getAmount() <= 0) backpack.remove(inventoryItem);
                    found = true;
                    break;
                }
            }
            if (!found) return;
        }
        addItemToBackpack(item);
    }

    public void reset() {
        this.backpack.clear();
        for (int i = 0; i < equipped.length; i++) {
            if (equipped[i] != null) {
                removeBonus(equipped[i].getItemType());
                equipped[i] = null;
            }
        }
    }

    public List<InventoryItem> getBackpack() {
        return backpack;
    }

    public InventoryItem[] getEquipped() {
        return equipped;
    }
}
