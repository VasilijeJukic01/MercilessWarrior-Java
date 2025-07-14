package platformer.model.inventory.loot;

/**
 * This class is used in loot tables to define what items can be dropped and their respective probabilities.
 */
public class LootItem {

    private String itemId;
    private int quantity;
    private int weight;

    public String getItemId() {
        return itemId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getWeight() {
        return weight;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
