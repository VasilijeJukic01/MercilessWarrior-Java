package platformer.model.inventory;

import java.awt.image.BufferedImage;

/**
 * Class that represents an item in the inventory or shop.
 */
public abstract class AbstractItem {

    private final ItemType itemType;
    private final BufferedImage model;
    private int amount;
    private int cost;

    public AbstractItem(ItemType itemType, BufferedImage model, int amount, int cost) {
        this.itemType = itemType;
        this.model = model;
        this.amount = amount;
        this.cost = cost;
    }

    public AbstractItem(ItemType itemType, BufferedImage model, int amount) {
        this.itemType = itemType;
        this.model = model;
        this.amount = amount;
    }

    /**
     * Adds a specified value to the item's amount.
     * If the value is negative and the resulting amount is non-negative, the amount is decreased.
     * Otherwise, the amount is increased.
     *
     * @param value the value to be added
     */
    public void addAmount(int value) {
        if (value < 0 && amount - value >= 0) {
             amount -= value;
        }
        else amount += value;
    }

    /**
     * Removes a specified value from the item's amount.
     * If the resulting amount is non-negative, the amount is increased.
     *
     * @param value the value to be removed
     */
    public void removeAmount(int value) {
        if (amount + value >= 0) {
            amount += value;
        }
    }

    public BufferedImage getModel() {
        return model;
    }

    public int getAmount() {
        return amount;
    }

    public int getCost() {
        return cost;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return itemType.getName() + "," + amount;
    }
}
