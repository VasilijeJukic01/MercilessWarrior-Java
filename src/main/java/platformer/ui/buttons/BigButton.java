package platformer.ui.buttons;

import platformer.animation.Animation;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.BTN_H;
import static platformer.constants.AnimConstants.BTN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.MENU_BTN_SHEET;

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
        int r = -1;
        switch (buttonType) {
            case PLAY:
                r = 0;
                break;
            case OPTIONS:
                r = 1;
                break;
            case CONTROLS:
                r = 2;
                break;
            case QUIT:
                r = 3;
                break;
            default: break;
        }
        images = Animation.getInstance().loadFromSprite(MENU_BTN_SHEET, 3, r, BIG_BTN_WID, BIG_BTN_HEI, 0, BTN_W, BTN_H);
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
    }

}
