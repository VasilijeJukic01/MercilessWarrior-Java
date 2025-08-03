package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.perks.Perk;
import platformer.state.types.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.BlacksmithOverlay;

import java.awt.event.MouseEvent;
import java.util.Arrays;

import static platformer.constants.Constants.PERK_SLOT_MAX_COL;
import static platformer.constants.Constants.PERK_SLOT_MAX_ROW;
import static platformer.constants.UI.*;

/**
 * Handles user interactions with the blacksmith overlay.
 * It manages the selection of perk slots, upgrades, and button actions.
 */
public class BlacksmithViewController {

    private final GameState gameState;
    private final BlacksmithOverlay blacksmithOverlay;
    private int slotNumber = 0;

    public BlacksmithViewController(GameState gameState, BlacksmithOverlay blacksmithOverlay) {
        this.gameState = gameState;
        this.blacksmithOverlay = blacksmithOverlay;
    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, blacksmithOverlay.getButtons());
        changeSlot(e);
    }

    public void mouseReleased(MouseEvent e) {
        for (AbstractButton button : blacksmithOverlay.getButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case BUY:
                        upgrade();
                        break;
                    case LEAVE:
                        gameState.setOverlay(null);
                        break;
                    default: break;
                }
                break;
            }
        }
        Arrays.stream(blacksmithOverlay.getButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, blacksmithOverlay.getButtons());
    }

    private void changeSlot(MouseEvent e) {
        int x = e.getX(), y = e.getY();
        for (int i = 0; i < PERK_SLOT_MAX_COL; i++) {
            for (int j = 0; j < PERK_SLOT_MAX_ROW; j++) {
                int xStart = i * PERK_SLOT_SPACING + PERK_SLOT_X;
                int xEnd = xStart + SLOT_SIZE;
                int yStart = j * PERK_SLOT_SPACING + PERK_SLOT_Y;
                int yEnd = yStart + SLOT_SIZE;
                if (x >= xStart && x <= xEnd && y >= yStart && y <= yEnd) {
                    int newSlotNumber = i + (j * PERK_SLOT_MAX_COL);
                    if (blacksmithOverlay.getPlaceHolders()[j][i] == 1) {
                        this.slotNumber = newSlotNumber;
                        blacksmithOverlay.updateSelectedSlot();
                    }
                    return;
                }
            }
        }
    }

    private void upgrade() {
        for (Perk perk : gameState.getPerksManager().getPerks()) {
            if (slotNumber == perk.getSlot() && perk.isUpgraded()) return;
        }
        if (!checkTokens()) return;
        Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
        gameState.getPerksManager().upgrade(PERK_SLOT_MAX_COL, PERK_SLOT_MAX_ROW, slotNumber);
    }

    private boolean checkTokens() {
        for (Perk perk : gameState.getPerksManager().getPerks()) {
            if (slotNumber == perk.getSlot() && gameState.getPlayer().getUpgradeTokens() >= perk.getCost()) {
                gameState.getPlayer().changeUpgradeTokens(-perk.getCost());
                return true;
            }
        }
        return false;
    }

    // Helpers
    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton button) {
        return button.getButtonHitBox().contains(e.getPoint());
    }

    // Getters
    public int getSlotNumber() {
        return slotNumber;
    }
}