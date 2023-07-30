package platformer.state;

import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.buttons.PauseButton;
import platformer.ui.overlays.Overlay;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.Constants.SCALE;

public class ControlsState extends StateAbstraction implements State {

    private BufferedImage keyboardSprite;
    private final int wKey = (int)(20*SCALE), hKey = (int)(20*SCALE);
    private BufferedImage controlsText;

    private CREButton exitBtn;

    // Size Variables [Init]
    private final int controlsTextWid = (int)(180*SCALE);
    private final int controlsTextHei = (int)(40*SCALE);

    private final int exitBtnX = (int)(480*SCALE);
    private final int exitBtnY = (int)(350*SCALE);

    // Size Variables [Render]
    private final int controlsTextX = (int)(330*SCALE);
    private final int controlsTextY = (int)(85*SCALE);


    public ControlsState(Game game) {
        super(game);
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.controlsText = Utils.getInstance().importImage("/images/buttons/ControlsText.png", controlsTextWid, controlsTextHei);
        this.keyboardSprite = Utils.getInstance().importImage("/images/keyboard.png", -1, -1);
    }

    private void loadButtons() {
        this.exitBtn = new CREButton(exitBtnX, exitBtnY, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        Overlay.getInstance().update();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        Overlay.getInstance().render(g);
        g.drawImage(controlsText, controlsTextX, controlsTextY, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        renderControls(g);
    }

    // Render
    private void renderControls(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(10*SCALE)));
        g.drawString("Move/Wall Slide:", (int)(300*SCALE), (int)(150*SCALE));
        g.drawImage(keyboardSprite.getSubimage(16*2,0, 16, 16), (int)(390*SCALE), (int)(135*SCALE), wKey, hKey, null);
        g.drawImage(keyboardSprite.getSubimage(16*3,0, 16, 16), (int)(410*SCALE), (int)(135*SCALE), wKey, hKey, null);
        g.drawString("Jump:", (int)(300*SCALE), (int)(170*SCALE));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(335*SCALE), (int)(155*SCALE), wKey, hKey, null);
        g.drawString("Attack:", (int)(300*SCALE), (int)(190*SCALE));
        g.drawImage(keyboardSprite.getSubimage(16*7,16*4, 16, 16), (int)(345*SCALE), (int)(175*SCALE), wKey, hKey, null);
        g.drawString("Flames:", (int)(300*SCALE), (int)(210*SCALE));
        g.drawImage(keyboardSprite.getSubimage(16*2,16*2, 16, 16), (int)(345*SCALE), (int)(195*SCALE), wKey, hKey, null);
        g.drawString("Block Attack:", (int)(300*SCALE), (int)(230*SCALE));
        g.drawImage(keyboardSprite.getSubimage(16*2,16*4, 16, 16), (int)(370*SCALE), (int)(215*SCALE), wKey, hKey, null);
        g.drawString("Dash:", (int)(300*SCALE), (int)(250*SCALE));
        g.drawImage(keyboardSprite.getSubimage(16*5,16*4, 16, 16), (int)(330*SCALE), (int)(235*SCALE), wKey, hKey, null);
        g.drawString("Transform:", (int)(300*SCALE), (int)(270*SCALE));
        g.drawImage(keyboardSprite.getSubimage(0,16*4, 16, 16), (int)(360*SCALE), (int)(255*SCALE), wKey, hKey, null);
        g.drawString("Double Jump:", (int)(300*SCALE), (int)(290*SCALE));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(370*SCALE), (int)(275*SCALE), wKey, hKey, null);
        g.drawString("+", (int)(390*SCALE), (int)(288*SCALE));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(398*SCALE), (int)(275*SCALE), wKey, hKey, null);
    }

    private boolean isMouseInButton(MouseEvent e, PauseButton pauseButton) {
        return pauseButton.getButtonHitBox().contains(e.getX(), e.getY());
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
    public void setPaused(boolean value) {

    }

    @Override
    public void setGameOver(boolean value) {

    }

    @Override
    public void setDying(boolean value) {

    }

    @Override
    public void reset() {
        exitBtn.setMouseOver(false);
        exitBtn.setMousePressed(false);
    }
}
