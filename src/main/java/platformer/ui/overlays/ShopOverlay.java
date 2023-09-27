package platformer.ui.overlays;

import platformer.model.gameObjects.objects.Shop;
import platformer.state.GameState;
import platformer.model.inventory.ShopItem;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

public class ShopOverlay implements Overlay {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int slotNumber;

    private List<Shop> shops;

    public ShopOverlay(GameState gameState) {
        this.gameState = gameState;
        this.shops = gameState.getObjectManager().getObjects(Shop.class);
        this.buttons = new MediumButton[2];
        initSelectedSlot();
        loadImages();
        loadButtons();
    }

    // Init
    private void loadImages() {
        this.overlay = new Rectangle2D.Double(SHOP_OVERLAY_X, SHOP_OVERLAY_Y, SHOP_OVERLAY_WID, SHOP_OVERLAY_HEI);
        this.shopText = Utils.getInstance().importImage(SHOP_TXT, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(BUY_BTN_X, BUY_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.BUY);
        buttons[1] = new MediumButton(LEAVE_BTN_X, LEAVE_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.LEAVE);
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_X;
        int yPos = (slotNumber / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        renderOverlay(g);
        renderButtons(g);
        renderSlots(g);
        renderItems(g);
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

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.render(g));
    }

    private void renderItems(Graphics g) {
        for (Shop shop : shops) {
            if (shop.isActive()) {
                for (ShopItem item : shop.getShopItems()) {
                    if (item.getAmount() > 0) {
                        renderItem(g, item);
                        g.setColor(Color.RED);
                    }
                }
            }
        }
    }

    private void renderItem(Graphics g, ShopItem item) {
        int xPos = (item.getSlot() % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_X + ITEM_OFFSET_X;
        int yPos = (item.getSlot() / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_Y + ITEM_OFFSET_Y;
        g.setColor(item.getItemType().getRarity().getColor());
        g.fillRect(xPos-(int)(ITEM_OFFSET_X/1.1), yPos-(int)(ITEM_OFFSET_Y/1.1), (int)(SLOT_SIZE/1.06), (int)(SLOT_SIZE/1.06));
        g.drawImage(item.getItemImage(), xPos, yPos, ITEM_SIZE, ITEM_SIZE, null);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        int countX = xPos + ITEM_COUNT_OFFSET_X, countY =  yPos + ITEM_COUNT_OFFSET_Y;
        g.drawString(String.valueOf(item.getAmount()), countX, countY);
        if (slotNumber == item.getSlot()) {
            g.drawString("Cost: "+item.getCost(), COST_TEXT_X, COST_TEXT_Y);
        }
    }

    private void renderSlots(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
                g.drawImage(slotImage, i* SLOT_SPACING + SLOT_X, j* SLOT_SPACING + SLOT_Y, slotImage.getWidth(), slotImage.getHeight(), null);
            }
        }
    }

    // Other
    private void setSelectedSlot() {
        this.selectedSlot.x = (slotNumber % SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_X;
        this.selectedSlot.y = (slotNumber / SHOP_SLOT_MAX_ROW) * SLOT_SPACING + SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < SHOP_SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SHOP_SLOT_MAX_COL; j++) {
                if (x >= i*SLOT_SPACING+SLOT_X && x <= i*SLOT_SPACING+SLOT_X+SLOT_SIZE && y >= j*SLOT_SPACING+SLOT_Y && y <= j*SLOT_SPACING+SLOT_Y+SLOT_SIZE) {
                    slotNumber = i + (j* SHOP_SLOT_MAX_ROW);
                    setSelectedSlot();
                    break;
                }
            }
        }
    }

    private void buyItem() {
        shops.stream()
                .filter(Shop::isActive)
                .forEach(shop -> shop.buyItem(gameState.getPlayer(), slotNumber));
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));

        changeSlot(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY:
                        buyItem();
                        break;
                    case LEAVE:
                        gameState.setOverlay(null);
                        break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(buttons).forEach(AbstractButton::resetMouseSet);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(mediumButton -> isMouseInButton(e, mediumButton))
                .findFirst()
                .ifPresent(mediumButton -> mediumButton.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public void reset() {
        this.shops = gameState.getObjectManager().getObjects(Shop.class);
    }

}
