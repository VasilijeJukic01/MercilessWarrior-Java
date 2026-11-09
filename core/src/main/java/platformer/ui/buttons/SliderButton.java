package platformer.ui.buttons;

import platformer.animation.SpriteManager;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SL_BTN_H;
import static platformer.constants.AnimConstants.SL_BTN_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.SLIDER_IMG;
import static platformer.constants.FilePaths.SLIDE_BTN_SHEET;

/**
 * A UI component representing a slider with a draggable button.
 * This is used for settings that require a continuous range of values, such as audio volume.
 */
public class SliderButton extends AbstractButton {

    private BufferedImage[] images;
    private BufferedImage slider;
    private final int minValue, maxValue;
    private final int grooveInset;
    private final int knobSize;
    private int buttonX;
    private int imageIndex;
    private float value = 0f;

    public SliderButton(int xPos, int yPos, int width, int height) {
        super(xPos + width / 2, yPos, (int) (SLIDER_BTN_SIZE * 1.25), SLIDER_HEI);
        this.knobSize = (int) (SLIDER_BTN_SIZE * 1.25);
        this.grooveInset = (int) (15 * SCALE);
        this.xPos = xPos;
        this.width = width;
        this.minValue = xPos + grooveInset;
        this.maxValue = xPos + SLIDER_WID - grooveInset;
        this.buttonX = (minValue + maxValue) / 2;
        super.buttonHitBox.x = buttonX - knobSize / 2;
        super.buttonHitBox.y = yPos + (SLIDER_HEI - knobSize) / 2;
        super.buttonHitBox.width = knobSize;
        super.buttonHitBox.height = knobSize;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = SpriteManager.getInstance().loadFromSprite(SLIDE_BTN_SHEET, 3, 0, knobSize, knobSize, 0, SL_BTN_W, SL_BTN_H);
        slider = ImageUtils.importImage(SLIDER_IMG, SLIDER_WID, SLIDER_HEI);
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

        int fillWidth = Math.max(0, buttonX - minValue);
        if (fillWidth > 0) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int barH = Math.max(4, (int) (6 * SCALE));
            int barY = yPos + (SLIDER_HEI - barH) / 2;
            GradientPaint gp = new GradientPaint(minValue, barY, new Color(0, 175, 185, 200), buttonX, barY, new Color(70, 250, 240, 230));
            g2d.setPaint(gp);
            g2d.fillRoundRect(minValue, barY, fillWidth, barH, barH, barH);
            g2d.dispose();
        }

        g.drawImage(images[imageIndex], buttonX - knobSize / 2, yPos + (SLIDER_HEI - knobSize) / 2, knobSize, knobSize, null);
    }

    public void updateSlider(int mouseX) {
        if (mouseX < minValue) buttonX = minValue;
        else buttonX = Math.min(mouseX, maxValue);
        updateValue();
        buttonHitBox.x = buttonX - knobSize / 2;
    }

    public void setSliderValue(float value) {
        this.value = Math.max(0f, Math.min(1f, value));
        this.buttonX = minValue + (int) (this.value * (maxValue - minValue));
        buttonHitBox.x = buttonX - knobSize / 2;
    }

    /**
     * Recalculates the slider's normalized value (0.0 to 1.0) based on the button's position.
     */
    private void updateValue() {
        value = ((buttonX - minValue) * 1.0f) / (maxValue - minValue);
    }

    /**
     * Gets the current value of the slider.
     *
     * @return A float value between 0.0 and 1.0, representing the slider's position.
     */
    public float getValue() {
        return value;
    }
}
