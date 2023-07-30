package platformer.ui.buttons;

import platformer.animation.AnimUtils;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SLIDER_IMG;
import static platformer.constants.FilePaths.SLIDE_BTN_SHEET;

public class VolumeButton extends PauseButton {

    private BufferedImage[] images;
    private BufferedImage slider;
    private final int minValue, maxValue;
    private int buttonX;
    private int imageIndex;
    private boolean mouseOver, mousePressed;
    private float value = 0f;

    // Size Variables [Render]
    private final int sliderX = (int)(315*SCALE);
    private final int sliderY = (int)(290*SCALE);

    public VolumeButton(int xPos, int yPos, int width, int height) {
        super(xPos+width/2, yPos+(int)(SCALE*9.5), SLIDER_BTN_WID, height/2);
        super.buttonHitBox.x -= SLIDER_BTN_WID/2;
        this.buttonX = xPos+width/2;
        this.xPos = xPos;
        this.width = width;
        this.minValue = (int)(320*SCALE);
        this.maxValue = (int)(525*SCALE);
        loadButtons();
    }

    @Override
    public void loadButtons() {
        images = AnimUtils.getInstance().loadFromSprite(SLIDE_BTN_SHEET, 3, 0, SLIDER_BTN_WID, SLIDER_BTN_HEI, 0, SL_BTN_W, SL_BTN_H);
        slider = Utils.getInstance().importImage(SLIDER_IMG, SLIDER_WID, SLIDER_HEI);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(slider, sliderX, sliderY, SLIDER_WID, SLIDER_HEI, null);
        g.drawImage(images[imageIndex], buttonX-SLIDER_BTN_WID/2, yPos, SLIDER_BTN_WID, SLIDER_BTN_HEI, null);
    }

    public void updateSlider(int value) {
        if (value < minValue) buttonX = minValue;
        else buttonX = Math.min(value, maxValue);
        updateValue();
        buttonHitBox.x = buttonX- SLIDER_BTN_WID /2;
    }

    private void updateValue() {
        value = ((buttonX - minValue)*1.0f)/(maxValue - minValue);
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

    public float getValue() {
        return value;
    }
}
