package platformer.ui.buttons;

import platformer.ui.UI;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

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
        this.buttonHitBox = new Rectangle(xPos, yPos, UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
        loadButtons();
    }

    private void loadButtons() {
        images = new BufferedImage[3];
        switch (buttonType) {
            case BUY:
                images[0] = Utils.getInstance().importImage("src/main/resources/images/buttons/BuyBtn0.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("src/main/resources/images/buttons/BuyBtn1.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("src/main/resources/images/buttons/BuyBtn1.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
                break;
            case LEAVE:
                images[0] = Utils.getInstance().importImage("src/main/resources/images/buttons/LeaveBtn0.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("src/main/resources/images/buttons/LeaveBtn1.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("src/main/resources/images/buttons/LeaveBtn1.png", UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue());
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
        if (mousePressed) g.drawImage(images[imageIndex], xPos, yPos+10, UI.SB_WIDTH.getValue()-20, UI.SB_HEIGHT.getValue()-20, null);
        else g.drawImage(images[imageIndex], xPos, yPos, UI.SB_WIDTH.getValue(), UI.SB_HEIGHT.getValue(), null);
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
