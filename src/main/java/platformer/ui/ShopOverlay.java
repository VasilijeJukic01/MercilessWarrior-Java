package platformer.ui;

import platformer.model.Tiles;
import platformer.model.objects.Shop;
import platformer.state.PlayingState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.ShopButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ShopOverlay {

    private final PlayingState playingState;

    private BufferedImage overlay;
    private BufferedImage shopText;
    private final ShopButton[] buttons;

    private BufferedImage slotImage;
    private final Rectangle2D.Double selectedSlot;
    private final int SLOT_MAX_ROW = 7, SLOT_MAX_COL = 3;
    private int slot;

    private ArrayList<Shop> shops;

    // Size Variables [Init]
    private final int overlayWid = (int)(400* Tiles.SCALE.getValue());
    private final int overlayHei = (int)(340*Tiles.SCALE.getValue());
    private final int shopTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int shopTextHei = (int)(60*Tiles.SCALE.getValue());
    private final int slotWid = (int)(40*Tiles.SCALE.getValue());
    private final int slotHei = (int)(40*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int overlayX = (int)(220*Tiles.SCALE.getValue());
    private final int overlayY = (int)(50*Tiles.SCALE.getValue());
    private final int shopTextX = (int)(330*Tiles.SCALE.getValue());
    private final int shopTextY = (int)(80*Tiles.SCALE.getValue());
    private final int buyBtnX = (int)(300*Tiles.SCALE.getValue());
    private final int buyBtnY = (int)(330*Tiles.SCALE.getValue());
    private final int exitBtnX = (int)(440*Tiles.SCALE.getValue());
    private final int exitBtnY = (int)(330*Tiles.SCALE.getValue());
    private final int slotX = (int)(290*Tiles.SCALE.getValue());
    private final int slotY = (int)(160*Tiles.SCALE.getValue());
    private final int costX = (int)(530*Tiles.SCALE.getValue());
    private final int costY = (int)(145*Tiles.SCALE.getValue());

    private final int slotSpacing = (int)(40*Tiles.SCALE.getValue());

    public ShopOverlay(PlayingState playingState) {
        this.playingState = playingState;
        this.shops = playingState.getLevelManager().getCurrentLevel().getShops();
        this.buttons = new ShopButton[2];
        this.selectedSlot = new Rectangle2D.Double((slot%SLOT_MAX_ROW)*slotSpacing+slotX, (slot/SLOT_MAX_ROW)*slotSpacing+slotY, slotWid, slotHei);
        init();
    }

    private void init() {
        this.overlay = Utils.instance.importImage("src/main/resources/images/overlay1.png", overlayWid, overlayHei);
        this.shopText = Utils.instance.importImage("src/main/resources/images/buttons/ShopText.png", shopTextWid, shopTextHei);
        this.slotImage = Utils.instance.importImage("src/main/resources/images/shop/Slot.png", slotWid, slotHei);
        buttons[0] = new ShopButton(buyBtnX, buyBtnY, ButtonType.BUY);
        buttons[1] = new ShopButton(exitBtnX, exitBtnY, ButtonType.LEAVE);
    }

    public void update() {
        for (ShopButton button : buttons) {
            button.update();
        }
    }

    public void render(Graphics g) {
        g.drawImage(overlay, overlayX, overlayY, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(shopText, shopTextX, shopTextY, shopText.getWidth(), shopText.getHeight(), null);
        for (ShopButton button : buttons) {
            button.render(g);
        }
        g.setColor(Color.RED);
        renderSlots(g);
        renderItems(g);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    private void renderItems(Graphics g) {
        for (Shop shop : shops) {
            if (shop.isActive()) {
                for (ShopItem item : shop.getShopItems()) {
                    if (item.getAmount() > 0) {
                        int xPos = (item.getSlot()%SLOT_MAX_ROW)*slotSpacing+slotX;
                        int yPos = (item.getSlot()/SLOT_MAX_ROW)*slotSpacing+slotY;
                        int wid = (int)(slotImage.getWidth()/1.75), hei = (int)(slotImage.getHeight()/1.75);
                        g.drawImage(item.getItemImage(), xPos+(int)(wid/2.5), yPos+(int)(hei/2.5), wid, hei, null);
                        g.setColor(Color.WHITE);
                        g.setFont(new Font("Arial", Font.BOLD, 20));
                        int countX = (int)(xPos+slotWid/1.5), countY = (int)(yPos+slotHei/1.2);
                        g.drawString(String.valueOf(item.getAmount()), countX, countY);
                        if (slot == item.getSlot()) {
                            g.drawString("Cost: "+item.getCost(), costX, costY);
                        }
                        g.setColor(Color.RED);
                    }
                }
            }
        }
    }

    private void renderSlots(Graphics g) {
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                g.drawImage(slotImage, i*slotSpacing+slotX, j*slotSpacing+slotY, slotImage.getWidth(), slotImage.getHeight(), null);
            }
        }
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                if (x >= i*slotSpacing+slotX && x <= i*slotSpacing+slotX+slotWid && y >= j*slotSpacing+slotY && y <= j*slotSpacing+slotY+slotHei) {
                    slot = i + (j*SLOT_MAX_ROW);
                    setSelectedSlot();
                    break;
                }
            }
        }
    }

    public void mousePressed(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMousePressed(true);
                break;
            }
        }
        changeSlot(e);
    }

    private void buyItem() {
        for (Shop shop : shops) {
            if (shop.isActive()) {
                shop.buyItem(playingState.getPlayer(), slot);
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY:
                        buyItem();
                        break;
                    case LEAVE:
                        playingState.setShopVisible(false);
                        break;
                    default: break;
                }
                break;
            }
        }
        for (ShopButton button : buttons) {
            button.resetMouseSet();
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (ShopButton button : buttons) {
            button.setMouseOver(false);
        }
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMouseOver(true);
                break;
            }
        }
    }

    private boolean isMouseInButton(MouseEvent e, ShopButton shopButton) {
        return shopButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void setSelectedSlot() {
        this.selectedSlot.x = (slot%SLOT_MAX_ROW)*slotSpacing+slotX;
        this.selectedSlot.y = (slot/SLOT_MAX_ROW)*slotSpacing+slotY;
    }

    public void reset() {
        this.shops = playingState.getLevelManager().getCurrentLevel().getShops();
    }
}
