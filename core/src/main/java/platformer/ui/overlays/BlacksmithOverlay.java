package platformer.ui.overlays;

import platformer.core.GameContext;
import platformer.model.perks.Perk;
import platformer.model.perks.PerksManager;
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
    private final PerksManager perksManager;

    private Rectangle2D overlay;
    private BufferedImage shopText;
    private final MediumButton[] buttons;

    private BufferedImage slotImage;
    private Rectangle2D.Double selectedSlot;
    private final int SLOT_MAX_ROW, SLOT_MAX_COL;

    private final int[][] placeHolders = {
            {1, 0, 1, 0, 1, 0, 1},
            {1, 0, 1, 0, 1, 0, 1},
            {0, 1, 1, 1, 0, 1, 1},
            {0, 1, 0, 1, 0, 0, 1}
    };

    public BlacksmithOverlay(GameContext context) {
        this.perksManager = context.getPerksManager();
        this.gameState = context.getGameState();
        this.controller = new BlacksmithViewController(context, this);
        this.buttons = new MediumButton[2];
        this.SLOT_MAX_ROW = PERK_SLOT_MAX_ROW;
        this.SLOT_MAX_COL = PERK_SLOT_MAX_COL;
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
        renderSlotsAndConnections(g);
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

    private void renderSlotsAndConnections(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke(2));
        g2d.setColor(Color.RED);

        for (int i = 0; i < SLOT_MAX_ROW; i++) {
            for (int j = 0; j < SLOT_MAX_COL; j++) {
                if (placeHolders[i][j] == 1) {
                    int xPos = j * PERK_SLOT_SPACING + PERK_SLOT_X;
                    int yPos = i * PERK_SLOT_SPACING + PERK_SLOT_Y;
                    g.drawImage(slotImage, xPos, yPos, slotImage.getWidth(), slotImage.getHeight(), null);

                    // Check connection to the right
                    if (j + 1 < SLOT_MAX_COL && placeHolders[i][j+1] == 1) {
                        int startX = xPos + SLOT_SIZE;
                        int startY = yPos + SLOT_SIZE / 2;
                        int endX = startX + PERK_SLOT_SPACING - SLOT_SIZE;
                        g2d.drawLine(startX, startY, endX, startY);
                    }
                    // Check connection downwards
                    if (i + 1 < SLOT_MAX_ROW && placeHolders[i+1][j] == 1) {
                        int startX = xPos + SLOT_SIZE / 2;
                        int startY = yPos + SLOT_SIZE;
                        int endY = startY + PERK_SLOT_SPACING - SLOT_SIZE;
                        g2d.drawLine(startX, startY, startX, endY);
                    }
                }
            }
        }
    }

    private void renderPerks(Graphics g) {
        for (Perk p : perksManager.getPerks()) {
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
        for (Perk perk : perksManager.getPerks()) {
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
        controller.keyPressed(e);
    }

    @Override
    public void reset() {

    }

    public MediumButton[] getButtons() {
        return buttons;
    }
}