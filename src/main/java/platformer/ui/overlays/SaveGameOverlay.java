package platformer.ui.overlays;

import platformer.controller.GameSaveController;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;

import java.awt.*;
import java.awt.event.MouseEvent;

import static platformer.constants.Constants.SMALL_BTN_HEI;
import static platformer.constants.Constants.SMALL_BTN_WID;
import static platformer.constants.UI.SAVE_BTN_X;
import static platformer.constants.UI.SAVE_BTN_Y;

public class SaveGameOverlay implements Overlay {

    private MediumButton saveBtn;
    private final GameSaveController saveController;

    public SaveGameOverlay(Game game) {
        this.saveController = new GameSaveController(game);
        loadButtons();
    }

    private void loadButtons() {
        this.saveBtn = new MediumButton(SAVE_BTN_X, SAVE_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.SAVE);
    }

    @Override
    public void update() {
        saveBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().renderOverlay(g);
        saveController.getGameSlots().forEach(s -> s.render(g));
        saveBtn.render(g);
    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, saveBtn)) {
            saveBtn.setMousePressed(true);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseInButton(e, saveBtn) && saveBtn.isMousePressed()) {
            saveController.saveSlot();
        }
        saveController.checkSlotSelection(e);
        saveBtn.resetMouseSet();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        saveBtn.setMouseOver(false);
        if (isMouseInButton(e, saveBtn)) saveBtn.setMouseOver(true);
    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {

    }

}
