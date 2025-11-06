package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.event.EventBus;
import platformer.event.events.ItemPurchasedEvent;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.storage.StorageStrategy;
import platformer.core.Framework;
import platformer.core.Account;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.objects.Shop;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.ShopOverlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Optional;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * Controller for managing the shop view in the game.
 * Handles user input for buying and selling items, navigating through shop slots and updating the quantity slider based on user interactions.
 */
public class ShopViewController {

    private final GameState gameState;
    private final ShopOverlay shopOverlay;

    private int buySelectedSlot = 0, sellSelectedSlot = 0;
    private int buySlotNumber = 0, sellSlotNumber = 0;
    private boolean isSelling = false;
    private boolean sliderActive = false;
    private int quantityToTrade = 1;

    public ShopViewController(GameState gameState, ShopOverlay shopOverlay) {
        this.gameState = gameState;
        this.shopOverlay = shopOverlay;
    }

    public void update() {
        updateSliderVisibility();
        if (sliderActive) updateQuantityFromSlider();
    }

    // Event Handlers
    public void mousePressed(MouseEvent e) {
        if (sliderActive && isMouseInButton(e, shopOverlay.getSlider())) shopOverlay.getSlider().setMousePressed(true);
        else {
            setMousePressed(e, shopOverlay.getSmallButtons());
            setMousePressed(e, shopOverlay.getMediumButtons());
            changeSlot(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        shopOverlay.getSlider().resetMouseSet();
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(shopOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(shopOverlay.getMediumButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, shopOverlay.getSmallButtons());
        setMouseMoved(e, shopOverlay.getMediumButtons());
        shopOverlay.getSlider().setMouseOver(false);
        if (isMouseInButton(e, shopOverlay.getSlider())) shopOverlay.getSlider().setMouseOver(true);
    }

    public void mouseDragged(MouseEvent e) {
        if (shopOverlay.getSlider().isMousePressed()) shopOverlay.getSlider().updateSlider(e.getX());
    }

    public void keyPressed(KeyEvent e) {

    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_B -> buyItem();
            case KeyEvent.VK_S -> sellItem();
            case KeyEvent.VK_UP -> moveUp();
            case KeyEvent.VK_DOWN -> moveDown();
            case KeyEvent.VK_LEFT -> moveLeft();
            case KeyEvent.VK_RIGHT -> moveRight();
        }
    }

    // Actions
    private void buyItem() {
        if (isSelling) return;
        StorageStrategy strategy = Framework.getInstance().getStorageStrategy();
        int absoluteIndex = buySlotNumber + (buySelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL));

        getActiveShop().ifPresent(shop -> {
            if (absoluteIndex < shop.getShopItems().size()) {
                ShopItem selectedItem = shop.getShopItems().get(absoluteIndex);
                boolean success = strategy.buyItem(gameState.getPlayer(), selectedItem, quantityToTrade);

                if (success) {
                    Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
                    addToInventory(gameState.getPlayer(), selectedItem.getItemId(), quantityToTrade);
                    postTransactionUpdate(true, selectedItem, null, quantityToTrade);
                }
            }
        });
        updateSliderVisibility();
    }

    private void sellItem() {
        if (!isSelling) return;
        StorageStrategy strategy = Framework.getInstance().getStorageStrategy();
        int absoluteIndex = sellSlotNumber + (sellSelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL));
        Inventory inventory = gameState.getPlayer().getInventory();

        if (absoluteIndex < inventory.getBackpack().size()) {
            InventoryItem selectedItem = inventory.getBackpack().get(absoluteIndex);
            if (selectedItem == null) return;
            boolean success = strategy.sellItem(gameState.getPlayer(), selectedItem, quantityToTrade);

            if (success) {
                Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
                selectedItem.setAmount(selectedItem.getAmount() - quantityToTrade);
                if (selectedItem.getAmount() <= 0) {
                    inventory.getBackpack().set(absoluteIndex, null);
                }
                inventory.syncAccount();
                postTransactionUpdate(false, null, selectedItem, quantityToTrade);
            }
        }
        updateSliderVisibility();
    }

