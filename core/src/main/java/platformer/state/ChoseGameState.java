package platformer.state;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.LOAD_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is choosing a game slot to play.
 * It includes displaying the game slots to the user and handling user interactions within this state.
 */
public class ChoseGameState extends AbstractState implements State {

    private BufferedImage loadText;
    private final MediumButton[] buttons;

    public ChoseGameState(Game game) {
        super(game);
        this.buttons = new MediumButton[3];
        loadImages();
        loadButtons();
    }

    // Init
    private void loadImages() {
        this.loadText = ImageUtils.importImage(LOAD_TXT, SAVE_LOAD_TEXT_WID, SAVE_LOAD_TEXT_HEI);
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(LOAD_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.LOAD);
        buttons[1] = new MediumButton(DELETE_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.DELETE);
        buttons[2] = new MediumButton(CLOSE_SAVE_BTN_X, LOAD_BTN_Y, TINY_BTN_WID, TINY_BTN_HEI, ButtonType.CLOSE);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(loadText, SAVE_LOAD_TEXT_X, SAVE_LOAD_TEXT_Y, loadText.getWidth(), loadText.getHeight(), null);
        renderSlots(g);
        Arrays.stream(buttons).forEach(b -> b.render(g));
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    private void renderSlots(Graphics g) {
        Framework.getInstance().getSaveController().getGameSlots().forEach(s -> s.render(g));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case LOAD:
                        Framework.getInstance().getSaveController().loadSlot();
                        break;
                    case DELETE:
                        Framework.getInstance().getSaveController().deleteSlot();
                        break;
                    case CLOSE:
                        game.startMenuState();
                        break;
                    default: break;
                }
            }
        }
        Framework.getInstance().getSaveController().checkSlotSelection(e);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
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
    }

}
