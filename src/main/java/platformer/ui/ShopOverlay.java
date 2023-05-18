package platformer.ui;

import platformer.model.Tiles;
import platformer.model.entities.Player;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.ShopButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ShopOverlay {

    private final Player player;

    private BufferedImage overlay;
    private BufferedImage shopText;
    private final ShopButton[] buttons;

    // Size Variables [Init]
    private final int overlayWid = (int)(400* Tiles.SCALE.getValue());
    private final int overlayHei = (int)(340*Tiles.SCALE.getValue());
    private final int shopTextWid = (int)(180*Tiles.SCALE.getValue());
    private final int shopTextHei = (int)(60*Tiles.SCALE.getValue());

    // Size Variables [Render]
    private final int overlayX = (int)(220*Tiles.SCALE.getValue());
    private final int overlayY = (int)(50*Tiles.SCALE.getValue());
    private final int shopTextX = (int)(330*Tiles.SCALE.getValue());
    private final int shopTextY = (int)(80*Tiles.SCALE.getValue());
    private final int buyBtnX = (int)(300*Tiles.SCALE.getValue());
    private final int buyBtnY = (int)(330*Tiles.SCALE.getValue());
    private final int exitBtnX = (int)(440*Tiles.SCALE.getValue());
    private final int exitBtnY = (int)(330*Tiles.SCALE.getValue());

    public ShopOverlay(Player player) {
        this.player = player;
        this.buttons = new ShopButton[2];
        init();
    }

    private void init() {
        this.overlay = Utils.instance.importImage("src/main/resources/images/overlay1.png", overlayWid, overlayHei);
        this.shopText = Utils.instance.importImage("src/main/resources/images/buttons/ShopText.png", shopTextWid, shopTextHei);
        buttons[0] = new ShopButton(buyBtnX, buyBtnY, ButtonType.BUY);
        buttons[1] = new ShopButton(exitBtnX, exitBtnY, ButtonType.LEAVE);
    }

    public void update() {
        for (ShopButton button : buttons) {
            button.update();
        }
    }

    public void render(Graphics g) {
        g.drawImage(overlay, overlayX, overlayY, overlay.getWidth(), overlay.getHeight(), null);
        g.drawImage(shopText, shopTextX, shopTextY, shopText.getWidth(), shopText.getHeight(), null);
        for (ShopButton button : buttons) {
            button.render(g);
        }
    }

    public void mousePressed(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMousePressed(true);
                break;
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY: //TODO
                        break;
                    case LEAVE: //TODO
                        break;
                    default: break;
                }
                break;
            }
        }
        for (ShopButton button : buttons) {
            button.resetMouseSet();
        }
    }

    public void mouseMoved(MouseEvent e) {
        for (ShopButton button : buttons) {
            button.setMouseOver(false);
        }
        for (ShopButton button : buttons) {
            if (isMouseInButton(e, button)) {
                button.setMouseOver(true);
                break;
            }
        }
    }

    private boolean isMouseInButton(MouseEvent e, ShopButton shopButton) {
        return shopButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    private void resetButtons() {

    }

}
