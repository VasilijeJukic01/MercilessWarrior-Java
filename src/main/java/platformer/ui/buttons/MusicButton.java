package platformer.ui.buttons;

import platformer.ui.UI;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.SCALE;

public class MusicButton extends PauseButton {

    private BufferedImage[][] images;
    private int imageIndexI, imageIndexJ;
    private boolean mouseOver, mousePressed;
    private boolean muted;

    public MusicButton(int xPos, int yPos, int width, int height) {
        super(xPos, yPos, width, height);
        loadButtons();
    }

    protected void loadButtons() {
        images = new BufferedImage[2][3];

        images[0][0] = Utils.getInstance().importImage("/images/buttons/MusicON0.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        images[0][1] = Utils.getInstance().importImage("/images/buttons/MusicON1.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        images[0][2] = Utils.getInstance().importImage("/images/buttons/MusicON1.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());

        images[1][0] = Utils.getInstance().importImage("/images/buttons/MusicOFF0.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        images[1][1] = Utils.getInstance().importImage("/images/buttons/MusicOFF1.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
        images[1][2] = Utils.getInstance().importImage("/images/buttons/MusicOFF1.png", UI.SOUND_B_SIZE.getValue(), UI.SOUND_B_SIZE.getValue());
    }

    @Override
    public void update() {
        imageIndexI = muted ? 0 : 1;
        imageIndexJ = 0;
        if (mouseOver) imageIndexJ = 1;
        if (mousePressed) imageIndexJ = 2;
    }

    @Override
    public void render(Graphics g) {
        if (mousePressed) {
            int buttonX = xPos+(int)(5 * SCALE), buttonY = yPos+(int)(5 * SCALE);
            int buttonWid = width-(int)(10 * SCALE), buttonHei = height-(int)(10 * SCALE);
            g.drawImage(images[imageIndexI][imageIndexJ], buttonX, buttonY, buttonWid, buttonHei, null);
        }
        else g.drawImage(images[imageIndexI][imageIndexJ], xPos, yPos, width, height, null);
    }

    public boolean isMousePressed() {
        return mousePressed;
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMouseOver(boolean mouseOver) {
        this.mouseOver = mouseOver;
    }

    public void setMousePressed(boolean mousePressed) {
        this.mousePressed = mousePressed;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

}
