package platformer.model.inventory;

import platformer.model.gameObjects.objects.Loot;

import java.util.ArrayList;
import java.util.List;

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
        backpack.get(index).addAmount(-1);
        if (backpack.get(index).getAmount() <= 0)
            backpack.remove(index);
    }

    public void unequipItem(int index) {
        if (index >= equipped.length) return;
        if (equipped[index] == null) return;
        removeBonus(equipped[index].getItemType());
        equipped[index].addAmount(1);
        backpack.add(equipped[index]);
        equipped[index] = null;
    }

    private void addToEquipment(InventoryItem item) {
        if (item.getItemType().getName().contains("Helmet")) equipped[0] = item;
        if (item.getItemType().getName().contains("Armor")) equipped[2] = item;
        else if (item.getItemType().getName().contains("Bracelets")) equipped[4] = item;
        else if (item.getItemType().getName().contains("Trousers")) equipped[1] = item;
        else if (item.getItemType().getName().contains("Amulet")) equipped[3] = item;
        else if (item.getItemType().getName().contains("Boots")) equipped[5] = item;
    }

    private void applyBonus(ItemType itemType) {
        if (itemType == ItemType.HELMET_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.HELMET_WARRIOR);
        else if (itemType == ItemType.ARMOR_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.ARMOR_WARRIOR);
        else if (itemType == ItemType.BRACELETS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.BRACELETS_WARRIOR);
        else if (itemType == ItemType.TROUSERS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.TROUSERS_WARRIOR);
        else if (itemType == ItemType.BOOTS_WARRIOR) InventoryBonus.getInstance().applyBonus(ItemBonus.BOOTS_WARRIOR);
    }

    private void removeBonus(ItemType itemType) {
        if (itemType == ItemType.HELMET_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.HELMET_WARRIOR);
        else if (itemType == ItemType.ARMOR_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.ARMOR_WARRIOR);
        else if (itemType == ItemType.BRACELETS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.BRACELETS_WARRIOR);
        else if (itemType == ItemType.TROUSERS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.TROUSERS_WARRIOR);
        else if (itemType == ItemType.BOOTS_WARRIOR) InventoryBonus.getInstance().removeBonus(ItemBonus.BOOTS_WARRIOR);
    }

    public void addItemFromLoot(InventoryItem item) {
        for (InventoryItem inventoryItem : backpack) {
            if (inventoryItem.getItemType() == item.getItemType()) {
                inventoryItem.addAmount(item.getAmount());
                return;
            }
        }
        backpack.add(item);
    }

    public void addAllItemsFromLoot(Loot loot) {
        loot.getItems().forEach(this::addItemFromLoot);
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
