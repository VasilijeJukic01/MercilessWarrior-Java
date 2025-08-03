package platformer.event.events;

import platformer.event.Event;
import platformer.model.inventory.ShopItem;

/**
 * An event published when a player successfully buys an item from a shop.
 *
 * @param purchasedItem The ShopItem that was bought.
 * @param quantity The quantity purchased.
 */
public record ItemPurchasedEvent(ShopItem purchasedItem, int quantity) implements Event {}