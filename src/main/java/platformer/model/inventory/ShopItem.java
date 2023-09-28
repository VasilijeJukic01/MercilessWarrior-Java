package platformer.model.inventory;

import java.awt.image.BufferedImage;

public class ShopItem {

    private final ItemType itemType;
    private final BufferedImage model;
    private int amount;
    private final int cost;

    public ShopItem(ItemType itemType, BufferedImage model, int amount, int cost) {
        this.itemType = itemType;
        this.model = model;
        this.amount = amount;
        this.cost = cost;
    }

    public void update(int amount) {
        if (this.amount - amount >= 0) this.amount -= amount;
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
