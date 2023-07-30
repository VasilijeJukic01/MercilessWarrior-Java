package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.*;

public class CREButton extends AbstractButton implements GameButton {

    private BufferedImage[] images;
    private int imageIndex;

    public CREButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        int r = -1;
        switch (buttonType) {
            case CONTINUE: r = 0;
                break;
            case RETRY: r = 1;
                break;
            case EXIT: r = 2;
                break;
            default: break;
        }
        images = AnimUtils.getInstance().loadFromSprite(CRE_BTN_SHEET, 3, r, CRE_BTN_SIZE, CRE_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, CRE_BTN_SIZE, CRE_BTN_SIZE, null);
    }

}
