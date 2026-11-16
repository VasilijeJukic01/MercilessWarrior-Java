package platformer.ui.buttons;

import platformer.animation.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.BTN_H;
import static platformer.constants.AnimConstants.BTN_W;
import static platformer.constants.Constants.BIG_BTN_HEI;
import static platformer.constants.Constants.BIG_BTN_WID;
import static platformer.constants.FilePaths.RUNIC_RECT_BTN_SHEET;

/**
 * Represents a large-sized button, typically used for primary actions in the main menu.
 */
public class BigButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;

    public BigButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = SpriteManager.getInstance().loadFromSprite(RUNIC_RECT_BTN_SHEET, 3, 0, BIG_BTN_WID, BIG_BTN_HEI, 0, BTN_W, BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, BIG_BTN_WID, BIG_BTN_HEI, null);
        UIButtonRenderer.drawButtonLabel(g, buttonType.name(), xPos, yPos, BIG_BTN_WID, BIG_BTN_HEI, mouseOver, mousePressed);
    }
}