    private void postTransactionUpdate(boolean isBuy, ShopItem shopItem, InventoryItem inventoryItem, int quantity) {
        StorageStrategy strategy = Framework.getInstance().getStorageStrategy();
        if (strategy.isOnline()) {
            Account refreshedAccount = strategy.fetchAccountData(Framework.getInstance().getAccount().getName(), 0);
            if (refreshedAccount != null) {
                int currentCoins = gameState.getPlayer().getCoins();
                int newCoins = refreshedAccount.getCoins();
                gameState.getPlayer().getPlayerDataManager().changeCoins(newCoins - currentCoins);
            }
        }
        else {
            if (isBuy) {
                gameState.getPlayer().changeCoins(-(shopItem.getCost() * quantity));
                shopItem.setStock(shopItem.getStock() - quantity);
                if (shopItem.getStock() <= 0) {
                    getActiveShop().ifPresent(shop -> shop.getShopItems().remove(shopItem));
                }
            }
            else {
                ItemData data = inventoryItem.getData();
                if (data == null) return;
                gameState.getPlayer().changeCoins(data.sellValue * quantity);
                getActiveShop().ifPresent(shop -> addToShop(shop, inventoryItem, quantity));
            }
        }
    }

    private void addToInventory(Player player, String itemId, int quantity) {
        Inventory inventory = player.getInventory();
        Optional<InventoryItem> existingItem = inventory.getBackpack().stream()
                .filter(invItem -> invItem != null && invItem.getItemId().equals(itemId))
                .findFirst();

        getActiveShop().flatMap(shop -> shop.getShopItems().stream()
                        .filter(si -> si.getItemId().equals(itemId))
                        .findFirst())
                .ifPresent(shopItem -> EventBus.getInstance().publish(new ItemPurchasedEvent(shopItem, quantity)));

        if (existingItem.isPresent() && existingItem.get().getData().stackable) {
            existingItem.get().addAmount(quantity);
        }
        else inventory.addItemToBackpack(new InventoryItem(itemId, quantity));
    }

    private void addToShop(Shop shop, InventoryItem inventoryItem, int quantity) {
        Optional<ShopItem> existingItem = shop.getShopItems().stream()
                .filter(shopItem -> shopItem.getItemId().equals(inventoryItem.getItemId()))
                .findFirst();

        ItemData data = inventoryItem.getData();
        if (data == null) return;
        if (existingItem.isPresent()) existingItem.get().addStock(quantity);
        else shop.getShopItems().add(new ShopItem(inventoryItem.getItemId(), quantity, data.sellValue * 2));
    }

    private void releaseSmallButtons(MouseEvent e) {
        AbstractButton[] buttons = shopOverlay.getSmallButtons();
        if (isMouseInButton(e, buttons[0]) && buttons[0].isMousePressed()) prevBuyPage();
        else if (isMouseInButton(e, buttons[1]) && buttons[1].isMousePressed()) nextBuyPage();
        else if (isMouseInButton(e, buttons[2]) && buttons[2].isMousePressed()) prevSellPage();
        else if (isMouseInButton(e, buttons[3]) && buttons[3].isMousePressed()) nextSellPage();
    }

    private void prevBuyPage() {
        this.buySelectedSlot = Math.max(buySelectedSlot - 1, 0);
    }

    private void nextBuyPage() {
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        getActiveShop().ifPresent(shop -> {
            int totalItems = shop.getShopItems().size();
            int maxPages = (int) Math.ceil((double) totalItems / itemsPerPage);
            int maxPageIndex = Math.max(0, maxPages - 1);
            this.buySelectedSlot = Math.min(buySelectedSlot + 1, maxPageIndex);
        });
    }

    private void prevSellPage() {
        this.sellSelectedSlot = Math.max(sellSelectedSlot - 1, 0);
    }

