package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.perks.Perk;
import platformer.state.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.PERKS_TXT_IMG;
import static platformer.constants.FilePaths.SLOT_IMG;
import static platformer.constants.UI.*;

/**
 * BlacksmithOverlay is a class that is responsible for rendering the blacksmith overlay.
 * The blacksmith overlay is a screen that allows the player to interact with the blacksmith mechanics.
 */
public class BlacksmithOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private int slotNumber;
    private int SLOT_MAX_ROW, SLOT_MAX_COL;
    private int[][] placeHolders;

    public BlacksmithOverlay(GameState gameState) {
        this.gameState = gameState;
        this.buttons = new MediumButton[2];
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
        this.overlay = new Rectangle2D.Double(PERKS_OVERLAY_X, PERKS_OVERLAY_Y, PERKS_OVERLAY_WID, PERKS_OVERLAY_HEI);
        this.shopText = Utils.getInstance().importImage(PERKS_TXT_IMG, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = Utils.getInstance().importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(BUY_PERK_BTN_X, BUY_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.BUY);
        buttons[1] = new MediumButton(LEAVE_PERK_BTN_X, LEAVE_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.LEAVE);
    }

    private void initSelectedSlot() {
        int xPos = (slotNumber % SLOT_MAX_ROW) * PERK_SLOT_SPACING + PERK_SLOT_X;
        int yPos = (slotNumber / SLOT_MAX_ROW) * PERK_SLOT_SPACING + PERK_SLOT_Y;
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
        renderPerks(g);
        renderPerkInfo(g);
        g.setColor(Color.RED);
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
        g.drawImage(shopText, PERKS_TEXT_X, PERKS_TEXT_Y, shopText.getWidth(), shopText.getHeight(), null);
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.render(g));
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
                g.setColor(PERK_SLOT_LOCK_COL);
                renderPerkOverlay(g, p);
            }
            else if (p.isUpgraded()) {
                g.setColor(PERK_SLOT_UPGRADE_COL);
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
        int offset = slotNumber / SLOT_MAX_COL;
        this.selectedSlot.y = offset * PERK_SLOT_SPACING + PERK_SLOT_Y;
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
        for (Perk perk : gameState.getPerksManager().getPerks()) {
            if (slotNumber == perk.getSlot() && perk.isUpgraded()) return;
        }
        if (!checkTokens()) return;
        Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
        gameState.getPerksManager().upgrade(SLOT_MAX_COL, SLOT_MAX_ROW, slotNumber);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

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
                        upgrade();
                        break;
                    case LEAVE:
                        gameState.setOverlay(null);
                        break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(buttons).forEach(MediumButton::resetMouseSet);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(buttons).forEach(mediumButton -> mediumButton.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(mediumButton -> isMouseInButton(e, mediumButton))
                .findFirst()
                .ifPresent(mediumButton -> mediumButton.setMouseOver(true));
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void reset() {

    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }
}
