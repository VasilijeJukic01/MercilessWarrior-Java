package platformer.model.inventory;

import java.util.ArrayList;
import java.util.List;

public class Inventory {

    private final List<InventoryItem> backpack = new ArrayList<>();
    private final InventoryItem[] equipped = new InventoryItem[6];
    private final ItemsManager itemsManager;

    public Inventory() {
        this.itemsManager = new ItemsManager();
    }

    public void useItem(int index) {
        // TODO: Use item
    }

    public void equipItem(int index) {
        if (index >= backpack.size()) return;
        if (!backpack.get(index).getItemType().canEquip()) return;
        addToEquipment(backpack.get(index));
        dropItem(index);

    }

    public void dropItem(int index) {
        if (index >= backpack.size()) return;
        backpack.get(index).addAmount(-1);
        if (backpack.get(index).getAmount() <= 0)
            backpack.remove(index);
    }

    private void addToEquipment(InventoryItem item) {
        if (item.getItemType().getName().contains("Helmet")) equipped[0] = item;
        if (item.getItemType().getName().contains("Armor")) equipped[1] = item;
        else if (item.getItemType().getName().contains("Gloves")) equipped[2] = item;
        else if (item.getItemType().getName().contains("Trousers")) equipped[3] = item;
        else if (item.getItemType().getName().contains("Amulet")) equipped[4] = item;
        else if (item.getItemType().getName().contains("Boots")) equipped[5] = item;
    }

    public List<InventoryItem> getBackpack() {
        return backpack;
    }

    public InventoryItem[] getEquipped() {
        return equipped;
    }
}