    private void nextSellPage() {
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        int maxPages = (int) Math.ceil((double) BACKPACK_CAPACITY / itemsPerPage);
        int maxPageIndex = Math.max(0, maxPages - 1);
        this.sellSelectedSlot = Math.min(sellSelectedSlot + 1, maxPageIndex);
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (AbstractButton button : shopOverlay.getMediumButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY -> buyItem();
                    case SELL -> sellItem();
                    case LEAVE -> EventBus.getInstance().publish(new OverlayChangeEvent(null));
                }
                break;
            }
        }
    }

    private void changeSlot(MouseEvent e) {
        if (checkSelection(e, SHOP_BUY_SLOT_X, SHOP_BUY_SLOT_Y, false)) return;
        checkSelection(e, SHOP_SELL_SLOT_X, SHOP_SELL_SLOT_Y, true);
    }

    private boolean checkSelection(MouseEvent e, int startX, int startY, boolean selling) {
        for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
            for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
                int x = startX + i * SLOT_SPACING;
                int y = startY + j * SLOT_SPACING;
                Rectangle slotBounds = new Rectangle(x, y, SLOT_SIZE, SLOT_SIZE);
                if (slotBounds.contains(e.getPoint())) {
                    if (selling) sellSlotNumber = i + j * SHOP_SLOT_MAX_ROW;
                    else buySlotNumber = i + j * SHOP_SLOT_MAX_ROW;
                    this.isSelling = selling;
                    shopOverlay.updateSelectedSlot(isSelling, selling ? sellSlotNumber : buySlotNumber);
                    return true;
                }
            }
        }
        return false;
    }

    private void moveUp() {
        if (isSelling) sellSlotNumber = Math.max(0, sellSlotNumber - SHOP_SLOT_MAX_ROW);
        else buySlotNumber = Math.max(0, buySlotNumber - SHOP_SLOT_MAX_ROW);
        shopOverlay.updateSelectedSlot(isSelling, isSelling ? sellSlotNumber : buySlotNumber);
    }

    private void moveDown() {
        if (isSelling) sellSlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, sellSlotNumber + SHOP_SLOT_MAX_ROW);
        else buySlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, buySlotNumber + SHOP_SLOT_MAX_ROW);
        shopOverlay.updateSelectedSlot(isSelling, isSelling ? sellSlotNumber : buySlotNumber);
    }

    private void moveLeft() {
        if (isSelling) sellSlotNumber = Math.max(0, sellSlotNumber - 1);
        else buySlotNumber = Math.max(0, buySlotNumber - 1);
        shopOverlay.updateSelectedSlot(isSelling, isSelling ? sellSlotNumber : buySlotNumber);
    }

    private void moveRight() {
        if (isSelling) sellSlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, sellSlotNumber + 1);
        else buySlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, buySlotNumber + 1);
        shopOverlay.updateSelectedSlot(isSelling, isSelling ? sellSlotNumber : buySlotNumber);
    }

    private void updateSliderVisibility() {
        ItemData selectedData = getSelectedItemData();
        boolean previouslyActive = sliderActive;
        sliderActive = selectedData != null && selectedData.stackable;
        if (sliderActive && !previouslyActive) {
            shopOverlay.getSlider().updateSlider(shopOverlay.getSlider().getButtonHitBox().x);
            quantityToTrade = 1;
        }
        else if (!sliderActive) quantityToTrade = 1;
    }

    private void updateQuantityFromSlider() {
        int maxQuantity = 1;
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        if (isSelling) {
            Inventory inventory = gameState.getPlayer().getInventory();
            int absoluteIndex = sellSlotNumber + (sellSelectedSlot * itemsPerPage);
            if (absoluteIndex < inventory.getBackpack().size()) {
                InventoryItem item = inventory.getBackpack().get(absoluteIndex);
                if (item != null) maxQuantity = item.getAmount();
            }
        }
        else {
            Optional<Shop> activeShop = getActiveShop();
            if (activeShop.isPresent()) {
                int absoluteIndex = buySlotNumber + (buySelectedSlot * itemsPerPage);
                if (absoluteIndex < activeShop.get().getShopItems().size())
                    maxQuantity = activeShop.get().getShopItems().get(absoluteIndex).getStock();
            }
        }
        quantityToTrade = 1 + (int) (shopOverlay.getSlider().getValue() * (maxQuantity - 1));
        quantityToTrade = Math.max(1, quantityToTrade);
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    // Getters
    public int getBuySlotNumber() {
        return buySlotNumber;
    }

    public int getSellSlotNumber() {
        return sellSlotNumber;
    }

    public int getBuySelectedSlot() {
        return buySelectedSlot;
    }

    public int getSellSelectedSlot() {
        return sellSelectedSlot;
    }

    public boolean isSelling() {
        return isSelling;
    }

    public boolean isSliderActive() {
        return sliderActive;
    }

    public int getQuantityToTrade() {
        return quantityToTrade;
    }

    public GameState getGameState() {
        return gameState;
    }

    public Optional<Shop> getActiveShop() {
        return gameState.getObjectManager().getObjects(Shop.class).stream().filter(Shop::isActive).findFirst();
    }

    public ItemData getSelectedItemData() {
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        if (isSelling) {
            Inventory inventory = gameState.getPlayer().getInventory();
            int absoluteIndex = sellSlotNumber + (sellSelectedSlot * itemsPerPage);
            if (absoluteIndex < inventory.getBackpack().size() && inventory.getBackpack().get(absoluteIndex) != null)
                return inventory.getBackpack().get(absoluteIndex).getData();
        }
        else {
            Optional<Shop> activeShop = getActiveShop();
            if (activeShop.isPresent()) {
                int absoluteIndex = buySlotNumber + (buySelectedSlot * itemsPerPage);
                if (absoluteIndex < activeShop.get().getShopItems().size())
                    return activeShop.get().getShopItems().get(absoluteIndex).getData();
            }
        }
        return null;
    }

    public void reset() {

    }
}