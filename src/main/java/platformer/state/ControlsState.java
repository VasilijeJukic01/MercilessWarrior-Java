package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.CONTROLS_TXT;
import static platformer.constants.FilePaths.KEYBOARD_SPRITE;
import static platformer.constants.UI.*;

public class ControlsState extends AbstractState implements State {

    private BufferedImage keyboardSprite;
    private BufferedImage controlsText;

    private CREButton exitBtn;

    public ControlsState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.controlsText = Utils.getInstance().importImage(CONTROLS_TXT, CONTROLS_TXT_WID, CONTROLS_TXT_HEI);
        this.keyboardSprite = Utils.getInstance().importImage(KEYBOARD_SPRITE, -1, -1);
    }

    private void loadButtons() {
        this.exitBtn = new CREButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        g.drawImage(controlsText, CONTROLS_TXT_X, CONTROLS_TXT_Y, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        renderControls(g);
    }

    // Render
    private void renderControls(Graphics g) {
        renderTexts(g);
        renderKeys(g);
    }

    private void renderTexts(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, FONT_MEDIUM));
        g.drawString("Move/Wall Slide:",    CTRL_ROW_TXT_X, CTRL_ROW1_TXT_Y);
        g.drawString("Jump:",               CTRL_ROW_TXT_X, CTRL_ROW2_TXT_Y);
        g.drawString("Attack:",             CTRL_ROW_TXT_X, CTRL_ROW3_TXT_Y);
        g.drawString("Flames:",             CTRL_ROW_TXT_X, CTRL_ROW4_TXT_Y);
        g.drawString("Block Attack:",       CTRL_ROW_TXT_X, CTRL_ROW5_TXT_Y);
        g.drawString("Dash:",               CTRL_ROW_TXT_X, CTRL_ROW6_TXT_Y);
        g.drawString("Transform:",          CTRL_ROW_TXT_X, CTRL_ROW7_TXT_Y);
        g.drawString("Double Jump:",        CTRL_ROW_TXT_X, CTRL_ROW8_TXT_Y);
    }

    private void renderKeys(Graphics g) {
        g.drawImage(keyboardSprite.getSubimage(16*2,0, 16, 16),     K1_X, K_ROW1, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(16*3,0, 16, 16),     K2_X, K_ROW1, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16),        K3_X, K_ROW2, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(16*7,16*4, 16, 16),  K4_X, K_ROW3, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(16*2,16*2, 16, 16),  K5_X, K_ROW4, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(16*2,16*4, 16, 16),  K6_X, K_ROW5, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(16*5,16*4, 16, 16),  K7_X, K_ROW6, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(0,16*4, 16, 16),     K8_X, K_ROW7, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16),        K9_X, K_ROW8, KEY_SIZE, KEY_SIZE, null);
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16),        K10_X, K_ROW8, KEY_SIZE, KEY_SIZE, null);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
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
        exitBtn.resetMouseSet();
    }

}
