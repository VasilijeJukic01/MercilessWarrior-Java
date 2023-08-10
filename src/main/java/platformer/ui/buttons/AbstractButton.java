package platformer.ui.buttons;

import java.awt.*;

public abstract class AbstractButton implements GameButton {

    protected ButtonType buttonType;
    protected int xPos, yPos;
    protected int width, height;
    protected Rectangle buttonHitBox;
    protected boolean mouseOver, mousePressed;

    public AbstractButton(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.buttonHitBox = new Rectangle(xPos, yPos, width, height);
    }

    public void resetMouseSet() {
        this.mouseOver = false;
        this.mousePressed = false;
    }

    protected abstract void loadButtons();

    public Rectangle getButtonHitBox() {
        return buttonHitBox;
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

    public ButtonType getButtonType() {
        return buttonType;
    }

}
