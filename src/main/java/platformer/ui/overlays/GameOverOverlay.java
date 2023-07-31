package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.Songs;
import platformer.core.Game;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.buttons.AbstractButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class GameOverOverlay implements Overlay {

    private final Game game;
    private BufferedImage overlay;
    private BufferedImage deadText, menuText, respawnText;
    private CREButton retryBtn, menuBtn;

    // Size Variables [Init]
    private final int overlayWid = (int)(300*SCALE);
    private final int overlayHei = (int)(260*SCALE);
    private final int deadTextWid = (int)(180*SCALE);
    private final int deadTextHei = (int)(40*SCALE);
    private final int respawnTextWid = (int)(110*SCALE);
    private final int respawnTextHei = (int)(30*SCALE);
    private final int menuTextWid = (int)(90*SCALE);
    private final int menuTextHei = (int)(30*SCALE);

    private final int retryX = (int)(340*SCALE);
    private final int retryY = (int)(290*SCALE);
    private final int menuX = (int)(480*SCALE);
    private final int menuY = (int)(290*SCALE);

    // Size Variables [Render]
    private final int overlayX = (int)(270*SCALE);
    private final int overlayY = (int)(90*SCALE);
    private final int deadTextX = (int)(330*SCALE);
    private final int deadTextY = (int)(130*SCALE);
    private final int respawnTextX = (int)(300*SCALE);
    private final int respawnTextY = (int)(230*SCALE);
    private final int menuTextX = (int)(450*SCALE);
    private final int menuTextY = (int)(230*SCALE);

    public GameOverOverlay(Game game) {
        this.game = game;
        init();
    }

    private void init() {
        this.overlay = Utils.getInstance().importImage("/images/overlay1.png", overlayWid, overlayHei);
        this.deadText = Utils.getInstance().importImage("/images/buttons/DeadText.png", deadTextWid, deadTextHei);
        this.respawnText = Utils.getInstance().importImage("/images/buttons/RespawnText.png", respawnTextWid, respawnTextHei);
        this.menuText = Utils.getInstance().importImage("/images/buttons/MenuText.png", menuTextWid, menuTextHei);
        this.retryBtn = new CREButton(retryX, retryY, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.RETRY);
        this.menuBtn = new CREButton(menuX, menuY, CRE_BTN_SIZE, CRE_BTN_SIZE, ButtonType.EXIT);
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
        g.drawImage(overlay, overlayX, overlayY, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(deadText, deadTextX, deadTextY, deadText.getWidth(), deadText.getHeight(), null);
        g.drawImage(respawnText, respawnTextX, respawnTextY, respawnText.getWidth(), respawnText.getHeight(), null);
        g.drawImage(menuText, menuTextX, menuTextY, menuText.getWidth(), menuText.getHeight(), null);
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
            Audio.getInstance().getAudioPlayer().playSong(Songs.FOREST_1.ordinal());
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
        menuBtn.setMouseOver(false);
        menuBtn.setMousePressed(false);
        retryBtn.setMouseOver(false);
        retryBtn.setMousePressed(false);
    }

}
