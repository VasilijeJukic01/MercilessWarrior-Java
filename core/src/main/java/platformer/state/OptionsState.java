package platformer.state;

import platformer.core.Game;
import platformer.ui.AudioOptions;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.overlays.OverlayLayer;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * State of the game when the player is in the options menu.
 * In this state, the player can adjust various settings such as audio options.
 */
@SuppressWarnings("FieldCanBeLocal")
public class OptionsState extends AbstractState implements State {

    private final AudioOptions audioOptions;

    private BufferedImage optionsText;
    private BufferedImage SFXText, musicText, volumeText;

    private SmallButton exitBtn;

    public OptionsState(Game game) {
        super(game);
        this.audioOptions = game.getAudioOptions();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.optionsText = Utils.getInstance().importImage(OPTIONS_TXT, OPTIONS_TEXT_WID, OPTIONS_TEXT_HEI);
        this.volumeText = Utils.getInstance().importImage(VOLUME_TXT, VOLUME_TEXT_WID, VOLUME_TEXT_HEI);
        this.SFXText = Utils.getInstance().importImage(SFX_TXT, SFX_TEXT_WID, SFX_TEXT_HEI);
        this.musicText = Utils.getInstance().importImage(MUSIC_TXT, MUSIC_TEXT_WID, MUSIC_TEXT_HEI);
    }

    private void loadButtons() {
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    @Override
    public void update() {
        OverlayLayer.getInstance().update();
        exitBtn.update();
        audioOptions.update();
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().render(g);
        renderImages(g);
        exitBtn.render(g);
        audioOptions.render(g);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    // Render
    private void renderImages(Graphics g) {
        g.drawImage(optionsText, OPTIONS_TEXT_X, OPTIONS_TEXT_Y, optionsText.getWidth(), optionsText.getHeight(), null);
        g.drawImage(volumeText, VOLUME_TEXT_X, VOLUME_TEXT_Y, volumeText.getWidth(), volumeText.getHeight(), null);
        g.drawImage(SFXText, SFX_TEXT_X, SFX_TEXT_Y, SFXText.getWidth(), SFXText.getHeight(), null);
        g.drawImage(musicText, MUSIC_TEXT_X, MUSIC_TEXT_Y, musicText.getWidth(), musicText.getHeight(), null);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else audioOptions.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.startMenuState();
        }
        else audioOptions.mouseReleased(e);
        reset();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
        else audioOptions.mouseMoved(e);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        audioOptions.mouseDragged(e);
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
        exitBtn.setMouseOver(false);
        exitBtn.setMousePressed(false);
    }

}
