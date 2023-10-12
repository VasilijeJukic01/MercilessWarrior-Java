package platformer.model.inventory;

import java.awt.image.BufferedImage;

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

    public void addAmount(int value) {
        if (value < 0 && amount - value >= 0) {
             amount -= value;
        }
        else amount += value;
    }

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

}
