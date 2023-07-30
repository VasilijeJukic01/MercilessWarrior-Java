package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SMALL_BTN_H;
import static platformer.constants.AnimConstants.SMALL_BTN_W;
import static platformer.constants.Constants.SOUND_BTN_SIZE;
import static platformer.constants.FilePaths.AUDIO_BTN_SHEET;

public class AudioButton extends AbstractButton {

    private BufferedImage[][] images;
    private int imageIndexI, imageIndexJ;
    private boolean muted;

    public AudioButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = new BufferedImage[2][3];
        if (buttonType == ButtonType.SFX) {
            images[0] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 2, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
            images[1] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 3, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
        }
        else {
            images[0] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 0, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
            images[1] = AnimUtils.getInstance().loadFromSprite(AUDIO_BTN_SHEET, 3, 1, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
        }
    }

    // Core
    @Override
    public void update() {
        updateImageIndex();
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndexI][imageIndexJ], xPos, yPos, width, height, null);
    }

    private void updateImageIndex() {
        imageIndexI = muted ? 0 : 1;
        imageIndexJ = 0;
        if (mouseOver) imageIndexJ = 1;
        if (mousePressed) imageIndexJ = 2;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
