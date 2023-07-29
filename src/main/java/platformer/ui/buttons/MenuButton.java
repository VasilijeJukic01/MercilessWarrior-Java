package platformer.ui.buttons;

import platformer.ui.UI;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class MenuButton implements GameButton{

    private final int xPos;
    private final int yPos;
    private final int xOffset = UI.B_WIDTH.getValue()/2;
    private final ButtonType buttonType;
    private BufferedImage[] images;
    private int imageIndex;
    private boolean mouseOver, mousePressed;
    private final Rectangle buttonHitBox;

    public MenuButton(int xPos, int yPos, ButtonType buttonType) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.buttonType = buttonType;
        this.buttonHitBox = new Rectangle(xPos-xOffset, yPos, UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
        loadButtons();
    }

    private void loadButtons() {
        images = new BufferedImage[3];
        switch (buttonType) {
            case PLAY:
                images[0] = Utils.getInstance().importImage("/images/buttons/PlayBtn0.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/PlayBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/PlayBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                break;
            case OPTIONS:
                images[0] = Utils.getInstance().importImage("/images/buttons/OptionsBtn0.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/OptionsBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/OptionsBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                break;
            case CONTROLS:
                images[0] = Utils.getInstance().importImage("/images/buttons/ControlsBtn0.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/ControlsBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/ControlsBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                break;
            case QUIT:
                images[0] = Utils.getInstance().importImage("/images/buttons/QuitBtn0.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/QuitBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/QuitBtn1.png", UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue());
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
        if (mousePressed) g.drawImage(images[imageIndex], xPos - xOffset+10, yPos+10, UI.B_WIDTH.getValue()-20, UI.B_HEIGHT.getValue()-20, null);
        else g.drawImage(images[imageIndex], xPos - xOffset, yPos, UI.B_WIDTH.getValue(), UI.B_HEIGHT.getValue(), null);
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
