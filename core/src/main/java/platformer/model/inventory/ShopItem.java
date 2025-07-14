package platformer.model.inventory;

import platformer.utils.Utils;

import java.awt.image.BufferedImage;

/**
 * Represents an item available for purchase in a shop.
 * It holds the item's ID, the stock available, and the purchase cost.
 * All other static data is fetched from ItemDatabase.
 */
public class ShopItem {

    private final String itemId;
    private int stock;
    private int cost;

    private transient BufferedImage model;

    public ShopItem(String itemId, int stock, int cost) {
        this.itemId = itemId;
        this.stock = stock;
        this.cost = cost;
    }

    public ItemData getData() {
        return ItemDatabase.getInstance().getItemData(this.itemId);
    }

    public BufferedImage getModel() {
        if (model == null) {
            ItemData data = getData();
            if (data != null && data.imagePath != null) {
                this.model = Utils.getInstance().importImage(data.imagePath, -1, -1);
            }
        }
        return model;
    }

    public void addStock(int value) {
        this.stock += value;
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }
}
