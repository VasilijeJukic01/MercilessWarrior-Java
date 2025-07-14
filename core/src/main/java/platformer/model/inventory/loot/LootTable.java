package platformer.model.inventory.loot;

import java.util.List;

/**
 * Represents a loot table containing various loot items with associated weights.
 */
public class LootTable {

    private List<LootItem> items;
    private transient int totalWeight = -1;

    public int getTotalWeight() {
        if (totalWeight == -1) totalWeight = items.stream().mapToInt(LootItem::getWeight).sum();
        return totalWeight;
    }

    public List<LootItem> getItems() {
        return items;
    }

    public void setItems(List<LootItem> items) {
        this.items = items;
    }
}