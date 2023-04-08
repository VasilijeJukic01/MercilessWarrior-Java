package platformer.ui.buttons;

import platformer.model.Tiles;
import platformer.ui.UI;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class VolumeButton extends PauseButton{

    private BufferedImage[] images;
    private BufferedImage slider;
    private final int minValue, maxValue;
    private int buttonX;
    private int imageIndex;
    private boolean mouseOver, mousePressed;
    private float value = 0f;

    // Size Variables [Render]
    private final int sliderX = (int)(315*Tiles.SCALE.getValue());
    private final int sliderY = (int)(290*Tiles.SCALE.getValue());

    public VolumeButton(int xPos, int yPos, int width, int height) {
        super(xPos+width/2, yPos+(int)(Tiles.SCALE.getValue()*9.5), UI.VOLUME_WIDTH.getValue(), height);
        super.buttonHitBox.x -= UI.VOLUME_WIDTH.getValue()/2;
        this.buttonX = xPos+width/2;
        this.xPos = xPos;
        this.width = width;
        this.minValue = (int)(320*Tiles.SCALE.getValue());
        this.maxValue = (int)(525*Tiles.SCALE.getValue());
        loadButtons();
    }

    @Override
    public void loadButtons() {
        images = new BufferedImage[3];

        images[0] = Utils.getInstance().importImage("src/main/resources/images/buttons/SliderBtn0.png", UI.VOLUME_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue());
        images[1] = Utils.getInstance().importImage("src/main/resources/images/buttons/SliderBtn1.png", UI.VOLUME_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue());
        images[2] = Utils.getInstance().importImage("src/main/resources/images/buttons/SliderBtn1.png", UI.VOLUME_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue());

        slider = Utils.getInstance().importImage("src/main/resources/images/buttons/Slider.png", UI.SLIDER_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue());
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(slider, sliderX, sliderY, UI.SLIDER_WIDTH.getValue(), UI.SLIDER_HEIGHT.getValue(), null);
        if (mousePressed) {
            int imageX = buttonX-UI.VOLUME_WIDTH.getValue()/2+3, imageY = yPos+3;
            int imageWid = UI.VOLUME_WIDTH.getValue()-6, imageHei = UI.VOLUME_HEIGHT.getValue()-6;
            g.drawImage(images[imageIndex], imageX, imageY, imageWid, imageHei, null);
        }
        else g.drawImage(images[imageIndex], buttonX-UI.VOLUME_WIDTH.getValue()/2, yPos, UI.VOLUME_WIDTH.getValue(), UI.VOLUME_HEIGHT.getValue(), null);
    }

    public void updateSlider(int value) {
        if (value < minValue) buttonX = minValue;
        else if (value > maxValue) buttonX = maxValue;
        else buttonX = value;
        updateValue();
        buttonHitBox.x = buttonX-UI.VOLUME_WIDTH.getValue()/2;
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
