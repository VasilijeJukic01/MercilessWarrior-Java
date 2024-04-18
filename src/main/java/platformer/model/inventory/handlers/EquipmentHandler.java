package platformer.model.inventory.handlers;

import platformer.model.inventory.InventoryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EquipmentHandler {

    private final InventoryItem[] equipped = new InventoryItem[6];
    private final Map<String, Integer> equipment;

    private final BonusHandler bonusHandler;

    public EquipmentHandler() {
        this.equipment = new HashMap<>();
        this.bonusHandler = new BonusHandler();
        initEquipment();
    }

    private void initEquipment() {
        equipment.put("Helmet", 0);
        equipment.put("Armor", 2);
        equipment.put("Bracelets", 4);
        equipment.put("Trousers", 1);
        equipment.put("Ring", 3);
        equipment.put("Charm", 3);
        equipment.put("Boots", 5);
    }

    public void equipItem(BackpackHandler backpackHandler, int index) {
        List<InventoryItem> backpack = backpackHandler.getBackpack();
        if (index >= backpack.size()) return;
        if (!backpack.get(index).getItemType().canEquip()) return;
        addToEquipment(backpack.get(index), equipment);
        bonusHandler.applyBonus(backpack.get(index).getItemType());
        backpackHandler.dropItem(index);
    }

    public void unequipItem(int index, BackpackHandler backpack) {
        if (index >= equipped.length) return;
        if (equipped[index] == null) return;
        bonusHandler.removeBonus(equipped[index].getItemType());
        equipped[index].addAmount(1);
        backpack.addItemAmountToBackpack(equipped[index], 1);
        equipped[index] = null;
    }

    private void addToEquipment(InventoryItem item, Map<String, Integer> equipment) {
        String name = item.getItemType().getName();
        equipment.keySet().stream()
                .filter(name::contains)
                .findFirst()
                .map(equipment::get).ifPresent(index -> equipped[index] = item);
    }

    public void reset() {
        for (int i = 0; i < equipped.length; i++) {
            if (equipped[i] != null) {
                bonusHandler.removeBonus(equipped[i].getItemType());
                equipped[i] = null;
            }
        }
    }

    public InventoryItem[] getEquipped() {
        return equipped;
    }

}
