package platformer.ui.buttons;

import java.awt.*;

public abstract class PauseButton implements GameButton {

    protected int xPos;
    protected int yPos;
    protected int width, height;
    protected Rectangle buttonHitBox;

    public PauseButton(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.buttonHitBox = new Rectangle(xPos, yPos, width, height);
    }

    protected abstract void loadButtons();

    public Rectangle getButtonHitBox() {
        return buttonHitBox;
    }

}
