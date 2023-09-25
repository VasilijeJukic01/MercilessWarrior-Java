package platformer.ui;

import platformer.model.inventory.ItemType;

import java.awt.image.BufferedImage;

public class ShopItem {

    private final ItemType itemType;
    private final BufferedImage itemImage;
    private final int slot;
    private int amount;
    private final int cost;

    public ShopItem(ItemType itemType, BufferedImage itemImage, int slot, int amount, int cost) {
        this.itemType = itemType;
        this.itemImage = itemImage;
        this.slot = slot;
        this.amount = amount;
        this.cost = cost;
    }

    public void update(int amount) {
        if (this.amount - amount >= 0) this.amount -= amount;
    }

    public BufferedImage getItemImage() {
        return itemImage;
    }

    public int getSlot() {
        return slot;
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
