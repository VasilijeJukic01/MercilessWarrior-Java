package platformer.ui;

import platformer.audio.Audio;
import platformer.ui.buttons.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.SOUND_BTN_SIZE;

public class AudioOptions implements MouseControls{

    private AudioButton sfxBtn, musicBtn;
    private SliderButton sliderButton;

    // Size Variables [Render]
    private final int sfxX = (int)(450*SCALE);
    private final int sfxY = (int)(148*SCALE);
    private final int musicX = (int)(450*SCALE);
    private final int musicY = (int)(198*SCALE);
    private final int volumeX = (int)(330*SCALE);
    private final int volumeY = (int)(290*SCALE);

    public AudioOptions() {
        init();
    }

    private void init() {
        this.sfxBtn = new AudioButton(sfxX, sfxY, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.SFX);
        this.musicBtn = new AudioButton(musicX, musicY, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.MUSIC);
        this.sliderButton = new SliderButton(volumeX, volumeY, SOUND_BTN_SIZE, SOUND_BTN_SIZE);
    }

    public void update() {
        sfxBtn.update();
        musicBtn.update();
        sliderButton.update();
    }

    public void render(Graphics g) {
        sfxBtn.render(g);
        musicBtn.render(g);
        sliderButton.render(g);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (sliderButton.isMousePressed()) {
            float prevValue = sliderButton.getValue();
            sliderButton.updateSlider(e.getX());
            float newValue = sliderButton.getValue();
            if (prevValue != newValue) Audio.getInstance().getAudioPlayer().setVolume(newValue);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMousePressed(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMousePressed(true);
        else if (isMouseInButton(e, sliderButton)) sliderButton.setMousePressed(true);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseInButton(e, sfxBtn) && sfxBtn.isMousePressed()) {
            sfxBtn.setMuted(!sfxBtn.isMuted());
            Audio.getInstance().getAudioPlayer().soundMute();
        }
        else if(isMouseInButton(e, musicBtn) && musicBtn.isMousePressed()) {
            musicBtn.setMuted(!musicBtn.isMuted());
            Audio.getInstance().getAudioPlayer().songMute();
        }
        resetButtons();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        sfxBtn.setMouseOver(false);
        musicBtn.setMouseOver(false);
        sliderButton.setMouseOver(false);
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMouseOver(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMouseOver(true);
        else if (isMouseInButton(e, sliderButton)) sliderButton.setMouseOver(true);
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        sfxBtn.setMouseOver(false);
        sfxBtn.setMousePressed(false);
        musicBtn.setMouseOver(false);
        musicBtn.setMousePressed(false);
        sliderButton.setMouseOver(false);
        sliderButton.setMousePressed(false);
    }
}
