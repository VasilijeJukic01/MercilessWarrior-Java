package platformer.ui.overlays;

import platformer.model.perks.Perk;
import platformer.state.PlayingState;
import platformer.ui.MouseControls;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.ShopButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class BlacksmithOverlay implements MouseControls {

    private final PlayingState playingState;

    private BufferedImage overlay;
    private BufferedImage shopText;
    private final ShopButton[] buttons;

    private BufferedImage slotImage;
    private int SLOT_MAX_ROW, SLOT_MAX_COL;
    private Rectangle2D.Double selectedSlot;
    private int slot;
    private int[][] placeHolders;

    // Size Variables [Init]
    private final int overlayWid = (int)(700*SCALE);
    private final int overlayHei = (int)(410*SCALE);
    private final int shopTextWid = (int)(180*SCALE);
    private final int shopTextHei = (int)(60*SCALE);
    private final int slotWid = (int)(40*SCALE);
    private final int slotHei = (int)(40*SCALE);

    // Size Variables [Render]
    private final int overlayX = (int)(65*SCALE);
    private final int overlayY = (int)(30*SCALE);
    private final int shopTextX = (int)(330*SCALE);
    private final int shopTextY = (int)(60*SCALE);
    private final int buyBtnX = (int)(300*SCALE);
    private final int buyBtnY = (int)(380*SCALE);
    private final int exitBtnX = (int)(440*SCALE);
    private final int exitBtnY = (int)(380*SCALE);
    private final int slotX = (int)(110*SCALE);
    private final int slotY = (int)(140*SCALE);
    private final int tokensX = (int)(550*SCALE);
    private final int tokensY = (int)(150*SCALE);
    private final int perkNameX = (int)(550*SCALE);
    private final int perkNameY = (int)(240*SCALE);
    private final int perkCostX = (int)(550*SCALE);
    private final int perkCostY = (int)(255*SCALE);
    private final int perkDescX = (int)(550*SCALE);
    private final int perkDescY = (int)(275*SCALE);;

    private final int slotSpacing = (int)(60*SCALE);

    public BlacksmithOverlay(PlayingState playingState) {
        this.playingState = playingState;
        this.buttons = new ShopButton[2];
        init();
    }

    private void init() {
        this.SLOT_MAX_COL = playingState.getPerksManager().getSlotMaxCol();
        this.SLOT_MAX_ROW = playingState.getPerksManager().getSlotMaxRow();
        this.placeHolders = playingState.getPerksManager().getPlaceHolders();
        this.overlay = Utils.instance.importImage("/images/overlay1.png", overlayWid, overlayHei);
        this.shopText = Utils.instance.importImage("/images/buttons/PerksText.png", shopTextWid, shopTextHei);
        this.slotImage = Utils.instance.importImage("/images/shop/Slot.png", slotWid, slotHei);
        buttons[0] = new ShopButton(buyBtnX, buyBtnY, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.BUY);
        buttons[1] = new ShopButton(exitBtnX, exitBtnY, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.LEAVE);
        this.selectedSlot = new Rectangle2D.Double((slot%SLOT_MAX_ROW)*slotSpacing+slotX, (slot/SLOT_MAX_ROW)*slotSpacing+slotY, slotWid, slotHei);
    }

    // Core
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
        renderSlots(g);
        renderPerks(g);
        renderPerkInfo(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    // Render
    private boolean isSafe(int i, int j, int n, int m) {
        return i >= 0 && j >= 0 && i < n && j < m;
    }

    private void renderSlots(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                if (placeHolders[i][j] == 1) {
                    g.drawImage(slotImage, j * slotSpacing + slotX, i * slotSpacing + slotY, slotImage.getWidth(), slotImage.getHeight(), null);
                    if (isSafe(i, j+1, SLOT_MAX_ROW, SLOT_MAX_COL) && placeHolders[i][j+1] == 1) {
                        int x = j * slotSpacing + slotX + slotWid - (int)(2*SCALE);
                        int y = i * slotSpacing + slotY;
                        g.drawLine(x, y + slotHei/2, x+slotSpacing/2, y + slotHei/2);
                    }
                    if (isSafe(i+1, j, SLOT_MAX_ROW, SLOT_MAX_COL) && placeHolders[i+1][j] == 1) {
                        int x = j * slotSpacing + slotX + slotWid/2;
                        int y = i * slotSpacing + slotY + slotHei - (int)(2*SCALE);
                        g.drawLine(x, y, x, y + slotSpacing/2);
                    }
                }
            }
        }
    }

    private void renderPerks(Graphics g) {
        for (Perk p : playingState.getPerksManager().getPerks()) {
            int x = (p.getSlot()%SLOT_MAX_COL)*slotSpacing+slotX + slotWid/4;
            int y = (p.getSlot()/SLOT_MAX_COL)*slotSpacing+slotY + slotHei/4;
            g.drawImage(p.getImage(), x, y, slotWid/2, slotHei/2, null);
            if (p.isLocked()) {
                g.setColor(new Color(0, 0, 0, 200));
                g.fillRect((p.getSlot()%SLOT_MAX_COL)*slotSpacing+slotX, (p.getSlot()/SLOT_MAX_COL)*slotSpacing+slotY, slotWid, slotHei);
            }
            else if (p.isUpgraded()) {
                g.setColor(new Color(255, 100, 0, 100));
                g.fillRect((p.getSlot()%SLOT_MAX_COL)*slotSpacing+slotX, (p.getSlot()/SLOT_MAX_COL)*slotSpacing+slotY, slotWid, slotHei);
            }
        }
    }

    private void renderPerkInfo(Graphics g) {
        for (Perk perk : playingState.getPerksManager().getPerks()) {
            if (slot == perk.getSlot()) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, (int)(10*SCALE)));
                g.drawString("Tokens: "+playingState.getPlayer().getUpgradeTokens(), tokensX, tokensY);
                g.drawString(perk.getName(), perkNameX, perkNameY);
                g.drawString("Cost: "+perk.getCost(), perkCostX, perkCostY);
                g.setFont(new Font("Arial", Font.PLAIN, (int)(10*SCALE)));
                g.drawString(perk.getDescription(), perkDescX, perkDescY);
            }
        }
    }

    // Other
    private void setSelectedSlot() {
        this.selectedSlot.x = (slot%SLOT_MAX_COL)*slotSpacing+slotX;
        this.selectedSlot.y = (slot/SLOT_MAX_COL)*slotSpacing+slotY;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < SLOT_MAX_COL; i++) {
            for (int j = 0; j < SLOT_MAX_ROW; j++) {
                if (x >= i*slotSpacing+slotX && x <= i*slotSpacing+slotX+slotWid && y >= j*slotSpacing+slotY && y <= j*slotSpacing+slotY+slotHei) {
                    slot = i + (j*SLOT_MAX_COL);
                    if (placeHolders[j][i] == 1)
                        setSelectedSlot();
                    break;
                }
            }
        }
    }

    private boolean checkTokens() {
        for (Perk perk : playingState.getPerksManager().getPerks()) {
            if (slot == perk.getSlot() && playingState.getPlayer().getUpgradeTokens() >= perk.getCost()) {
                playingState.getPlayer().changeUpgradeTokens(-perk.getCost());
                return true;
            }
        }
        return false;
    }

    public void upgrade() {
        if (!checkTokens()) return;
        playingState.getPerksManager().upgrade(placeHolders, SLOT_MAX_COL, SLOT_MAX_ROW, slot);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMousePressed(true);
                break;
            }
        }
        changeSlot(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY:
                        upgrade();
                        break;
                    case LEAVE:
                        playingState.setBmVisible(false);
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

    @Override
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
}
