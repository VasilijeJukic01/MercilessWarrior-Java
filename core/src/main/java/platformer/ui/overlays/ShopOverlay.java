package platformer.ui.overlays;

import platformer.animation.SpriteManager;
import platformer.model.inventory.Inventory;
import platformer.model.inventory.item.InventoryItem;
import platformer.model.inventory.item.ItemData;
import platformer.model.inventory.item.ShopItem;
import platformer.state.types.GameState;
import platformer.ui.buttons.*;
import platformer.ui.overlays.controller.ShopViewController;

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
import static platformer.constants.FilePaths.COIN_SHEET;
import static platformer.constants.UI.*;

/**
 * Renders the Shop overlay with runic plaques, recessed item slots, a central Trade Inspector,
 * and dynamic total price calculation.
 */
public class ShopOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final ShopViewController controller;

    private Rectangle2D overlay;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;
    private Rectangle2D buyPanel, sellPanel, tradePanel;
    private BufferedImage coinIcon;
    private Rectangle2D.Double selectedSlot;
    private SliderButton slider;

    private final Font boldFont = new Font("Arial", Font.BOLD, FONT_MEDIUM);
    private final Font plainFont = new Font("Arial", Font.PLAIN, FONT_LIGHT);
    private ItemData lastSelectedItem = null;
    private List<String> linesCache = new ArrayList<>();

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
        this.tradePanel = new Rectangle2D.Double(scale(302), SHOP_BUY_OVERLAY_Y, scale(228), SHOP_PANEL_HEI);
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
        g.setColor(new Color(0, 0, 0, 165));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        Graphics2D g2d = (Graphics2D) g;
        renderOverlay(g2d);
        renderPanels(g2d);
        renderShopSlots(g2d);
        renderInventorySlots(g2d);
        renderTradeInspector(g2d);

        if (controller.isSliderActive()) slider.render(g);
        UIButtonRenderer.drawSelectedSlotFrame(g, (int) selectedSlot.x, (int) selectedSlot.y, (int) selectedSlot.width);
        renderButtons(g);
    }

    private void renderOverlay(Graphics2D g2d) {
        UIButtonRenderer.drawRunicPanel(g2d, overlay);
        UIButtonRenderer.drawRunicHeader(g2d, "MERCHANT", INV_OVERLAY_X + INV_OVERLAY_WID / 2, SHOP_TEXT_Y);
    }

    private void renderPanels(Graphics2D g2d) {
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) buyPanel.getX(), (int) buyPanel.getY(), (int) buyPanel.getWidth(), (int) buyPanel.getHeight());
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) sellPanel.getX(), (int) sellPanel.getY(), (int) sellPanel.getWidth(), (int) sellPanel.getHeight());
        UIButtonRenderer.drawRunicSubPanel(g2d, (int) tradePanel.getX(), (int) tradePanel.getY(), (int) tradePanel.getWidth(), (int) tradePanel.getHeight());

        g2d.setFont(UIButtonRenderer.getFont(10.5f * SCALE));
        g2d.setColor(new Color(210, 228, 230));
        g2d.drawString("MERCHANT WARES", (int) buyPanel.getX() + scale(4), (int) buyPanel.getY() - scale(5));
        g2d.drawString("YOUR BACKPACK", (int) sellPanel.getX() + scale(4), (int) sellPanel.getY() - scale(5));

        g2d.setFont(new Font("Arial", Font.BOLD, FONT_LIGHT));
        g2d.setColor(INV_TEXT_LABEL);
        String buyPage = "Page " + (controller.getBuySelectedSlot() + 1);
        String sellPage = "Page " + (controller.getSellSelectedSlot() + 1);
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(buyPage, (int) (buyPanel.getX() + buyPanel.getWidth() - fm.stringWidth(buyPage) - scale(4)), (int) buyPanel.getY() - scale(5));
        g2d.drawString(sellPage, (int) (sellPanel.getX() + sellPanel.getWidth() - fm.stringWidth(sellPage) - scale(4)), (int) sellPanel.getY() - scale(5));
    }

    private void renderShopSlots(Graphics2D g2d) {
        List<ShopItem> items = controller.getActiveShop().map(s -> (List<ShopItem>) s.getShopItems()).orElse(List.of());
        int pageOffset = controller.getBuySelectedSlot() * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);

        for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
            for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
                int slotIdx = j * SHOP_SLOT_MAX_ROW + i;
                int absIdx = pageOffset + slotIdx;
                int xPos = i * SLOT_SPACING + SHOP_BUY_SLOT_X;
                int yPos = j * SLOT_SPACING + SHOP_BUY_SLOT_Y;

                ShopItem shopItem = (absIdx < items.size() && items.get(absIdx).getStock() > 0) ? items.get(absIdx) : null;
                Color rFill = (shopItem != null && shopItem.getData() != null) ? shopItem.getData().rarity.getColor() : null;
                Color rBorder = (shopItem != null && shopItem.getData() != null) ? shopItem.getData().rarity.getTextColor() : null;

                UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, SLOT_SIZE, rFill, rBorder, false);
                if (shopItem != null) {
                    g2d.drawImage(shopItem.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    UIButtonRenderer.drawStackCount(g2d, shopItem.getStock(), xPos, yPos, SLOT_SIZE);
                }
            }
        }
    }

    private void renderInventorySlots(Graphics2D g2d) {
        Inventory inventory = controller.getGameState().getPlayer().getInventory();
        List<InventoryItem> backpack = inventory.getBackpack();
        int pageOffset = controller.getSellSelectedSlot() * (SHOP_SLOT_MAX_ROW * SHOP_SLOT_MAX_COL);

        for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
            for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
                int slotIdx = j * SHOP_SLOT_MAX_ROW + i;
                int absIdx = pageOffset + slotIdx;
                int xPos = i * SLOT_SPACING + SHOP_SELL_SLOT_X;
                int yPos = j * SLOT_SPACING + SHOP_SELL_SLOT_Y;

                InventoryItem invItem = (absIdx < backpack.size()) ? backpack.get(absIdx) : null;
                Color rFill = (invItem != null && invItem.getData() != null) ? invItem.getData().rarity.getColor() : null;
                Color rBorder = (invItem != null && invItem.getData() != null) ? invItem.getData().rarity.getTextColor() : null;

                UIButtonRenderer.drawRunicSlot(g2d, xPos, yPos, SLOT_SIZE, rFill, rBorder, false);
                if (invItem != null) {
                    g2d.drawImage(invItem.getModel(), xPos + ITEM_OFFSET_X, yPos + ITEM_OFFSET_Y, ITEM_SIZE, ITEM_SIZE, null);
                    UIButtonRenderer.drawStackCount(g2d, invItem.getAmount(), xPos, yPos, SLOT_SIZE);
                }
            }
        }
    }

    private void renderTradeInspector(Graphics2D g2d) {
        int leftX = (int) tradePanel.getX() + scale(10);
        int rightX = (int) (tradePanel.getX() + tradePanel.getWidth() - scale(10));
        int topY = (int) tradePanel.getY() + scale(16);

        // Player Gold Pouch Header
        int playerCoins = controller.getGameState().getPlayer().getCoins();
        g2d.setFont(boldFont);
        g2d.setColor(INV_TEXT_LABEL);
        g2d.drawString("Your Gold:", leftX, topY);

        String coinStr = String.valueOf(playerCoins);
        int coinW = g2d.getFontMetrics().stringWidth(coinStr);
        g2d.setColor(SHOP_TEXT_GOLD);
        g2d.drawString(coinStr, rightX - coinW - COIN_WID - scale(3), topY);
        g2d.drawImage(coinIcon, rightX - COIN_WID, topY - g2d.getFontMetrics().getAscent() + 1, COIN_WID, COIN_HEI, null);

        topY += scale(6);
        g2d.setColor(new Color(55, 215, 210, 70));
        g2d.drawLine(leftX, topY, rightX, topY);

        ItemData itemData = controller.getSelectedItemData();
        BufferedImage itemModel = controller.getSelectedItemModel();
        int unitValue = controller.getSelectedItemUnitValue();

        if (itemData == null) {
            g2d.setFont(new Font("Arial", Font.ITALIC, FONT_LIGHT));
            g2d.setColor(new Color(135, 155, 160));
            String msg = "Select an item to trade.";
            int mw = g2d.getFontMetrics().stringWidth(msg);
            g2d.drawString(msg, (int) (tradePanel.getX() + (tradePanel.getWidth() - mw) / 2), (int) (tradePanel.getY() + tradePanel.getHeight() / 2));
            return;
        }

        int previewY = topY + scale(8);
        int previewS = scale(34);
        UIButtonRenderer.drawRunicSlot(g2d, leftX, previewY, previewS, itemData.rarity.getColor(), itemData.rarity.getTextColor(), false);
        if (itemModel != null) {
            g2d.drawImage(itemModel, leftX + scale(4), previewY + scale(4), previewS - scale(8), previewS - scale(8), null);
        }

        int nameX = leftX + previewS + scale(8);
        int nameY = previewY + scale(13);
        g2d.setFont(boldFont);
        g2d.setColor(itemData.rarity.getTextColor());
        g2d.drawString(itemData.name, nameX, nameY);

        UIButtonRenderer.drawBadge(g2d, itemData.rarity.name(), nameX, nameY + scale(14), itemData.rarity.getColor(), itemData.rarity.getTextColor());

        int priceY = previewY + previewS + scale(14);
        int totalValue = unitValue * controller.getQuantityToTrade();
        boolean canAfford = controller.isSelling() || playerCoins >= totalValue;

        g2d.setFont(boldFont);
        g2d.setColor(INV_TEXT_LABEL);
        String priceLabel = controller.isSelling() ? "Sell Gain:" : "Total Cost:";
        g2d.drawString(priceLabel, leftX, priceY);

        String totalStr = totalValue + (controller.getQuantityToTrade() > 1 ? " (" + unitValue + " ea)" : "");
        int tw = g2d.getFontMetrics().stringWidth(totalStr);
        g2d.setColor(canAfford ? SHOP_TEXT_GOLD : SHOP_TEXT_CANNOT_AFFORD);
        g2d.drawString(totalStr, rightX - tw - COIN_WID - scale(3), priceY);
        g2d.drawImage(coinIcon, rightX - COIN_WID, priceY - g2d.getFontMetrics().getAscent() + 1, COIN_WID, COIN_HEI, null);

        // Desc
        g2d.setColor(SHOP_TEXT_DESC);
        g2d.setFont(plainFont);
        if (itemData != lastSelectedItem) {
            lastSelectedItem = itemData;
            linesCache = wrapText(itemData.description, rightX - leftX, g2d.getFontMetrics());
        }

        int descY = priceY + scale(15);
        for (int i = 0; i < Math.min(linesCache.size(), 4); i++) {
            g2d.drawString(linesCache.get(i), leftX, descY);
            descY += g2d.getFontMetrics().getHeight();
        }

        if (controller.isSliderActive()) {
            g2d.setColor(SHOP_TEXT_DEFAULT);
            g2d.setFont(boldFont);
            g2d.drawString("Quantity: " + controller.getQuantityToTrade(), SHOP_QUANTITY_TEXT_X, SHOP_QUANTITY_TEXT_Y);
        }

        g2d.setFont(new Font("Arial", Font.ITALIC, FONT_SMALL));
        g2d.setColor(new Color(120, 175, 180));
        g2d.drawString("Right-Click slot to quick " + (controller.isSelling() ? "Sell (1x)" : "Buy (1x)"), leftX, (int) (tradePanel.getY() + tradePanel.getHeight() - scale(5)));
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(mediumButtons).forEach(mediumButton -> mediumButton.render(g));
        Arrays.stream(smallButtons).forEach(smallButton -> smallButton.render(g));
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
                } else {
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
    public void mouseClicked(MouseEvent e) {}

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
    public void keyReleased(KeyEvent e) {
        controller.keyReleased(e);
    }

    @Override
    public void reset() {
        controller.reset();
        lastSelectedItem = null;
        linesCache.clear();
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