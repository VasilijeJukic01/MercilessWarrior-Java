package platformer.ui.buttons;

import platformer.animation.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.BTN_H;
import static platformer.constants.AnimConstants.BTN_W;
import static platformer.constants.FilePaths.RUNIC_RECT_BTN_SHEET;

/**
 * Represents a medium-sized button used for a variety of actions within UI overlays.
 */
public class MediumButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;

    public MediumButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = SpriteManager.getInstance().loadFromSprite(RUNIC_RECT_BTN_SHEET, 3, 0, width, height, 0, BTN_W, BTN_H);
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
        String label = buttonType == ButtonType.TAKE_ALL ? "TAKE ALL" : buttonType.name();
        UIButtonRenderer.drawButtonLabel(g, label, xPos, yPos, width, height, mouseOver, mousePressed);
    }

}
