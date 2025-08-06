package platformer.ui.overlays.controller;

import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.perks.Perk;
import platformer.model.perks.PerksManager;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.overlays.BlacksmithOverlay;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static platformer.constants.Constants.PERK_SLOT_MAX_COL;
import static platformer.constants.UI.*;

/**
 * Handles user interactions with the blacksmith overlay.
 * It manages the selection of perk slots, upgrades, and button actions.
 */
public class BlacksmithViewController {

    private final PerksManager perksManager;
    private final BlacksmithOverlay blacksmithOverlay;
    private int selectedSlotNumber = 0;

    public BlacksmithViewController(PerksManager perksManager, BlacksmithOverlay blacksmithOverlay) {
        this.perksManager = perksManager;
        this.blacksmithOverlay = blacksmithOverlay;
        findFirstAvailablePerk();
    }

    private void findFirstAvailablePerk() {
        perksManager.getPerks().stream()
                .min(Comparator.comparingInt(Perk::getSlot))
                .ifPresent(p -> this.selectedSlotNumber = p.getSlot());
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
                        // TODO: Implement leave functionality
                        // gameState.setOverlay(null);
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

    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> navigate(-PERK_SLOT_MAX_COL);
            case KeyEvent.VK_DOWN -> navigate(PERK_SLOT_MAX_COL);
            case KeyEvent.VK_LEFT -> navigate(-1);
            case KeyEvent.VK_RIGHT -> navigate(1);
            case KeyEvent.VK_ENTER -> upgrade();
        }
    }

    /**
     * Navigates the perk selection based on a delta value. Finds the nearest valid perk in the given direction.
     *
     * @param delta Change in slot number (-1 for left, 1 for right, -PERK_SLOT_MAX_COL for up, PERK_SLOT_MAX_COL for down).
     */
    private void navigate(int delta) {
        List<Perk> perks = new ArrayList<>(perksManager.getPerks());
        perks.sort(Comparator.comparingInt(Perk::getSlot));
        int currentIndex = -1;
        for (int i = 0; i < perks.size(); i++) {
            if (perks.get(i).getSlot() == selectedSlotNumber) {
                currentIndex = i;
                break;
            }
        }
        if (currentIndex == -1) return;

        if (Math.abs(delta) == 1) {
            int newIndex = (currentIndex + delta + perks.size()) % perks.size();
            selectedSlotNumber = perks.get(newIndex).getSlot();
        }
        else {
            int currentCol = selectedSlotNumber % PERK_SLOT_MAX_COL;
            Optional<Perk> nextPerk;
            if (delta > 0) {
                nextPerk = perks.stream()
                        .filter(p -> p.getSlot() > selectedSlotNumber && p.getSlot() % PERK_SLOT_MAX_COL == currentCol)
                        .min(Comparator.comparingInt(Perk::getSlot));
            }
            else {
                nextPerk = perks.stream()
                        .filter(p -> p.getSlot() < selectedSlotNumber && p.getSlot() % PERK_SLOT_MAX_COL == currentCol)
                        .max(Comparator.comparingInt(Perk::getSlot));
            }
            nextPerk.ifPresent(perk -> selectedSlotNumber = perk.getSlot());
        }
        blacksmithOverlay.updateSelectedSlot();
    }


    private void changeSlot(MouseEvent e) {
        for (Perk perk : perksManager.getPerks()) {
            int slot = perk.getSlot();
            int xPos = (slot % PERK_SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_X;
            int yPos = (slot / PERK_SLOT_MAX_COL) * PERK_SLOT_SPACING + PERK_SLOT_Y;
            Rectangle slotBounds = new Rectangle(xPos, yPos, SLOT_SIZE, SLOT_SIZE);
            if (slotBounds.contains(e.getPoint())) {
                this.selectedSlotNumber = slot;
                blacksmithOverlay.updateSelectedSlot();
                return;
            }
        }
    }

    private void upgrade() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.SHOP_BUY);
        perksManager.upgrade(selectedSlotNumber);
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
        return selectedSlotNumber;
    }
}