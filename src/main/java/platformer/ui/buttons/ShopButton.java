package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.SMALL_BTN_HEI;
import static platformer.constants.Constants.SMALL_BTN_WID;
import static platformer.constants.FilePaths.BTN_BUY_SHEET;
import static platformer.constants.FilePaths.BTN_LEAVE_SHEET;

public class ShopButton extends AbstractButton implements GameButton {

    private BufferedImage[] images;
    private int imageIndex;

    public ShopButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        switch (buttonType) {
            case BUY:
                images = AnimUtils.getInstance().loadFromSprite(BTN_BUY_SHEET, 3, 0, SMALL_BTN_WID, SMALL_BTN_HEI, 0, BTN_W, BTN_H);
                break;
            case LEAVE:
                images = AnimUtils.getInstance().loadFromSprite(BTN_LEAVE_SHEET, 3, 0, SMALL_BTN_WID, SMALL_BTN_HEI, 0, BTN_W, BTN_H);
                break;
            default: break;
        }
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, SMALL_BTN_WID, SMALL_BTN_HEI, null);
    }

}
