package platformer.model.inventory;

import platformer.ui.ItemType;

import java.awt.image.BufferedImage;

public class InventoryItem {

    private final ItemType itemType;
    private final BufferedImage model;
    private int amount;

    public InventoryItem(ItemType itemType, BufferedImage model, int amount) {
        this.itemType = itemType;
        this.model = model;
        this.amount = amount;
    }

    public void addAmount(int value) {
        this.amount += value;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public BufferedImage getModel() {
        return model;
    }

    public int getAmount() {
        return amount;
    }

}
