package platformer.model.inventory.item;

import platformer.model.inventory.database.ItemDatabase;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;

/**
 * Represents an item instance within the player's inventory.
 * It holds the unique ID of the item and the quantity.
 * All static data (name, description, etc.) is fetched from ItemDatabase.
 */
public class InventoryItem {

    private final String itemId;
    private int amount;
    private transient BufferedImage model;

    public InventoryItem(String itemId, int amount) {
        this.itemId = itemId;
        this.amount = amount;
    }

    /**
     * @return The static data for this item from the central database.
     */
    public ItemData getData() {
        return ItemDatabase.getInstance().getItemData(this.itemId);
    }

    /**
     * Lazily loads the item's image when first requested.
     * @return The item's visual model.
     */
    public BufferedImage getModel() {
        if (model == null) {
            ItemData data = getData();
            if (data != null && data.imagePath != null) {
                this.model = ImageUtils.importImage(data.imagePath, -1, -1);
            }
        }
        return model;
    }

    public void addAmount(int value) {
        this.amount += value;
    }

    // Getters and setters
    public String getItemId() {
        return itemId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        ItemData data = getData();
        String name = (data != null) ? data.name : "UNKNOWN";
        return name + "," + amount;
    }
}
