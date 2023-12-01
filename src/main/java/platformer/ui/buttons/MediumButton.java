package platformer.ui.buttons;

import platformer.animation.Animation;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.*;
import static platformer.constants.Constants.MEDIUM_BTN_HEI;
import static platformer.constants.Constants.MEDIUM_BTN_WID;
import static platformer.constants.FilePaths.*;

public class MediumButton extends AbstractButton {

    private BufferedImage[] images;
    private int imageIndex;

    public MediumButton(int xPos, int yPos, int width, int height, ButtonType buttonType) {
        super(xPos, yPos, width, height);
        super.buttonType = buttonType;
        loadButtons();
    }

    @Override
    protected void loadButtons() {
        String sheet = "";
        int frames = 3;
        int row = 0;

        switch (buttonType) {
            case BUY:
                sheet = BTN_BUY_SHEET; break;
            case SELL:
                sheet = BTN_SELL_SHEET; break;
            case LEAVE:
                sheet = BTN_LEAVE_SHEET; break;
            case SAVE:
                sheet = BTN_SAVE_SHEET; break;
            case USE:
                sheet = INVENTORY_BTN_SHEET; break;
            case EQUIP:
                sheet = INVENTORY_BTN_SHEET;
                row = 1;
                break;
            case DROP:
                sheet = INVENTORY_BTN_SHEET;
                row = 2;
                break;
            case UNEQUIP:
                sheet = INVENTORY_BTN_SHEET;
                row = 3;
                break;
            case TAKE:
                sheet = LOOT_BTN_SHEET;
                break;
            case TAKE_ALL:
                sheet = LOOT_BTN_SHEET;
                row = 1;
                break;
            case CLOSE:
                sheet = LOOT_BTN_SHEET;
                row = 2;
                break;
                case CRAFT:
                sheet = CRAFT_BTN_SHEET;
                break;
            default: break;
        }
        if (!sheet.isEmpty())
            images = Animation.getInstance().loadFromSprite(sheet, frames, row, MEDIUM_BTN_WID, MEDIUM_BTN_HEI, 0, BTN_W, BTN_H);
    }

    @Override
    public void update() {
        imageIndex = 0;
        if (mouseOver) imageIndex = 1;
        if (mousePressed) imageIndex = 2;
    }

    @Override
    public void render(Graphics g) {
        g.drawImage(images[imageIndex], xPos, yPos, width, height, null);
    }

}
