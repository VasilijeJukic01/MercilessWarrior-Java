package platformer.ui;

import platformer.audio.Audio;
import platformer.audio.Songs;
import platformer.core.Game;
import platformer.model.Tiles;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.CREButton;
import platformer.ui.buttons.PauseButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class GameOverOverlay implements MouseControls{

    private final Game game;
    private BufferedImage overlay;
    private BufferedImage deadText, menuText, respawnText;
    private CREButton retryBtn, menuBtn;

    // Size Variables [Init]
    private final int overlayWid = (int)(300*Tiles.SCALE.getValue());
    private final int overlayHei = (int)(260*Tiles.SCALE.getValue());
    private final int deadTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int deadTextHei = (int)(40*Tiles.SCALE.getValue());
    private final int respawnTextWid = (int)(110*Tiles.SCALE.getValue());
    private final int respawnTextHei = (int)(30*Tiles.SCALE.getValue());
    private final int menuTextWid = (int)(90*Tiles.SCALE.getValue());
    private final int menuTextHei = (int)(30*Tiles.SCALE.getValue());

    private final int retryX = (int)(340*Tiles.SCALE.getValue());
    private final int retryY = (int)(290*Tiles.SCALE.getValue());
    private final int menuX = (int)(480*Tiles.SCALE.getValue());
    private final int menuY = (int)(290*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int overlayX = (int)(270*Tiles.SCALE.getValue());
    private final int overlayY = (int)(90*Tiles.SCALE.getValue());
    private final int deadTextX = (int)(330*Tiles.SCALE.getValue());
    private final int deadTextY = (int)(130*Tiles.SCALE.getValue());
    private final int respawnTextX = (int)(300*Tiles.SCALE.getValue());
    private final int respawnTextY = (int)(230*Tiles.SCALE.getValue());
    private final int menuTextX = (int)(450*Tiles.SCALE.getValue());
    private final int menuTextY = (int)(230*Tiles.SCALE.getValue());

    public GameOverOverlay(Game game) {
        this.game = game;
        init();
    }

    private void init() {
        this.overlay = Utils.instance.importImage("src/main/resources/images/overlay1.png", overlayWid, overlayHei);
        this.deadText = Utils.instance.importImage("src/main/resources/images/buttons/DeadText.png", deadTextWid, deadTextHei);
        this.respawnText = Utils.instance.importImage("src/main/resources/images/buttons/RespawnText.png", respawnTextWid, respawnTextHei);
        this.menuText = Utils.instance.importImage("src/main/resources/images/buttons/MenuText.png", menuTextWid, menuTextHei);
        this.retryBtn = new CREButton(retryX, retryY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.RETRY);
        this.menuBtn = new CREButton(menuX, menuY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.EXIT);
    }

    public void update() {
        retryBtn.update();
        menuBtn.update();
    }

    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, (int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
        g.drawImage(overlay, overlayX, overlayY, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(deadText, deadTextX, deadTextY, deadText.getWidth(), deadText.getHeight(), null);
        g.drawImage(respawnText, respawnTextX, respawnTextY, respawnText.getWidth(), respawnText.getHeight(), null);
        g.drawImage(menuText, menuTextX, menuTextY, menuText.getWidth(), menuText.getHeight(), null);
        retryBtn.render(g);
        menuBtn.render(g);
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

    private boolean isMouseInButton(MouseEvent e, PauseButton pauseButton) {
        return pauseButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        menuBtn.setMouseOver(false);
        menuBtn.setMousePressed(false);
        retryBtn.setMouseOver(false);
        retryBtn.setMousePressed(false);
    }

}
