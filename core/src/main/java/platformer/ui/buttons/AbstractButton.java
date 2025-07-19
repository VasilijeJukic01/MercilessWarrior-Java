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

    /**
     * Constructs a new AbstractButton with specified dimensions and position.
     *
     * @param xPos   The x-coordinate of the button's top-left corner.
     * @param yPos   The y-coordinate of the button's top-left corner.
     * @param width  The width of the button.
     * @param height The height of the button.
     */
    public AbstractButton(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.buttonHitBox = new Rectangle(xPos, yPos, width, height);
    }

    /**
     * Resets the mouse interaction states (mouseOver and mousePressed) to false.
     * This is typically called after a mouse release event.
     */
    public void resetMouseSet() {
        this.mouseOver = false;
        this.mousePressed = false;
    }

    /**
     * Abstract method that must be implemented by subclasses to load their specific
     * image assets (spritesheets).
     */
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
