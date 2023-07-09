package platformer.state;

import platformer.core.Game;
import platformer.model.Tiles;
import platformer.ui.AudioOptions;
import platformer.ui.UI;
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

@SuppressWarnings("FieldCanBeLocal")
public class OptionsState extends StateAbstraction implements State{

    private final AudioOptions audioOptions;

    private BufferedImage optionsText;
    private BufferedImage SFXText, musicText, volumeText;

    private CREButton exitBtn;

    // Size Variables [Init]
    private final int optionsTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int optionsTextHei = (int)(40*Tiles.SCALE.getValue());
    private final int volumeTextWid = (int)(110*Tiles.SCALE.getValue());
    private final int volumeTextHei = (int)(30*Tiles.SCALE.getValue());
    private final int SFXTextWid = (int)(60*Tiles.SCALE.getValue());
    private final int SFXTextHei = (int)(30*Tiles.SCALE.getValue());
    private final int musicTextWid = (int)(90*Tiles.SCALE.getValue());
    private final int musicTextHei = (int)(30*Tiles.SCALE.getValue());

    private final int exitBtnX = (int)(480*Tiles.SCALE.getValue());
    private final int exitBtnY = (int)(350*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int optionsTextX = (int)(330*Tiles.SCALE.getValue());
    private final int optionsTextY = (int)(85*Tiles.SCALE.getValue());
    private final int volumeTextX = (int)(365*Tiles.SCALE.getValue());
    private final int volumeTextY = (int)(260*Tiles.SCALE.getValue());
    private final int SFXTextX = (int)(325*Tiles.SCALE.getValue());
    private final int SFXTextY = (int)(150*Tiles.SCALE.getValue());
    private final int musicTextX = (int)(325*Tiles.SCALE.getValue());
    private final int musicTextY = (int)(200*Tiles.SCALE.getValue());

    public OptionsState(Game game) {
        super(game);
        this.audioOptions = game.getAudioOptions();
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.optionsText = Utils.instance.importImage("src/main/resources/images/buttons/OptionsText.png", optionsTextWid, optionsTextHei);
        this.volumeText = Utils.instance.importImage("src/main/resources/images/buttons/VolumeText.png", volumeTextWid, volumeTextHei);
        this.SFXText = Utils.instance.importImage("src/main/resources/images/buttons/SFXText.png", SFXTextWid, SFXTextHei);
        this.musicText = Utils.instance.importImage("src/main/resources/images/buttons/MusicText.png", musicTextWid,musicTextHei);
    }

    private void loadButtons() {
        this.exitBtn = new CREButton(exitBtnX, exitBtnY, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), ButtonType.EXIT);
    }

    @Override
    public void update() {
        Overlay.getInstance().update();
        exitBtn.update();
        audioOptions.update();
    }

    @Override
    public void render(Graphics g) {
        Overlay.getInstance().render(g);
        g.drawImage(optionsText, optionsTextX, optionsTextY, optionsText.getWidth(), optionsText.getHeight(), null);
        g.drawImage(volumeText, volumeTextX, volumeTextY, volumeText.getWidth(), volumeText.getHeight(), null);
        g.drawImage(SFXText, SFXTextX, SFXTextY, SFXText.getWidth(), SFXText.getHeight(), null);
        g.drawImage(musicText, musicTextX, musicTextY, musicText.getWidth(), musicText.getHeight(), null);
        exitBtn.render(g);
        audioOptions.render(g);
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
