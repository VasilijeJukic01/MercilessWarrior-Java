package platformer.ui.buttons;

import platformer.audio.Audio;
import platformer.audio.types.Sound;

import java.awt.*;

/**
 * Abstract class for all buttons in the game.
 * <p>
 * This class contains the basic functionality for all buttons in the game.
 * It contains the basic properties of a button such as its position, width, height, and hitbox.
 * <p>
 * AbstractButton class is extended by all the buttons in the game.
 */
public abstract class AbstractButton implements GameButton<Graphics> {

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
        if (mousePressed){
            Audio.getInstance().getAudioPlayer().playSound(Sound.BTN_CLICK);
        }
        this.mousePressed = mousePressed;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

}
