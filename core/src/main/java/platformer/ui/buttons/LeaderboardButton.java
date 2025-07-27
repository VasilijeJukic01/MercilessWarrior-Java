package platformer.ui.buttons;

import platformer.animation.Animation;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.SMALL_BTN_H;
import static platformer.constants.AnimConstants.SMALL_BTN_W;
import static platformer.constants.Constants.CRE_BTN_SIZE;
import static platformer.constants.FilePaths.LEADERBOARD_BTN_SHEET;

/**
 * A specific button used to navigate to the game's leaderboard screen.
 */
public class LeaderboardButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;

    public LeaderboardButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        images = Animation.getInstance().loadFromSprite(LEADERBOARD_BTN_SHEET, 3, 0, CRE_BTN_SIZE, CRE_BTN_SIZE, 0, SMALL_BTN_W, SMALL_BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, CRE_BTN_SIZE, CRE_BTN_SIZE, null);
    }

}
