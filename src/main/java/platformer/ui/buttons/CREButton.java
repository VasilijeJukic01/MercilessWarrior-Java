package platformer.ui.buttons;

import platformer.ui.UI;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class CREButton extends PauseButton implements GameButton{

    private BufferedImage[] images;
    private final ButtonType buttonType;
    private int imageIndex;
    private boolean mouseOver, mousePressed;

    public CREButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        this.buttonType = buttonType;
        loadButtons();
    }

    protected void loadButtons() {
        images = new BufferedImage[3];
        switch (buttonType) {
            case CONTINUE:
                images[0] = Utils.getInstance().importImage("/images/buttons/ContinueBtn0.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/ContinueBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/ContinueBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                break;
            case RETRY:
                images[0] = Utils.getInstance().importImage("/images/buttons/RetryBtn0.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/RetryBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/RetryBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                break;
            case EXIT:
                images[0] = Utils.getInstance().importImage("/images/buttons/ExitBtn0.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[1] = Utils.getInstance().importImage("/images/buttons/ExitBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
                images[2] = Utils.getInstance().importImage("/images/buttons/ExitBtn1.png", UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue());
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
        g.drawImage(images[imageIndex], xPos, yPos, UI.CRE_B_SIZE.getValue(), UI.CRE_B_SIZE.getValue(), null);
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

}
