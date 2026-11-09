package platformer.ui.buttons;

import platformer.animation.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SMALL_BTN_H;
import static platformer.constants.AnimConstants.SMALL_BTN_W;
import static platformer.constants.FilePaths.RUNIC_SQUARE_BTN_SHEET;

/**
 * Represents a small-sized button, typically used for navigation.
 */
public class SmallButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;

    public SmallButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = SpriteManager.getInstance().loadFromSprite(RUNIC_SQUARE_BTN_SHEET, 3, 0, width, height, 0, SMALL_BTN_W, SMALL_BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, width, height, null);
        UIButtonRenderer.drawSquareIcon(g, buttonType, false, xPos, yPos, width, height, mouseOver, mousePressed);
    }
}
