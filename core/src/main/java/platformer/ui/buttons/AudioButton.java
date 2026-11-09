package platformer.ui.buttons;

import platformer.animation.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SMALL_BTN_H;
import static platformer.constants.AnimConstants.SMALL_BTN_W;
import static platformer.constants.Constants.SOUND_BTN_SIZE;
import static platformer.constants.FilePaths.RUNIC_SQUARE_BTN_SHEET;

/**
 * A specialized button for controlling audio settings (Music and SFX).
 * This button manages its visual state based on whether it is muted, hovered over, or pressed.
 */
public class AudioButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;
    private boolean muted;

    public AudioButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = SpriteManager.getInstance().loadFromSprite(RUNIC_SQUARE_BTN_SHEET, 3, 0, SOUND_BTN_SIZE, SOUND_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
    }

    // Core
    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, width, height, null);
        UIButtonRenderer.drawSquareIcon(g, buttonType, muted, xPos, yPos, width, height, mouseOver, mousePressed);
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }
}
