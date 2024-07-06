package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.Song;
import platformer.core.Game;
import platformer.state.GameState;
import platformer.ui.AudioOptions;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

/**
 * PauseOverlay class is an overlay that is displayed when the game is paused.
 * It allows the player to interact with pause game options.
 */
public class PauseOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final Game game;
    private final GameState gameState;
    private final AudioOptions audioOptions;
    private BufferedImage pauseText;
    private BufferedImage SFXText, musicText, volumeText;
    private SmallButton continueBtn, retryBtn, exitBtn;

    public PauseOverlay(Game game, GameState gameState) {
        this.game = game;
        this.gameState = gameState;
        this.audioOptions = game.getAudioOptions();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.pauseText = Utils.getInstance().importImage(PAUSE_TXT, PAUSE_TEXT_WID, PAUSE_TEXT_HEI);
        this.SFXText = Utils.getInstance().importImage(SFX_TXT, SFX_TEXT_WID, SFX_TEXT_HEI);
        this.musicText = Utils.getInstance().importImage(MUSIC_TXT, MUSIC_TEXT_WID, MUSIC_TEXT_HEI);
        this.volumeText = Utils.getInstance().importImage(VOLUME_TXT, VOLUME_TEXT_WID, VOLUME_TEXT_HEI);
    }

    private void loadButtons() {
        this.continueBtn = new SmallButton(CONTINUE_BTN_X, CONTINUE_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.CONTINUE);
        this.retryBtn = new SmallButton(RETRY_BTN_X, RETRY_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        this.exitBtn = new SmallButton(EXIT_BTN_X, EXIT_BTN_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        continueBtn.update();
        retryBtn.update();
        exitBtn.update();
        audioOptions.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        OverlayLayer.getInstance().renderOverlay(g);
        renderTexts(g);
        renderButtons(g);
    }

    private void renderTexts(Graphics g) {
        g.drawImage(pauseText, PAUSE_TEXT_X, PAUSE_TEXT_Y, pauseText.getWidth(), pauseText.getHeight(), null);
        g.drawImage(SFXText, SFX_TEXT_X, SFX_TEXT_Y, SFXText.getWidth(), SFXText.getHeight(), null);
        g.drawImage(musicText, MUSIC_TEXT_X, MUSIC_TEXT_Y, musicText.getWidth(), musicText.getHeight(), null);
        g.drawImage(volumeText, VOLUME_TEXT_X, VOLUME_TEXT_Y, volumeText.getWidth(), volumeText.getHeight(), null);
    }

    private void renderButtons(Graphics g) {
        continueBtn.render(g);
        retryBtn.render(g);
        exitBtn.render(g);
        audioOptions.render(g);
    }

    @Override
    public void reset() {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        audioOptions.mouseDragged(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, continueBtn)) continueBtn.setMousePressed(true);
        else if (isMouseInButton(e, retryBtn)) retryBtn.setMousePressed(true);
        else if (isMouseInButton(e, exitBtn)) exitBtn.setMousePressed(true);
        else audioOptions.mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, continueBtn) && continueBtn.isMousePressed()) {
            gameState.setOverlay(null);
        }
        else if(isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        else if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            gameState.setOverlay(null);
            game.startMenuState();
        }
        else audioOptions.mouseReleased(e);
        resetButtons();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        continueBtn.setMouseOver(false);
        retryBtn.setMouseOver(false);
        exitBtn.setMouseOver(false);
        if (isMouseInButton(e, continueBtn)) continueBtn.setMouseOver(true);
        else if (isMouseInButton(e, retryBtn)) retryBtn.setMouseOver(true);
        else if (isMouseInButton(e, exitBtn)) exitBtn.setMouseOver(true);
        else audioOptions.mouseMoved(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        continueBtn.resetMouseSet();
        retryBtn.resetMouseSet();
        exitBtn.resetMouseSet();
    }
}
