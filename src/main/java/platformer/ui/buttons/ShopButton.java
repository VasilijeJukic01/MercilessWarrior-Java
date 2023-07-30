package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.SMALL_BTN_HEI;
import static platformer.constants.Constants.SMALL_BTN_WID;
import static platformer.constants.FilePaths.BTN_BUY_SHEET;
import static platformer.constants.FilePaths.BTN_LEAVE_SHEET;

public class ShopButton implements GameButton {

    private final int xPos;
    private final int yPos;
    private final ButtonType buttonType;
    private BufferedImage[] images;
    private int imageIndex;
    private boolean mouseOver, mousePressed;
    private final Rectangle buttonHitBox;

    public ShopButton(int xPos, int yPos, ButtonType buttonType) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.buttonType = buttonType;
        this.buttonHitBox = new Rectangle(xPos, yPos, SMALL_BTN_WID, SMALL_BTN_HEI);
        loadButtons();
    }

    private void loadButtons() {
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

    public void resetMouseSet() {
        mouseOver = false;
        mousePressed = false;
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public Rectangle getButtonHitBox() {
        return buttonHitBox;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }
}
