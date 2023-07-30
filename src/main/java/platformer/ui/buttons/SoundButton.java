package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SMALL_BTN_H;
import static platformer.constants.AnimConstants.SMALL_BTN_W;
import static platformer.constants.Constants.SOUND_BTN_SIZE;
import static platformer.constants.FilePaths.AUDIO_BTN_SHEET;

public class SoundButton extends PauseButton {

    private BufferedImage[][] images;
    private int imageIndexI, imageIndexJ;
    private boolean mouseOver, mousePressed;
    private boolean muted;

    public SoundButton(int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height);
        loadButtons();
    }

    @Override
    public void loadButtons() {
        images = new BufferedImage[2][3];
        images[0] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 2, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
        images[1] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 3, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
    }

    @Override
    public void update() {
        imageIndexI = muted ? 0 : 1;
        imageIndexJ = 0;
        if (mouseOver) imageIndexJ = 1;
        if (mousePressed) imageIndexJ = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndexI][imageIndexJ],xPos, yPos, width, height, null);
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
