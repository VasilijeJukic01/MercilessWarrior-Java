package platformer.ui.buttons;

import platformer.animation.AnimUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.BTN_H;
import static platformer.constants.AnimConstants.BTN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.MENU_BTN_SHEET;

public class MenuButton implements GameButton{

    private final int xPos;
    private final int yPos;
    private final int xOffset = BTN_WID/2;
    private final ButtonType buttonType;
    private BufferedImage[] images;
    private int imageIndex;
    private boolean mouseOver, mousePressed;
    private final Rectangle buttonHitBox;

    public MenuButton(int xPos, int yPos, ButtonType buttonType) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.buttonType = buttonType;
        this.buttonHitBox = new Rectangle(xPos-xOffset, yPos, BTN_WID, BTN_HEI);
        loadButtons();
    }

    private void loadButtons() {
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
        images = AnimUtils.getInstance().loadFromSprite(MENU_BTN_SHEET, 3, r, BTN_WID, BTN_HEI, 0, BTN_W, BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos - xOffset, yPos, BTN_WID, BTN_HEI, null);
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
