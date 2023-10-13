package platformer.model.inventory;

import platformer.core.Framework;
import platformer.model.gameObjects.objects.Loot;
import platformer.utils.Utils;

import java.awt.image.BufferedImage;
import java.util.*;

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
        refreshAccountItems();
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
                refreshAccountItems();
                return;
            }
        }
        backpack.add(item);
        refreshAccountItems();
    }

    private void addItemAmountToBackpack(InventoryItem item, int amount) {
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

    private void reset() {
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

    public void fillItems(List<String> values) {
        reset();
        for (String value : values) {
            String[] indexes = value.split(",");
            ItemType type = findItemTypeByName(indexes[0]);
            if (type == null) continue;
            BufferedImage img = Utils.getInstance().importImage(type.getImg(), -1, -1);
            InventoryItem item = new InventoryItem(type, img, Integer.parseInt(indexes[1]));
            backpack.add(item);
            if (indexes[2].equals("1")) {
                addItemAmountToBackpack(item, 1);
                equipItem(backpack.size()-1);
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

    private List<String> getFormattedItems() {
        List<String> values = new ArrayList<>();
        backpack.forEach(item -> values.add(item + ",0"));
        Arrays.stream(equipped)
                .filter(Objects::nonNull)
                .forEach(item -> values.add(item + ",1"));
        return values;
    }

    private void refreshAccountItems() {
        Framework.getInstance().getAccount().setItems(getFormattedItems());
    }

}
