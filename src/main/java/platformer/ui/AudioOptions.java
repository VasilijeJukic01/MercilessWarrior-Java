package platformer.ui;

import platformer.audio.Audio;
import platformer.ui.buttons.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import static platformer.constants.Constants.SOUND_BTN_SIZE;
import static platformer.constants.UI.*;

public class AudioOptions {

    private AudioButton sfxBtn, musicBtn;
    private SliderButton sliderButton;

    public AudioOptions() {
        init();
    }

    private void init() {
        this.sfxBtn = new AudioButton(SFX_X, SFX_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.SFX);
        this.musicBtn = new AudioButton(MUSIC_X, MUSIC_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.MUSIC);
        this.sliderButton = new SliderButton(SLIDER_BTN_X, SLIDER_BTN_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE);
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

    public void mouseDragged(MouseEvent e) {
        if (sliderButton.isMousePressed()) {
            float prevValue = sliderButton.getValue();
            sliderButton.updateSlider(e.getX());
            float newValue = sliderButton.getValue();
            if (prevValue != newValue) Audio.getInstance().getAudioPlayer().setVolume(newValue);
        }

    }

    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMousePressed(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMousePressed(true);
        else if (isMouseInButton(e, sliderButton)) sliderButton.setMousePressed(true);
    }

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
        sfxBtn.resetMouseSet();
        musicBtn.resetMouseSet();
        sliderButton.resetMouseSet();
    }
}
