package platformer.state.types;

import platformer.core.Game;
import platformer.state.AbstractState;
import platformer.state.State;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.buttons.UIButtonRenderer;
import platformer.ui.options.ControlsPanel;
import platformer.ui.overlays.OverlayLayer;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import static platformer.constants.Constants.*;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the controls.
 * It includes displaying the controls to the user and handling user interactions within this state.
 */
public class ControlsState extends AbstractState implements State {

    private SmallButton exitBtn;
    private final ControlsPanel controlsPanel;

    public ControlsState(Game game) {
        super(game);
        this.controlsPanel = new ControlsPanel(this.getClass());
        loadButtons();
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
        controlsPanel.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        UIButtonRenderer.drawRunicHeader(g, "CONTROLS", OVERLAY_X + OVERLAY_WID / 2, CONTROLS_TXT_Y);
        exitBtn.render(g);
        controlsPanel.render(g);
    }

    @Override
    public void enter() {

    }

    @Override
    public void exit() {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else controlsPanel.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) game.startMenuState();
        else controlsPanel.mouseReleased(e);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(isMouseInButton(e, exitBtn));
        controlsPanel.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) game.startMenuState();
        else controlsPanel.keyPressed(e);
    }

    @Override
    public void reset() {
        exitBtn.resetMouseSet();
        controlsPanel.reset();
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void windowFocusLost(WindowEvent e) {

    }

}
