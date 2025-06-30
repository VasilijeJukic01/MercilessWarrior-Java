package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.options.ControlsPanel;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.CONTROLS_TXT;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is viewing the controls.
 * It includes displaying the controls to the user and handling user interactions within this state.
 */
public class ControlsState extends AbstractState implements State {

    private BufferedImage controlsText;
    private SmallButton exitBtn;
    private final ControlsPanel controlsPanel;

    public ControlsState(Game game) {
        super(game);
        this.controlsPanel = new ControlsPanel(this.getClass());
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.controlsText = Utils.getInstance().importImage(CONTROLS_TXT, CONTROLS_TXT_WID, CONTROLS_TXT_HEI);
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
        g.drawImage(controlsText, CONTROLS_TXT_X, CONTROLS_TXT_Y, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        controlsPanel.render(g);
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
