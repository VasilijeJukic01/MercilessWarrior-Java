package platformer.ui.buttons;

import platformer.animation.Animation;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SL_BTN_H;
import static platformer.constants.AnimConstants.SL_BTN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SLIDER_IMG;
import static platformer.constants.FilePaths.SLIDE_BTN_SHEET;

public class SliderButton extends AbstractButton {

    private BufferedImage[] images;
    private BufferedImage slider;
    private final int minValue, maxValue;
    private int buttonX;
    private int imageIndex;
    private float value = 0f;

    public SliderButton(int xPos, int yPos, int width, int height) {
        super(xPos+width/2, yPos, SLIDER_BTN_SIZE, height/2);
        super.buttonHitBox.x -= SLIDER_BTN_SIZE/2;
        super.buttonHitBox.y = yPos + (int)(8 * SCALE);
        this.buttonX = xPos+width/2;
        this.xPos = xPos;
        this.width = width;
        this.minValue = xPos;
        this.maxValue = xPos + SLIDER_LEN;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = Animation.getInstance().loadFromSprite(SLIDE_BTN_SHEET, 3, 0, SLIDER_BTN_SIZE, SLIDER_BTN_SIZE, 0, SL_BTN_W, SL_BTN_H);
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
        g.drawImage(slider, xPos, yPos, SLIDER_WID, SLIDER_HEI, null);
        g.drawImage(images[imageIndex], buttonX - SLIDER_BTN_SIZE/2, yPos + (int)(8*SCALE), SLIDER_BTN_SIZE, SLIDER_BTN_SIZE, null);
    }

    public void updateSlider(int value) {
        if (value < minValue) buttonX = minValue;
        else buttonX = Math.min(value, maxValue);
        updateValue();
        buttonHitBox.x = buttonX - SLIDER_BTN_SIZE / 2;
    }

    private void updateValue() {
        value = ((buttonX - minValue)*1.0f)/(maxValue - minValue);
    }

    public float getValue() {
        return value;
    }
}
