package platformer.ui;

import platformer.audio.Audio;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.AudioButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.SliderButton;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import static platformer.constants.Constants.SOUND_BTN_SIZE;
import static platformer.constants.UI.*;

/**
 * Manages the audio control interface components and their interactions in the game.
 * This class handles both music and sound effects (SFX) controls through buttons and sliders.
 * <p>
 * The interface includes:
 * <ul>
 *   <li>Mute/unmute buttons for both music and SFX</li>
 *   <li>Volume slider controls for independent adjustment of music and SFX levels</li>
 * </ul>
 *
 * The class processes mouse events for button interactions and slider adjustments, updating the audio settings through the {@link Audio} system.
 *
 * @see AudioButton
 * @see SliderButton
 * @see Audio
 */
public class AudioOptions {

    private AudioButton sfxBtn, musicBtn;
    private SliderButton musicSliderButton, sfxSliderButton;

    public AudioOptions() {
        init();
    }

    private void init() {
        this.sfxBtn = new AudioButton(SFX_X, SFX_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.SFX);
        this.musicBtn = new AudioButton(MUSIC_X, MUSIC_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE, ButtonType.MUSIC);
        this.musicSliderButton = new SliderButton(MUSIC_SLIDER_BTN_X, MUSIC_SLIDER_BTN_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE);
        this.sfxSliderButton = new SliderButton(SFX_SLIDER_BTN_X, SFX_SLIDER_BTN_Y, SOUND_BTN_SIZE, SOUND_BTN_SIZE);
    }

    public void update() {
        sfxBtn.update();
        musicBtn.update();
        musicSliderButton.update();
        sfxSliderButton.update();
    }

    public void render(Graphics g) {
        sfxBtn.render(g);
        musicBtn.render(g);
        musicSliderButton.render(g);
        sfxSliderButton.render(g);
    }

    private void updateSliderAndVolume(SliderButton sliderButton, MouseEvent e, Consumer<Float> volumeSetter) {
        if (sliderButton.isMousePressed()) {
            float prevValue = sliderButton.getValue();
            sliderButton.updateSlider(e.getX());
            float newValue = sliderButton.getValue();
            if (prevValue != newValue) {
                volumeSetter.accept(newValue);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        updateSliderAndVolume(musicSliderButton, e, Audio.getInstance().getAudioPlayer()::setMusicVolume);
        updateSliderAndVolume(sfxSliderButton, e, Audio.getInstance().getAudioPlayer()::setSfxVolume);
    }

    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMousePressed(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMousePressed(true);
        else if (isMouseInButton(e, musicSliderButton)) musicSliderButton.setMousePressed(true);
        else if (isMouseInButton(e, sfxSliderButton)) sfxSliderButton.setMousePressed(true);
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
        musicSliderButton.setMouseOver(false);
        sfxSliderButton.setMouseOver(false);
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMouseOver(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMouseOver(true);
        else if (isMouseInButton(e, musicSliderButton)) musicSliderButton.setMouseOver(true);
        else if (isMouseInButton(e, sfxSliderButton)) sfxSliderButton.setMouseOver(true);
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton abstractButton) {
        return abstractButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    public void resetButtons() {
        sfxBtn.resetMouseSet();
        musicBtn.resetMouseSet();
        musicSliderButton.resetMouseSet();
        sfxSliderButton.resetMouseSet();
    }
}
