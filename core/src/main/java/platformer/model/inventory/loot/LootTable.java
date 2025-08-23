package platformer.model.inventory.loot;

import java.util.List;
import java.util.Map;

/**
 * Represents a loot table containing various loot items with associated weights.
 */
public class LootTable {

    private Map<String, Integer> numRolls;
    private List<LootItem> items;
    private transient int totalItemWeight = -1;
    private transient int totalRollsWeight = -1;

    public int getTotalItemWeight() {
        if (totalItemWeight == -1 && items != null) {
            totalItemWeight = items.stream().mapToInt(LootItem::getWeight).sum();
        }
        return totalItemWeight;
    }

    public int getTotalRollsWeight() {
        if (totalRollsWeight == -1 && numRolls != null) {
            totalRollsWeight = numRolls.values().stream().mapToInt(Integer::intValue).sum();
        }
        return totalRollsWeight;
    }

    public List<LootItem> getItems() {
        return items;
    }

    public void setItems(List<LootItem> items) {
        this.items = items;
    }

    public Map<String, Integer> getNumRolls() {
        return numRolls;
    }

    public void setNumRolls(Map<String, Integer> numRolls) {
        this.numRolls = numRolls;
    }
}