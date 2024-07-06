package platformer.model.inventory;

import java.awt.image.BufferedImage;

public class ShopItem extends AbstractItem {

    public ShopItem(ItemType itemType, BufferedImage model, int amount, int cost) {
        super(itemType, model, amount, cost);
    }

}
