package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.inventory.*;
import platformer.model.inventory.database.ItemDatabase;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;
import platformer.state.types.GameState;
import platformer.ui.buttons.*;
import platformer.ui.overlays.controller.ShopViewController;
import platformer.utils.ImageUtils;

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
public class ShopOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final ShopViewController controller;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;
    private Rectangle2D buyPanel, sellPanel;
    private BufferedImage slotImage, coinIcon;
    private Rectangle2D.Double selectedSlot;
    private SliderButton slider;

    public ShopOverlay(GameState gameState) {
        this.controller = new ShopViewController(gameState, this);
        this.mediumButtons = new MediumButton[3];
        this.smallButtons = new SmallButton[4];
        init();
    }

    private void init() {
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    private void loadImages() {
        this.overlay = new Rectangle2D.Double(INV_OVERLAY_X, INV_OVERLAY_Y, INV_OVERLAY_WID, INV_OVERLAY_HEI);
        this.buyPanel = new Rectangle2D.Double(SHOP_BUY_OVERLAY_X, SHOP_BUY_OVERLAY_Y, SHOP_PANEL_WID, SHOP_PANEL_HEI);
        this.sellPanel = new Rectangle2D.Double(SHOP_SELL_OVERLAY_X, SHOP_SELL_OVERLAY_Y, SHOP_PANEL_WID, SHOP_PANEL_HEI);
        this.shopText = ImageUtils.importImage(SHOP_TXT, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = ImageUtils.importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
        this.coinIcon = SpriteManager.getInstance().loadFromSprite(COIN_SHEET, 1, 1, COIN_WID, COIN_HEI, 0, COIN_W, COIN_H)[0];
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
        int xPos = (controller.getBuySlotNumber() % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_X;
        int yPos = (controller.getBuySlotNumber() / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    public void updateSelectedSlot(boolean isSelling, int slotNumber) {
        if (isSelling) {
            this.selectedSlot.x = (slotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_SELL_SLOT_X;
            this.selectedSlot.y = (slotNumber / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_SELL_SLOT_Y;
        }
        else {
            this.selectedSlot.x = (slotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_X;
            this.selectedSlot.y = (slotNumber / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SHOP_BUY_SLOT_Y;
        }
    }

    @Override
    public void update() {
        controller.update();
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        if (controller.isSliderActive()) slider.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        renderOverlay(g);
        renderPanels((Graphics2D) g);
        renderButtons(g);
        renderSlots(g);
        renderShopItems(g);
        renderInventoryItems(g);
        if (controller.isSliderActive()) renderSlider(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

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

        g2d.drawString("Page: " + (controller.getBuySelectedSlot() + 1), (int) (buyPanel.getX() + buyPanel.getWidth() - 40 * SCALE), (int) buyPanel.getY() - 10);
        g2d.drawString("Page: " + (controller.getSellSelectedSlot() + 1), (int) (sellPanel.getX() + sellPanel.getWidth() - 40 * SCALE), (int) sellPanel.getY() - 10);
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(mediumButtons).forEach(mediumButton -> mediumButton.render(g));
        Arrays.stream(smallButtons).forEach(smallButton -> smallButton.render(g));
    }

    private void renderShopItems(Graphics g) {
        controller.getActiveShop().ifPresent(shop -> {
            int slot = controller.getBuySelectedSlot() * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);
            List<ShopItem> items = shop.getShopItems();
            for (int i = slot; i < items.size(); i++) {
                int slotIndex = i - slot;
                if (slotIndex < (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL)) {
                    ShopItem item = items.get(i);
                    if (item.getStock() > 0) {
                        renderItem(g, item.getItemId(), item.getStock(), item.getCost(), slotIndex, SHOP_BUY_SLOT_X, SHOP_BUY_SLOT_Y, false);
                    }
                }
            }
        });
    }

    private void renderInventoryItems(Graphics g) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        int slot = controller.getSellSelectedSlot() * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);
        List<InventoryItem> backpack = inventory.getBackpack();
        for (int i = slot; i < backpack.size(); i++) {
            int slotIndex = i - slot;
            if (slotIndex < (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL)) {
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
        g.drawImage(ImageUtils.importImage(itemData.imagePath, -1, -1), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString(String.valueOf(amount), xPos + ITEM_COUNT_OFFSET_X, yPos + ITEM_COUNT_OFFSET_Y);

        if (controller.isSelling() == isInventoryPanel) renderText(g, itemData, value, slot);
    }

    private void renderText(Graphics g, ItemData itemData, int value, int slot) {
        int currentSlotNumber = controller.isSelling() ? controller.getSellSlotNumber() : controller.getBuySlotNumber();
        if (currentSlotNumber == slot) {
            int totalCoins = controller.getGameState().getPlayer().getCoins();
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));

            g.setColor(SHOP_TEXT_DEFAULT);
            g.drawString("Item value: ", COST_TEXT_X, COST_TEXT_Y);
            int valueTextX = COST_TEXT_X + g.getFontMetrics().stringWidth("Item value: ");
            g.setColor(totalCoins >= value * controller.getQuantityToTrade() || controller.isSelling() ? SHOP_TEXT_GOLD : SHOP_TEXT_CANNOT_AFFORD);
            g.drawString(String.valueOf(value), valueTextX, COST_TEXT_Y);
            g.drawImage(coinIcon, valueTextX + g.getFontMetrics().stringWidth(String.valueOf(value)) + (int)(3 * SCALE), COST_TEXT_Y - (int)(12 * SCALE), COIN_WID, COIN_HEI, null);

            g.setColor(SHOP_TEXT_DEFAULT);
            g.drawString("Total coins: ", POCKET_TEXT_X, POCKET_TEXT_Y);
            int coinsTextX = POCKET_TEXT_X + g.getFontMetrics().stringWidth("Total coins: ");
            g.setColor(SHOP_TEXT_GOLD);
            g.drawString(String.valueOf(totalCoins), coinsTextX, POCKET_TEXT_Y);
            g.drawImage(coinIcon, coinsTextX + g.getFontMetrics().stringWidth(String.valueOf(totalCoins)) + (int)(3 * SCALE), POCKET_TEXT_Y - (int)(12 * SCALE), COIN_WID, COIN_HEI, null);

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

        if (controller.isSliderActive()) {
            g.setColor(SHOP_TEXT_DEFAULT);
            g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
            g.drawString("Quantity: " + controller.getQuantityToTrade(), SHOP_QUANTITY_TEXT_X, SHOP_QUANTITY_TEXT_Y);
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

    // Event Delegation
    @Override
    public void mouseDragged(MouseEvent e) {
        controller.mouseDragged(e);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
        controller.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        controller.mouseReleased(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        controller.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        controller.keyPressed(e);
    }

    @Override
    public void reset() {
        controller.reset();
    }

    public ShopViewController getController() {
        return controller;
    }

    public Rectangle2D getOverlay() {
        return overlay;
    }

    public MediumButton[] getMediumButtons() {
        return mediumButtons;
    }

    public SmallButton[] getSmallButtons() {
        return smallButtons;
    }

    public SliderButton getSlider() {
        return slider;
    }
}