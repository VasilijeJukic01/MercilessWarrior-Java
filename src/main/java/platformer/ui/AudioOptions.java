package platformer.ui;

import platformer.audio.Audio;
import platformer.ui.buttons.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import static platformer.constants.Constants.SCALE;

public class AudioOptions implements MouseControls{

    private SoundButton sfxBtn;
    private MusicButton musicBtn;
    private VolumeButton volumeButton;

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
        this.sfxBtn = new SoundButton(sfxX, sfxY, UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        this.musicBtn = new MusicButton(musicX, musicY, UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        this.volumeButton = new VolumeButton(volumeX, volumeY, UI.SLIDER_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue());
    }

    public void update() {
        sfxBtn.update();
        musicBtn.update();
        volumeButton.update();
    }

    public void render(Graphics g) {
        sfxBtn.render(g);
        musicBtn.render(g);
        volumeButton.render(g);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (volumeButton.isMousePressed()) {
            float prevValue = volumeButton.getValue();
            volumeButton.updateSlider(e.getX());
            float newValue = volumeButton.getValue();
            if (prevValue != newValue) Audio.getInstance().getAudioPlayer().setVolume(newValue);
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMousePressed(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMousePressed(true);
        else if (isMouseInButton(e, volumeButton)) volumeButton.setMousePressed(true);
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
        volumeButton.setMouseOver(false);
        if (isMouseInButton(e, sfxBtn)) sfxBtn.setMouseOver(true);
        else if (isMouseInButton(e, musicBtn)) musicBtn.setMouseOver(true);
        else if (isMouseInButton(e, volumeButton)) volumeButton.setMouseOver(true);
    }

    private boolean isMouseInButton(MouseEvent e, PauseButton pauseButton) {
        return pauseButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {
        sfxBtn.setMouseOver(false);
        sfxBtn.setMousePressed(false);
        musicBtn.setMouseOver(false);
        musicBtn.setMousePressed(false);
        volumeButton.setMouseOver(false);
        volumeButton.setMousePressed(false);
    }
}
