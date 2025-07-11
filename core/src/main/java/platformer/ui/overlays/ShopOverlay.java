package platformer.ui.overlays;

import platformer.animation.Animation;
import platformer.model.gameObjects.objects.Shop;
import platformer.model.inventory.*;
import platformer.state.GameState;
import platformer.ui.buttons.*;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.AnimConstants.COIN_H;
import static platformer.constants.AnimConstants.COIN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * ShopOverlay class is responsible for rendering the shop overlay.
 * It allows the player to buy and sell items from the shop.
 */
// TODO: Refactor !!!
public class ShopOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private Rectangle2D buyPanel, sellPanel;
    private BufferedImage slotImage, coinIcon;
    private Rectangle2D.Double selectedSlot;
    private int buySelectedSlot, sellSelectedSlot;
    private int buySlotNumber, sellSlotNumber;
    private boolean isSelling;

    private SliderButton slider;
    private int quantityToTrade = 1;
    private boolean sliderActive = false;

    private List<Shop> shops;

    public ShopOverlay(GameState gameState) {
        this.gameState = gameState;
        this.shops = gameState.getObjectManager().getObjects(Shop.class);
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[4];
        init();
    }

    // Init
    private void init() {
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.buyPanel = new Rectangle2D.Double(SHOP_BUY_OVERLAY_X, SHOP_BUY_OVERLAY_Y, SHOP_PANEL_WID, SHOP_PANEL_HEI);
        this.sellPanel = new Rectangle2D.Double(SHOP_SELL_OVERLAY_X, SHOP_SELL_OVERLAY_Y, SHOP_PANEL_WID, SHOP_PANEL_HEI);
        this.shopText = Utils.getInstance().importImage(SHOP_TXT, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
        this.coinIcon = Animation.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(PREV_BUY_BTN_X, SMALL_SHOP_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(NEXT_BUY_BTN_X, SMALL_SHOP_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);
        smallButtons[2] = new SmallButton(PREV_SELL_BTN_X, SMALL_SHOP_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[3] = new SmallButton(NEXT_SELL_BTN_X, SMALL_SHOP_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(BUY_BTN_X, BUY_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.BUY);
        mediumButtons[1] = new MediumButton(LEAVE_BTN_X, LEAVE_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.LEAVE);
        mediumButtons[2] = new MediumButton(SELL_BTN_X, SELL_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.SELL);

        this.slider = new SliderButton(SHOP_SLIDER_BTN_X, SHOP_SLIDER_BTN_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE);
    }

    private void initSelectedSlot() {
        int xPos = (buySlotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_X;
        int yPos = (buySlotNumber / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        updateSliderVisibility();
        if (sliderActive) {
            slider.update();
            updateQuantityFromSlider();
        }
    }

    private void updateSliderVisibility() {
        ItemData selectedData = getSelectedItemData();
        boolean previouslyActive = sliderActive;
        sliderActive = selectedData != null && selectedData.stackable;
        if (sliderActive && !previouslyActive) {
            slider.updateSlider(slider.getButtonHitBox().x);
            quantityToTrade = 1;
        }
        else if (!sliderActive) quantityToTrade = 1;
    }

    @Override
    public void render(Graphics g) {
        renderOverlay(g);
        renderPanels((Graphics2D) g);
        renderButtons(g);
        renderSlots(g);
        renderShopItems(g);
        renderInventoryItems(g);
        if (sliderActive) renderSlider(g);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    // Render
    private void renderOverlay(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(OVERLAY_COLOR);
        g2d.fill(overlay);
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(overlay);
        g.drawImage(shopText, SHOP_TEXT_X, SHOP_TEXT_Y, shopText.getWidth(), shopText.getHeight(), null);
    }

    private void renderPanels(Graphics2D g2d) {
        g2d.setColor(OVERLAY_SPACE_COLOR);
        g2d.fill(buyPanel);
        g2d.fill(sellPanel);
        g2d.setColor(SHOP_TEXT_DEFAULT);
        g2d.setStroke(new BasicStroke(1));
        g2d.draw(buyPanel);
        g2d.draw(sellPanel);

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g2d.setColor(SHOP_TEXT_DEFAULT);
        g2d.drawString("Merchant", (int) buyPanel.getX(), (int) buyPanel.getY() - 10);
        g2d.drawString("Backpack", (int) sellPanel.getX(), (int) sellPanel.getY() - 10);

        int buyPageNumber = buySelectedSlot + 1;
        g2d.drawString("Page: " + buyPageNumber, (int) (buyPanel.getX() + buyPanel.getWidth() - 40 * SCALE), (int) buyPanel.getY() - 10);
        int sellPageNumber = sellSelectedSlot + 1;
        g2d.drawString("Page: " + sellPageNumber, (int) (sellPanel.getX() + sellPanel.getWidth() - 40 * SCALE), (int) sellPanel.getY() - 10);
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(mediumButtons).forEach(mediumButton -> mediumButton.render(g));
        Arrays.stream(smallButtons).forEach(smallButton -> smallButton.render(g));
    }

    private void renderShopItems(Graphics g) {
        for (Shop shop : shops) {
            if (shop.isActive()) {
                int slot = buySelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);
                List<ShopItem> items = shop.getShopItems();
                for (int i = 0; i < items.size(); i++) {
                    int slotIndex = i - slot;
                    if (slotIndex >= 0 && slotIndex < (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL)) {
                        ShopItem item = items.get(i);
                        if (item.getStock() > 0) {
                            renderItem(g, item.getItemId(), item.getStock(), item.getCost(), slotIndex, SHOP_BUY_SLOT_X, SHOP_BUY_SLOT_Y, false);
                        }
                    }
                }
            }
        }
    }

    private void renderInventoryItems(Graphics g) {
        Inventory inventory = gameState.getPlayer().getInventory();
        int slot = sellSelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);
        List<InventoryItem> backpack = inventory.getBackpack();
        for (int i = 0; i < backpack.size(); i++) {
            int slotIndex = i - slot;
            if (slotIndex >= 0 && slotIndex < (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL)) {
                InventoryItem item = backpack.get(i);
                renderItem(g, item.getItemId(), item.getAmount(), item.getData().sellValue, slotIndex, SHOP_SELL_SLOT_X, SHOP_SELL_SLOT_Y, true);
            }
        }
    }

    private void renderItem(Graphics g, String itemId, int amount, int value, int slot, int xSlot, int ySlot, boolean isInventoryPanel) {
        ItemData itemData = ItemDatabase.getInstance().getItemData(itemId);
        if (itemData == null) return;

        int xPos = (slot % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + xSlot + ITEM_OFFSET_X;
        int yPos = (slot / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + ySlot + ITEM_OFFSET_Y;

        g.setColor(itemData.rarity.getColor());
        g.fillRect(xPos - (int) (ITEM_OFFSET_X / 1.1), yPos - (int) (ITEM_OFFSET_Y / 1.1), (int) (SLOT_SIZE / 1.06), (int) (SLOT_SIZE / 1.06));
        g.drawImage(Utils.getInstance().importImage(itemData.imagePath, -1, -1), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        int countX = xPos + ITEM_COUNT_OFFSET_X, countY = yPos + ITEM_COUNT_OFFSET_Y;
        g.drawString(String.valueOf(amount), countX, countY);

        if (isSelling == isInventoryPanel) {
            renderText(g, itemData, value, slot);
        }
    }

    private void renderText(Graphics g, ItemData itemData, int value, int slot) {
        int currentSlotNumber = isSelling ? sellSlotNumber : buySlotNumber;
        if (currentSlotNumber == slot) {
            int totalCoins = gameState.getPlayer().getCoins();
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

            g.setColor(SHOP_TEXT_DEFAULT);
            g.drawString("Item value: ", COST_TEXT_X, COST_TEXT_Y);
            int valueTextX = COST_TEXT_X + g.getFontMetrics().stringWidth("Item value: ");
            if (totalCoins >= value) g.setColor(SHOP_TEXT_GOLD);
            else g.setColor(SHOP_TEXT_CANNOT_AFFORD);
            g.drawString(String.valueOf(value), valueTextX, COST_TEXT_Y);
            int valueIconX = valueTextX + g.getFontMetrics().stringWidth(String.valueOf(value)) + (int)(3 * SCALE);
            g.drawImage(coinIcon, valueIconX, COST_TEXT_Y - (int)(12 * SCALE), COIN_WID, COIN_HEI, null);

            g.setColor(SHOP_TEXT_DEFAULT);
            g.drawString("Total coins: ", POCKET_TEXT_X, POCKET_TEXT_Y);
            int coinsTextX = POCKET_TEXT_X + g.getFontMetrics().stringWidth("Total coins: ");
            g.setColor(SHOP_TEXT_GOLD);
            g.drawString(String.valueOf(totalCoins), coinsTextX, POCKET_TEXT_Y);
            int coinsIconX = coinsTextX + g.getFontMetrics().stringWidth(String.valueOf(totalCoins)) + (int)(3 * SCALE);
            g.drawImage(coinIcon, coinsIconX, POCKET_TEXT_Y - (int)(12 * SCALE), COIN_WID, COIN_HEI, null);

            renderItemDescription(g, itemData);
        }
    }

    private void renderItemDescription(Graphics g, ItemData itemData) {
        Color rarityColor = itemData.rarity.getColor();
        g.setColor(new Color(rarityColor.getRed(), rarityColor.getGreen(), rarityColor.getBlue()));
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(itemData.name, SHOP_ITEM_NAME_X, SHOP_ITEM_NAME_Y);
        g.setColor(SHOP_TEXT_DESC);
        g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));

        int descriptionMaxWidth = (int) (sellPanel.getX() - SHOP_ITEM_DESC_X - (10 * SCALE));
        List<String> wrappedLines = wrapText(itemData.description, descriptionMaxWidth, g.getFontMetrics());

        int lineHeight = g.getFontMetrics().getHeight();
        int y = SHOP_ITEM_DESC_Y;
        for (String line : wrappedLines) {
            g.drawString(line, SHOP_ITEM_DESC_X, y);
            y += lineHeight;
        }

        if (sliderActive) {
            g.setColor(SHOP_TEXT_DEFAULT);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            g.drawString("Quantity: " + quantityToTrade, SHOP_QUANTITY_TEXT_X, SHOP_QUANTITY_TEXT_Y);
        }
    }

    private void renderSlots(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
                int xPosLeft = i * SLOT_SPACING + SHOP_BUY_SLOT_X;
                int yPosLeft = j * SLOT_SPACING + SHOP_BUY_SLOT_Y;
                g.drawImage(slotImage, xPosLeft, yPosLeft, slotImage.getWidth(), slotImage.getHeight(), null);
                int xPosRight = i * SLOT_SPACING + SHOP_SELL_SLOT_X;
                int yPosRight = j * SLOT_SPACING + SHOP_SELL_SLOT_Y;
                g.drawImage(slotImage, xPosRight, yPosRight, slotImage.getWidth(), slotImage.getHeight(), null);
            }
        }
    }

    private void renderSlider(Graphics g) {
        slider.render(g);
    }

    private List<String> wrapText(String text, int maxWidth, FontMetrics fm) {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (fm.stringWidth(currentLine + " " + word) <= maxWidth) {
                    if (!currentLine.isEmpty()) currentLine.append(" ");
                    currentLine.append(word);
                }
                else {
                    lines.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                }
            }
            if (!currentLine.isEmpty()) lines.add(currentLine.toString());
        }
        return lines;
    }

    // Selection
    private void setSelectedSlotBuy() {
        this.selectedSlot.x = (buySlotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_X;
        int offset = buySlotNumber / SHOP_SLOT_MAX_ROW;
        this.selectedSlot.y = offset * SLOT_SPACING + SHOP_BUY_SLOT_Y;
        isSelling = false;
    }

    private void setSelectedSlotSell() {
        this.selectedSlot.x = (sellSlotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_SELL_SLOT_X;
        int offset = sellSlotNumber / SHOP_SLOT_MAX_ROW;
        this.selectedSlot.y = offset * SLOT_SPACING + SHOP_SELL_SLOT_Y;
        isSelling = true;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        checkBuySelection(x, y);
        checkSellSelection(x, y);
    }

    private void checkBuySelection(int x, int y) {
        for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
                int xStart = i * SLOT_SPACING + SHOP_BUY_SLOT_X;
                int xEnd = i * SLOT_SPACING + SHOP_BUY_SLOT_X + SLOT_SIZE;
                int yStart =  j * SLOT_SPACING + SHOP_BUY_SLOT_Y;
                int yEnd = j * SLOT_SPACING + SHOP_BUY_SLOT_Y + SLOT_SIZE;

                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    buySlotNumber = i + (j * SHOP_SLOT_MAX_ROW);
                    setSelectedSlotBuy();
                    break;
                }
            }
        }
    }

    private void checkSellSelection(int x, int y) {
        for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
                int xStart = i * SLOT_SPACING + SHOP_SELL_SLOT_X;
                int xEnd = i * SLOT_SPACING + SHOP_SELL_SLOT_X + SLOT_SIZE;
                int yStart =  j * SLOT_SPACING + SHOP_SELL_SLOT_Y;
                int yEnd = j * SLOT_SPACING + SHOP_SELL_SLOT_Y + SLOT_SIZE;

                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    sellSlotNumber = i + (j * SHOP_SLOT_MAX_ROW);
                    setSelectedSlotSell();
                    break;
                }
            }
        }
    }

    // Actions
    private void buyItem() {
        if (isSelling) return;
        int absoluteIndex = buySlotNumber + (buySelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL));
        shops.stream()
                .filter(Shop::isActive)
                .forEach(shop -> shop.buyItem(gameState.getPlayer(), absoluteIndex, quantityToTrade));
        updateSliderVisibility();
    }

    private void sellItem() {
        if (!isSelling) return;
        int absoluteIndex = sellSlotNumber + (sellSelectedSlot * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL));
        shops.stream()
                .filter(Shop::isActive)
                .forEach(shop -> shop.sellItem(gameState.getPlayer(), absoluteIndex, quantityToTrade));
        updateSliderVisibility();
    }

    private void prevBackpackSlot(SmallButton button) {
        if (button == smallButtons[0])
            this.buySelectedSlot = Math.max(buySelectedSlot-1, 0);
        else if (button == smallButtons[2])
            this.sellSelectedSlot = Math.max(sellSelectedSlot-1, 0);
    }

    private void nextBackpackSlot(SmallButton button) {
        if (button == smallButtons[1])
            this.buySelectedSlot = Math.min(buySelectedSlot+1, SHOP_SLOT_CAP);
        else if (button == smallButtons[3])
            this.sellSelectedSlot = Math.min(sellSelectedSlot+1, SHOP_SLOT_CAP);
    }

    private ItemData getSelectedItemData() {
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        if (isSelling) {
            Inventory inventory = gameState.getPlayer().getInventory();
            int absoluteIndex = sellSlotNumber + (sellSelectedSlot * itemsPerPage);
            if (absoluteIndex < inventory.getBackpack().size())
                return inventory.getBackpack().get(absoluteIndex).getData();
        }
        else {
            for (Shop shop : shops) {
                if (shop.isActive()) {
                    int absoluteIndex = buySlotNumber + (buySelectedSlot * itemsPerPage);
                    if (absoluteIndex < shop.getShopItems().size())
                        return shop.getShopItems().get(absoluteIndex).getData();
                }
            }
        }
        return null;
    }

    private void updateQuantityFromSlider() {
        int maxQuantity = 1;
        int itemsPerPage = SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL;
        if (isSelling) {
            Inventory inventory = gameState.getPlayer().getInventory();
            int absoluteIndex = sellSlotNumber + (sellSelectedSlot * itemsPerPage);
            if (absoluteIndex < inventory.getBackpack().size())
                maxQuantity = inventory.getBackpack().get(absoluteIndex).getAmount();
        }
        else {
            for (Shop shop : shops) {
                if (shop.isActive()) {
                    int absoluteIndex = buySlotNumber + (buySelectedSlot * itemsPerPage);
                    if (absoluteIndex < shop.getShopItems().size())
                        maxQuantity = shop.getShopItems().get(absoluteIndex).getStock();
                }
            }
        }
        quantityToTrade = 1 + (int) (slider.getValue() * (maxQuantity - 1));
        quantityToTrade = Math.max(1, quantityToTrade);
    }


    @Override
    public void mouseDragged(MouseEvent e) {
        if (slider.isMousePressed()) slider.updateSlider(e.getX());
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (sliderActive && isMouseInButton(e, slider)) slider.setMousePressed(true);
        else {
            setMousePressed(e, smallButtons);
            setMousePressed(e, mediumButtons);
            changeSlot(e);
        }
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        slider.resetMouseSet();
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(smallButtons).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (SmallButton button : smallButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PREV:
                        prevBackpackSlot(button);
                        break;
                    case NEXT:
                        nextBackpackSlot(button);
                        break;
                    default: break;
                }
                break;
            }
        }
    }

    public void releaseMediumButtons(MouseEvent e) {
        for (MediumButton button : mediumButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY:
                        buyItem();
                        break;
                    case SELL:
                        sellItem();
                        break;
                    case LEAVE:
                        gameState.setOverlay(null);
                        break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, smallButtons);
        setMouseMoved(e, mediumButtons);
        slider.setMouseOver(false);
        if (isMouseInButton(e, slider)) slider.setMouseOver(true);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                moveUp();
                break;
            case KeyEvent.VK_DOWN:
                moveDown();
                break;
            case KeyEvent.VK_LEFT:
                moveLeft();
                break;
            case KeyEvent.VK_RIGHT:
                moveRight();
                break;
            case KeyEvent.VK_B:
                buyItem();
                break;
            case KeyEvent.VK_S:
                sellItem();
                break;
            default: break;
        }
    }

    private void moveUp() {
        if (isSelling) {
            sellSlotNumber -= SHOP_SLOT_MAX_ROW;
            sellSlotNumber = Math.max(0, sellSlotNumber);
            setSelectedSlotSell();
        }
        else {
            buySlotNumber -= SHOP_SLOT_MAX_ROW;
            buySlotNumber = Math.max(0, buySlotNumber);
            setSelectedSlotBuy();
        }
    }

    private void moveDown() {
        if (isSelling) {
            sellSlotNumber += SHOP_SLOT_MAX_ROW;
            sellSlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, sellSlotNumber);
            setSelectedSlotSell();
        }
        else {
            buySlotNumber += SHOP_SLOT_MAX_ROW;
            buySlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, buySlotNumber);
            setSelectedSlotBuy();
        }
    }

    private void moveLeft() {
        if (isSelling) {
            sellSlotNumber--;
            sellSlotNumber = Math.max(0, sellSlotNumber);
            setSelectedSlotSell();
        }
        else {
            buySlotNumber--;
            buySlotNumber = Math.max(0, buySlotNumber);
            setSelectedSlotBuy();
        }
    }

    private void moveRight() {
        if (isSelling) {
            sellSlotNumber++;
            sellSlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, sellSlotNumber);
            setSelectedSlotSell();
        }
        else {
            buySlotNumber++;
            buySlotNumber = Math.min(SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL - 1, buySlotNumber);
            setSelectedSlotBuy();
        }
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public void reset() {
        this.shops = gameState.getObjectManager().getObjects(Shop.class);
    }

}
