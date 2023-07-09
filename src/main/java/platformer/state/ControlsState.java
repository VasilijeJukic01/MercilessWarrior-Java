package platformer.state;

import platformer.animation.AnimationUtils;
import platformer.core.Game;
import platformer.model.Tiles;
import platformer.ui.UI;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.buttons.PauseButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class ControlsState extends StateAbstraction implements State {

    private final BufferedImage[] background;
    private BufferedImage keyboardSprite;
    private final int wKey = (int)(20*Tiles.SCALE.getValue()), hKey = (int)(20*Tiles.SCALE.getValue());
    private BufferedImage overlay;
    private BufferedImage controlsText;
    private final int animSpeed = 20;
    private int animTick = 0, animIndex = 0;

    private CREButton nextBtn;
    private CREButton prevBtn;
    private CREButton exitBtn;

    // Size Variables [Init]
    private final int overlayWid = (int)(300* Tiles.SCALE.getValue());
    private final int overlayHei = (int)(350*Tiles.SCALE.getValue());
    private final int controlsTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int controlsTextHei = (int)(40*Tiles.SCALE.getValue());

    private final int exitBtnX = (int)(480*Tiles.SCALE.getValue());
    private final int exitBtnY = (int)(350*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int overlayX = (int)(270*Tiles.SCALE.getValue());
    private final int overlayY = (int)(50*Tiles.SCALE.getValue());
    private final int controlsTextX = (int)(330*Tiles.SCALE.getValue());
    private final int controlsTextY = (int)(85*Tiles.SCALE.getValue());


    public ControlsState(Game game) {
        super(game);
        this.background = AnimationUtils.getInstance().loadMenuAnimation();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.overlay = Utils.getInstance().importImage("src/main/resources/images/overlay1.png",overlayWid, overlayHei);
        this.controlsText = Utils.getInstance().importImage("src/main/resources/images/buttons/ControlsText.png", controlsTextWid, controlsTextHei);
        this.keyboardSprite = Utils.getInstance().importImage("src/main/resources/images/keyboard.png", -1, -1);
    }

    private void loadButtons() {
        this.exitBtn = new CREButton(exitBtnX, exitBtnY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.EXIT);
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= 24) {
                animIndex = 0;
            }
        }
    }

    @Override
    public void update() {
        updateAnimation();
        exitBtn.update();
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(background[animIndex], 0, 0,null);
        g.drawImage(overlay,  overlayX,  overlayY, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(controlsText, controlsTextX, controlsTextY, controlsText.getWidth(), controlsText.getHeight(), null);
        exitBtn.render(g);
        renderControls(g);
    }

    private void renderControls(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, (int)(10*Tiles.SCALE.getValue())));
        g.drawString("Move/Wall Slide:", (int)(300*Tiles.SCALE.getValue()), (int)(150*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(16*2,0, 16, 16), (int)(390*Tiles.SCALE.getValue()), (int)(135*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawImage(keyboardSprite.getSubimage(16*3,0, 16, 16), (int)(410*Tiles.SCALE.getValue()), (int)(135*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Jump:", (int)(300*Tiles.SCALE.getValue()), (int)(170*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(335*Tiles.SCALE.getValue()), (int)(155*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Attack:", (int)(300*Tiles.SCALE.getValue()), (int)(190*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(16*7,16*4, 16, 16), (int)(345*Tiles.SCALE.getValue()), (int)(175*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Flames:", (int)(300*Tiles.SCALE.getValue()), (int)(210*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(16*2,16*2, 16, 16), (int)(345*Tiles.SCALE.getValue()), (int)(195*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Block Attack:", (int)(300*Tiles.SCALE.getValue()), (int)(230*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(16*2,16*4, 16, 16), (int)(370*Tiles.SCALE.getValue()), (int)(215*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Dash:", (int)(300*Tiles.SCALE.getValue()), (int)(250*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(16*5,16*4, 16, 16), (int)(330*Tiles.SCALE.getValue()), (int)(235*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Transform:", (int)(300*Tiles.SCALE.getValue()), (int)(270*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(0,16*4, 16, 16), (int)(360*Tiles.SCALE.getValue()), (int)(255*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("Double Jump:", (int)(300*Tiles.SCALE.getValue()), (int)(290*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(370*Tiles.SCALE.getValue()), (int)(275*Tiles.SCALE.getValue()), wKey, hKey, null);
        g.drawString("+", (int)(390*Tiles.SCALE.getValue()), (int)(288*Tiles.SCALE.getValue()));
        g.drawImage(keyboardSprite.getSubimage(0,0, 16, 16), (int)(398*Tiles.SCALE.getValue()), (int)(275*Tiles.SCALE.getValue()), wKey, hKey, null);
    }

    private boolean isMouseInButton(MouseEvent e, PauseButton pauseButton) {
        return pauseButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void mouseClicked(MouseEvent e) {

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
