package platformer.state;

import platformer.core.Framework;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.UI.*;
import static platformer.constants.UI.CONTINUE_BTN_Y;

public class ChoseGameState extends AbstractState implements State {

    private SmallButton playBtn, exitBtn;

    public ChoseGameState(Game game) {
        super(game);
        loadButtons();
    }

    // Init
    private void loadButtons() {
        this.playBtn = new SmallButton(CONTINUE_BTN_X, CONTINUE_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.CONTINUE);
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        playBtn.update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        renderSlots(g);
        playBtn.render(g);
        exitBtn.render(g);
    }

    private void renderSlots(Graphics g) {
        Framework.getInstance().getSaveController().getGameSlots().forEach(s -> s.render(g));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else if (isMouseInButton(e, playBtn)) playBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        else if(isMouseInButton(e, playBtn) && playBtn.isMousePressed()) {
            Framework.getInstance().getSaveController().loadSlot();
        }
        Framework.getInstance().getSaveController().checkSlotSelection(e);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        playBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
        else if (isMouseInButton(e, playBtn)) playBtn.setMouseOver(true);
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
        playBtn.resetMouseSet();
        exitBtn.resetMouseSet();
    }

}
