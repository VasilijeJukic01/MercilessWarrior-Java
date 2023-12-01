package platformer.ui.buttons;

import platformer.animation.Animation;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

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
        int r = -1;
        switch (buttonType) {
            case NEXT:
            case PREV:
            case CONTINUE: r = 0;
                break;
            case RETRY: r = 1;
                break;
            case EXIT: r = 2;
                break;
            default: break;
        }
        images = Animation.getInstance().loadFromSprite(CRE_BTN_SHEET, 3, r, CRE_BTN_SIZE, CRE_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
        if (buttonType == ButtonType.PREV) {
            for (int i = 0; i < images.length; i++) {
                images[i] = Utils.getInstance().flipImage(images[i]);
            }
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
        g.drawImage(images[imageIndex], xPos, yPos, width, height, null);
    }

}
