package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.Song;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SmallButton;
import platformer.ui.buttons.AbstractButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;
import static platformer.constants.UI.*;

public class GameOverOverlay implements Overlay<MouseEvent, Graphics> {

    private final Game game;
    private BufferedImage deadText, menuText, respawnText;
    private SmallButton retryBtn, menuBtn;

    public GameOverOverlay(Game game) {
        this.game = game;
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.deadText = Utils.getInstance().importImage(DEAD_TXT, DEAD_TEXT_WID, DEAD_TEXT_HEI);
        this.respawnText = Utils.getInstance().importImage(RESPAWN_TXT, RESPAWN_TEXT_WID, RESPAWN_TEXT_HEI);
        this.menuText = Utils.getInstance().importImage(MENU_TXT, MENU_TEXT_WID, MENU_TEXT_HEI);
    }

    private void loadButtons() {
        this.retryBtn = new SmallButton(RETRY_X, RETRY_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        this.menuBtn = new SmallButton(MENU_X, MENU_Y, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
    }

    // Core
    @Override
    public void update() {
        retryBtn.update();
        menuBtn.update();
    }

    @Override
    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        OverlayLayer.getInstance().renderOverlay(g);
        renderTexts(g);
        renderButtons(g);
    }

    private void renderTexts(Graphics g) {
        g.drawImage(deadText, DEAD_TEXT_X, DEAD_TEXT_Y, deadText.getWidth(), deadText.getHeight(), null);
        g.drawImage(respawnText, RESPAWN_TEXT_X, RESPAWN_TEXT_Y, respawnText.getWidth(), respawnText.getHeight(), null);
        g.drawImage(menuText, MENU_TEXT_X, MENU_TEXT_Y, menuText.getWidth(), menuText.getHeight(), null);
    }

    private void renderButtons(Graphics g) {
        retryBtn.render(g);
        menuBtn.render(g);
    }

    @Override
    public void reset() {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, retryBtn)) retryBtn.setMousePressed(true);
        if (isMouseInButton(e, menuBtn)) menuBtn.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Song.FOREST_1);
        }
        if(isMouseInButton(e, menuBtn) && menuBtn.isMousePressed()) {
            game.reset();
            game.startMenuState();
        }
        resetButtons();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        menuBtn.setMouseOver(false);
        retryBtn.setMouseOver(false);
        if (isMouseInButton(e, retryBtn)) retryBtn.setMouseOver(true);
        if (isMouseInButton(e, menuBtn)) menuBtn.setMouseOver(true);
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        menuBtn.resetMouseSet();
        retryBtn.resetMouseSet();
    }

}
