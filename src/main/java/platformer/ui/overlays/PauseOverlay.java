package platformer.ui.overlays;

import platformer.audio.Audio;
import platformer.audio.Songs;
import platformer.core.Game;
import platformer.model.Tiles;
import platformer.ui.AudioOptions;
import platformer.ui.MouseControls;
import platformer.ui.UI;
import platformer.ui.buttons.*;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PauseOverlay implements MouseControls {

    private final Game game;
    private final AudioOptions audioOptions;
    private BufferedImage pauseText;
    private BufferedImage SFXText, musicText, volumeText;

    // Size Variables [Init]
    private final int pauseTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int pauseTextHei = (int)(40*Tiles.SCALE.getValue());
    private final int SFXTextWid = (int)(60*Tiles.SCALE.getValue());
    private final int SFXTextHei = (int)(30*Tiles.SCALE.getValue());
    private final int musicTextWid = (int)(90*Tiles.SCALE.getValue());
    private final int musicTextHei = (int)(30*Tiles.SCALE.getValue());
    private final int volumeTextWid = (int)(110*Tiles.SCALE.getValue());
    private final int volumeTextHei = (int)(30*Tiles.SCALE.getValue());

    private final int continueBtnX = (int)(330*Tiles.SCALE.getValue());
    private final int continueBtnY = (int)(350*Tiles.SCALE.getValue());
    private final int retryBtnX = (int)(405*Tiles.SCALE.getValue());
    private final int retryBtnY = (int)(350*Tiles.SCALE.getValue());
    private final int exitBtnX = (int)(480*Tiles.SCALE.getValue());
    private final int exitBtnY = (int)(350*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int pauseTextX = (int)(330*Tiles.SCALE.getValue());
    private final int pauseTextY = (int)(85*Tiles.SCALE.getValue());
    private final int SFXTextX = (int)(325*Tiles.SCALE.getValue());
    private final int SFXTextY = (int)(150*Tiles.SCALE.getValue());
    private final int musicTextX = (int)(325*Tiles.SCALE.getValue());
    private final int musicTextY = (int)(200*Tiles.SCALE.getValue());
    private final int volumeTextX = (int)(365*Tiles.SCALE.getValue());
    private final int volumeTextY = (int)(260*Tiles.SCALE.getValue());


    private CREButton continueBtn, retryBtn, exitBtn;

    public PauseOverlay(Game game) {
        this.game = game;
        this.audioOptions = game.getAudioOptions();
        init();
    }

    private void init() {
        this.pauseText = Utils.instance.importImage("src/main/resources/images/buttons/PauseText.png", pauseTextWid, pauseTextHei);
        this.SFXText = Utils.instance.importImage("src/main/resources/images/buttons/SFXText.png", SFXTextWid, SFXTextHei);
        this.musicText = Utils.instance.importImage("src/main/resources/images/buttons/MusicText.png", musicTextWid, musicTextHei);
        this.volumeText = Utils.instance.importImage("src/main/resources/images/buttons/VolumeText.png", volumeTextWid, volumeTextHei);
        this.continueBtn = new CREButton(continueBtnX, continueBtnY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.CONTINUE);
        this.retryBtn = new CREButton(retryBtnX, retryBtnY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.RETRY);
        this.exitBtn = new CREButton(exitBtnX, exitBtnY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.EXIT);
    }

    public void update() {
        continueBtn.update();
        retryBtn.update();
        exitBtn.update();
        audioOptions.update();
    }

    public void render(Graphics g) {
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, (int)Tiles.GAME_WIDTH.getValue(), (int)Tiles.GAME_HEIGHT.getValue());
        Overlay.getInstance().renderOverlay(g);
        g.drawImage(pauseText, pauseTextX, pauseTextY, pauseText.getWidth(), pauseText.getHeight(), null);
        g.drawImage(SFXText, SFXTextX, SFXTextY, SFXText.getWidth(), SFXText.getHeight(), null);
        g.drawImage(musicText, musicTextX, musicTextY, musicText.getWidth(), musicText.getHeight(), null);
        g.drawImage(volumeText, volumeTextX, volumeTextY, volumeText.getWidth(), volumeText.getHeight(), null);
        continueBtn.render(g);
        retryBtn.render(g);
        exitBtn.render(g);
        audioOptions.render(g);
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
        if(isMouseInButton(e, continueBtn) && continueBtn.isMousePressed()) game.setPaused(false);
        else if(isMouseInButton(e, retryBtn) && retryBtn.isMousePressed()) {
            game.reset();
            Audio.getInstance().getAudioPlayer().playSong(Songs.FOREST_1.ordinal());
        }
        else if(isMouseInButton(e, exitBtn) && exitBtn.isMousePressed()) {
            game.setPaused(false);
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

    private boolean isMouseInButton(MouseEvent e, PauseButton pauseButton) {
        return pauseButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        continueBtn.setMouseOver(false);
        continueBtn.setMousePressed(false);
        retryBtn.setMouseOver(false);
        retryBtn.setMousePressed(false);
        exitBtn.setMouseOver(false);
        exitBtn.setMousePressed(false);
    }
}
