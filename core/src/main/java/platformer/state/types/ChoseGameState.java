package platformer.state.types;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.state.AbstractState;
import platformer.state.State;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.overlays.OverlayLayer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is choosing a game slot to play.
 * It includes displaying the game slots to the user and handling user interactions within this state.
 */
public class ChoseGameState extends AbstractState implements State {

    private final MediumButton[] buttons = new MediumButton[3];
    private final MediumButton[] confirmButtons = new MediumButton[2];
    private Rectangle2D.Double confirmDialogBox;
    private boolean confirmingDelete = false;

    public ChoseGameState(Game game) {
        super(game);
        loadButtons();
    }

    // Init
    private void loadButtons() {
        buttons[0] = new MediumButton(LOAD_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.LOAD);
        buttons[1] = new MediumButton(DELETE_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.DELETE);
        buttons[2] = new MediumButton(CLOSE_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CLOSE);
        initDialogue();
    }

    void initDialogue() {
        int dialogW = scale(220), dialogH = scale(115);
        int dialogX = (GAME_WIDTH - dialogW) / 2, dialogY = (GAME_HEIGHT - dialogH) / 2;
        this.confirmDialogBox = new Rectangle2D.Double(dialogX, dialogY, dialogW, dialogH);

        int btnY = dialogY + dialogH - TINY_BTN_HEI - scale(14);
        int gap = scale(18);
        int totalBtnsW = TINY_BTN_WID * 2 + gap;
        int startBtnX = dialogX + (dialogW - totalBtnsW) / 2;

        confirmButtons[0] = new MediumButton(startBtnX, btnY, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.DELETE);
        confirmButtons[1] = new MediumButton(startBtnX + TINY_BTN_WID + gap, btnY, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CLOSE);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        if (confirmingDelete) {
            Arrays.stream(confirmButtons).forEach(MediumButton::update);
        } else {
            Arrays.stream(buttons).forEach(MediumButton::update);
        }
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        UIButtonRenderer.drawRunicHeader(g, "LOAD GAME", OVERLAY_X + OVERLAY_WID / 2, SAVE_LOAD_TEXT_Y);
        renderSlots(g);
        Arrays.stream(buttons).forEach(b -> b.render(g));
        if (confirmingDelete) renderConfirmDialog(g);
    }

    private void renderSlots(Graphics g) {
        Framework.getInstance().getSaveController().getGameSlots().forEach(s -> s.render(g));
    }

    private void renderConfirmDialog(Graphics g) {
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);

        UIButtonRenderer.drawRunicPanel(g, confirmDialogBox);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int centerX = (int) confirmDialogBox.getCenterX();
        int topY = (int) confirmDialogBox.y;

        g2d.setFont(UIButtonRenderer.getFont(13f * SCALE));
        g2d.setColor(new Color(255, 105, 105));
        String title = "DELETE SAVE SLOT?";
        FontMetrics fmTitle = g2d.getFontMetrics();
        g2d.drawString(title, centerX - fmTitle.stringWidth(title) / 2, topY + scale(28));

        g2d.setFont(new Font("Arial", Font.PLAIN, FONT_MEDIUM));
        g2d.setColor(new Color(200, 215, 218));
        String msg1 = "This action is permanent and";
        String msg2 = "cannot be undone.";
        FontMetrics fmBody = g2d.getFontMetrics();
        g2d.drawString(msg1, centerX - fmBody.stringWidth(msg1) / 2, topY + scale(48));
        g2d.drawString(msg2, centerX - fmBody.stringWidth(msg2) / 2, topY + scale(62));

        g2d.dispose();

        Arrays.stream(confirmButtons).forEach(b -> b.render(g));
    }

    @Override
    public void enter() {
        confirmingDelete = false;
    }

    @Override
    public void exit() {
        confirmingDelete = false;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        MediumButton[] activeBtns = confirmingDelete ? confirmButtons : buttons;
        Arrays.stream(activeBtns)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (confirmingDelete) {
            for (MediumButton button : confirmButtons) {
                if (isMouseInButton(e, button) && button.isMousePressed()) {
                    if (button.getButtonType() == ButtonType.DELETE) {
                        Framework.getInstance().getSaveController().deleteSlot();
                        confirmingDelete = false;
                    }
                    else if (button.getButtonType() == ButtonType.CLOSE) {
                        confirmingDelete = false;
                    }
                    break;
                }
            }
            reset();
            return;
        }

        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case LOAD -> Framework.getInstance().getSaveController().loadSlot();
                    case DELETE -> {
                        if (Framework.getInstance().getSaveController().canDeleteSelectedSlot()) {
                            confirmingDelete = true;
                        }
                    }
                    case CLOSE -> game.startMenuState();
                    default -> {}
                }
                reset();
                return;
            }
        }
        Framework.getInstance().getSaveController().checkSlotSelection(e);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        MediumButton[] activeBtns = confirmingDelete ? confirmButtons : buttons;
        Arrays.stream(activeBtns).forEach(button -> button.setMouseOver(false));
        Arrays.stream(activeBtns)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (confirmingDelete) {
                confirmingDelete = false;
                reset();
            }
            else game.startMenuState();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

    @Override
    public void reset() {
        Arrays.stream(buttons).forEach(MediumButton::resetMouseSet);
        Arrays.stream(confirmButtons).forEach(MediumButton::resetMouseSet);
    }
}
