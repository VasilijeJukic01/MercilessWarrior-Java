package platformer.model.inventory;

import platformer.ui.ItemType;

import java.awt.image.BufferedImage;

public class InventoryItem {

    private final ItemType itemType;
    private final BufferedImage model;
    private int amount;
    private final int sellValue;
    private final String description;

    public InventoryItem(ItemType itemType, BufferedImage model, int amount, int sellValue, String description) {
        this.itemType = itemType;
        this.model = model;
        this.amount = amount;
        this.sellValue = sellValue;
        this.description = description;
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

    public int getSellValue() {
        return sellValue;
    }

    public String getDescription() {
        return description;
    }

}
