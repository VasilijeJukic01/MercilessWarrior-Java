package platformer.ui.overlays;

import platformer.model.perks.Perk;
import platformer.state.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.ShopButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

public class BlacksmithOverlay implements Overlay {

    private final GameState gameState;

    private BufferedImage overlay;
    private BufferedImage shopText;
    private final ShopButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int slotNumber;
    private int SLOT_MAX_ROW, SLOT_MAX_COL;
    private int[][] placeHolders;

    public BlacksmithOverlay(GameState gameState) {
        this.gameState = gameState;
        this.buttons = new ShopButton[2];
        init();
    }

    private void init() {
        this.SLOT_MAX_COL = PERK_SLOT_MAX_COL;
        this.SLOT_MAX_ROW = PERK_SLOT_MAX_ROW;
        this.placeHolders = gameState.getPerksManager().getPlaceHolders();
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    // Init
    private void loadImages() {
        this.overlay = Utils.getInstance().importImage(OVERLAY, PERKS_OVERLAY_WID, PERKS_OVERLAY_HEI);
        this.shopText = Utils.getInstance().importImage(PERKS_TXT_IMG, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        buttons[0] = new ShopButton(BUY_PERK_BTN_X, BUY_PERK_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.BUY);
        buttons[1] = new ShopButton(LEAVE_PERK_BTN_X, LEAVE_PERK_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.LEAVE);
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % SLOT_MAX_ROW) * PERK_SLOT_SPACING + PERK_SLOT_X;
        int yPos = (slotNumber / SLOT_MAX_ROW) * PERK_SLOT_SPACING + PERK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    // Core
    @Override
    public void update() {
        for (ShopButton button : buttons) {
            button.update();
        }
    }

    @Override
    public void render(Graphics g) {
        renderImages(g);
        renderButtons(g);
        renderSlots(g);
        renderPerks(g);
        renderPerkInfo(g);
        g.setColor(Color.RED);
        g.drawRect((int)selectedSlot.x, (int)selectedSlot.y,  (int)selectedSlot.width,  (int)selectedSlot.height);
    }

    // Render
    private void renderImages(Graphics g) {
        g.drawImage(overlay, PERKS_OVERLAY_X, PERKS_OVERLAY_Y, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(shopText, PERKS_TEXT_X, PERKS_TEXT_Y, shopText.getWidth(), shopText.getHeight(), null);
    }

    private void renderButtons(Graphics g) {
        for (ShopButton button : buttons) {
            button.render(g);
        }
    }

    private void renderSlots(Graphics g) {
        g.setColor(Color.RED);
        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                if (placeHolders[i][j] == 1) {
                    renderSlot(g, i, j);
                }
            }
        }
    }

    private void renderSlot(Graphics g, int i, int j) {
        int xPos = j * PERK_SLOT_SPACING + PERK_SLOT_X;
        int yPos = i * PERK_SLOT_SPACING + PERK_SLOT_Y;
        g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);
        if (isSafe(i, j+1, SLOT_MAX_ROW, SLOT_MAX_COL) && placeHolders[i][j+1] == 1) {
            int x = j * PERK_SLOT_SPACING + PERK_SLOT_X + SLOT_SIZE - (int)(2 * SCALE);
            int y = i * PERK_SLOT_SPACING + PERK_SLOT_Y;
            g.drawLine(x, y + SLOT_SIZE/2, x + PERK_SLOT_SPACING/2, y + SLOT_SIZE/2);
        }
        if (isSafe(i+1, j, SLOT_MAX_ROW, SLOT_MAX_COL) && placeHolders[i+1][j] == 1) {
            int x = j * PERK_SLOT_SPACING + PERK_SLOT_X + SLOT_SIZE/2;
            int y = i * PERK_SLOT_SPACING + PERK_SLOT_Y + SLOT_SIZE - (int)(2 * SCALE);
            g.drawLine(x, y, x, y + PERK_SLOT_SPACING/2);
        }
    }

    private void renderPerks(Graphics g) {
        for (Perk p : gameState.getPerksManager().getPerks()) {
            int x = (p.getSlot() % SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X + SLOT_SIZE/4;
            int y = (p.getSlot() / SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y + SLOT_SIZE/4;
            g.drawImage(p.getImage(), x, y, SLOT_SIZE/2, SLOT_SIZE/2, null);
            if (p.isLocked()) {
                g.setColor(new Color(0, 0, 0, 200));
                renderPerkOverlay(g, p);
            }
            else if (p.isUpgraded()) {
                g.setColor(new Color(255, 100, 0, 100));
                renderPerkOverlay(g, p);
            }
        }
    }

    private void renderPerkOverlay(Graphics g, Perk p) {
        int xPos = (p.getSlot() % SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X;
        int yPos = (p.getSlot() / SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y;
        g.fillRect(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    private void renderPerkInfo(Graphics g) {
        for (Perk perk : gameState.getPerksManager().getPerks()) {
            if (slotNumber == perk.getSlot()) {
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
                g.drawString("Tokens: "+ gameState.getPlayer().getUpgradeTokens(), TOKENS_TEXT_X, TOKENS_TEXT_Y);
                g.drawString(perk.getName(), PERK_NAME_X, PERK_NAME_Y);
                g.drawString("Cost: "+perk.getCost(), PERK_COST_X, PERK_COST_Y);
                g.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
                g.drawString(perk.getDescription(), PERK_DESC_X, PERK_DESC_Y);
            }
        }
    }

    // Other
    private boolean isSafe(int i, int j, int n, int m) {
        return i >= 0 && j >= 0 && i < n && j < m;
    }

    private void setSelectedSlot() {
        this.selectedSlot.x = (slotNumber % SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X;
        this.selectedSlot.y = (slotNumber / SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y;
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < SLOT_MAX_COL; i++) {
            for (int j = 0; j < SLOT_MAX_ROW; j++) {
                if (x >= i*PERK_SLOT_SPACING+PERK_SLOT_X && x <= i*PERK_SLOT_SPACING+PERK_SLOT_X+SLOT_SIZE && y >= j*PERK_SLOT_SPACING+PERK_SLOT_Y && y <= j*PERK_SLOT_SPACING+PERK_SLOT_Y+SLOT_SIZE) {
                    slotNumber = i + (j*SLOT_MAX_COL);
                    if (placeHolders[j][i] == 1)
                        setSelectedSlot();
                    break;
                }
            }
        }
    }

    private boolean checkTokens() {
        for (Perk perk : gameState.getPerksManager().getPerks()) {
            if (slotNumber == perk.getSlot() && gameState.getPlayer().getUpgradeTokens() >= perk.getCost()) {
                gameState.getPlayer().changeUpgradeTokens(-perk.getCost());
                return true;
            }
        }
        return false;
    }

    public void upgrade() {
        if (!checkTokens()) return;
        gameState.getPerksManager().upgrade(SLOT_MAX_COL, SLOT_MAX_ROW, slotNumber);
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
                        gameState.setBlacksmithVisible(false);
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

    @Override
    public void reset() {

    }

    private boolean isMouseInButton(MouseEvent e, ShopButton shopButton) {
        return shopButton.getButtonHitBox().contains(e.getX(), e.getY());
    }
}
