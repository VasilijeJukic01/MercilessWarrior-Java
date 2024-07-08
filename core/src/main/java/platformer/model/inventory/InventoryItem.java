package platformer.model.inventory;

import java.awt.image.BufferedImage;

public class InventoryItem extends AbstractItem {

    public InventoryItem(ItemType itemType, BufferedImage model, int amount) {
        super(itemType, model, amount);
    }

}
