package platformer.ui.overlays;

import platformer.model.perks.Perk;
import platformer.state.types.GameState;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.overlays.controller.BlacksmithViewController;
import platformer.utils.ImageUtils;

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

    private final BlacksmithViewController controller;
    private final GameState gameState;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private final int SLOT_MAX_ROW, SLOT_MAX_COL;
    private final int[][] placeHolders;

    public BlacksmithOverlay(GameState gameState) {
        this.gameState = gameState;
        this.controller = new BlacksmithViewController(gameState, this);
        this.buttons = new MediumButton[2];
        this.SLOT_MAX_COL = PERK_SLOT_MAX_COL;
        this.SLOT_MAX_ROW = PERK_SLOT_MAX_ROW;
        this.placeHolders = gameState.getPerksManager().getPlaceHolders();
        init();
    }

    private void init() {
        loadImages();
        loadButtons();
        initSelectedSlot();
    }

    // Init
    private void loadImages() {
        this.overlay = new Rectangle2D.Double(PERKS_OVERLAY_X, PERKS_OVERLAY_Y, PERKS_OVERLAY_WID, PERKS_OVERLAY_HEI);
        this.shopText = ImageUtils.importImage(PERKS_TXT_IMG, SHOP_TEXT_WID, SHOP_TEXT_HEI);
        this.slotImage = ImageUtils.importImage(SLOT_IMG, SLOT_SIZE, SLOT_SIZE);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(BUY_PERK_BTN_X, BUY_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.BUY);
        buttons[1] = new MediumButton(LEAVE_PERK_BTN_X, LEAVE_PERK_BTN_Y, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, ButtonType.LEAVE);
    }

    private void initSelectedSlot() {
        int xPos = (controller.getSlotNumber() % SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X;
        int yPos = (controller.getSlotNumber() / SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y;
        this.selectedSlot = new Rectangle2D.Double(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
    }

    public void updateSelectedSlot() {
        this.selectedSlot.x = (controller.getSlotNumber() % SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X;
        this.selectedSlot.y = (controller.getSlotNumber() / SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y;
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
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
            if (controller.getSlotNumber() == perk.getSlot()) {
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

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

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

    }

    @Override
    public void reset() {

    }

    public int[][] getPlaceHolders() {
        return placeHolders;
    }

    public MediumButton[] getButtons() {
        return buttons;
    }
}
