package platformer.ui.overlays;

import platformer.core.Framework;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.UIButtonRenderer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * This class manages the save game overlay in the game.
 * It lets the player interact with the save game mechanics.
 */
public class SaveGameOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final MediumButton[] buttons;

    public SaveGameOverlay() {
        this.buttons = new MediumButton[2];
        loadButtons();
    }

    private void loadButtons() {
        buttons[0] = new MediumButton(SAVE_BTN_X, SAVE_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.SAVE);
        buttons[1] = new MediumButton(SAVE_CLOSE_BTN_X, SAVE_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.CLOSE);
    }

    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        OverlayLayer.getInstance().renderOverlay(g);
        UIButtonRenderer.drawRunicHeader(g, "SAVE GAME", OVERLAY_X + OVERLAY_WID / 2, SAVE_LOAD_TEXT_Y);
        Framework.getInstance().getSaveController().getGameSlots().forEach(s -> s.render(g));
        Arrays.stream(buttons).forEach(b -> b.render(g));
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
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case SAVE:
                        Framework.getInstance().getSaveController().saveSlot();
                        break;
                    case CLOSE:
                        EventBus.getInstance().publish(new OverlayChangeEvent(null));
                        break;
                }
            }
            button.setMousePressed(false);
        }
        Framework.getInstance().getSaveController().checkSlotSelection(e);
        Arrays.stream(buttons).forEach(AbstractButton::resetMouseSet);
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
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {

    }

}
