package platformer.ui.overlays.controller;

import platformer.core.Framework;
import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.model.levels.LevelManager.TotemNode;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.MediumButton;
import platformer.ui.components.slots.GameSlot;
import platformer.ui.overlays.SaveGameOverlay;
import platformer.ui.transition.TransitionDirection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.UI.GAME_SLOT_CAP;
import static platformer.ui.overlays.SaveGameOverlay.*;

/**
 * Controller for the Save Totem overlay.
 * Handles Rest & Save (with time-skip fade, full heal, and enemy respawn) and Fast Travel between discovered totems.
 */
public class SaveViewController {

    private final GameContext context;
    private final SaveGameOverlay overlay;

    private int selectedTotemIndex = 0;

    public SaveViewController(GameContext context, SaveGameOverlay overlay) {
        this.context = context;
        this.overlay = overlay;
    }

    public void update() {
        List<TotemNode> nodes = context.getLevelManager().getUnlockedTotemNodes();
        if (selectedTotemIndex >= nodes.size()) {
            selectedTotemIndex = Math.max(0, nodes.size() - 1);
        }
    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, overlay.getButtons());
        checkSaveSlotClick(e);
        if (checkTotemSlotSelection(e) && SwingUtilities.isRightMouseButton(e)) {
            travelToSelectedTotem();
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) return;
        for (MediumButton button : overlay.getButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case SAVE -> performRestAndSave();
                    case TRAVEL -> travelToSelectedTotem();
                    case CLOSE -> EventBus.getInstance().publish(new OverlayChangeEvent(null));
                    default -> {}
                }
                break;
            }
        }
        Arrays.stream(overlay.getButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, overlay.getButtons());
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            travelToSelectedTotem();
        }
    }

    public void keyReleased(KeyEvent e) {
        List<TotemNode> nodes = context.getLevelManager().getUnlockedTotemNodes();
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            selectedTotemIndex = Math.max(0, selectedTotemIndex - 1);
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            selectedTotemIndex = Math.min(Math.max(0, nodes.size() - 1), selectedTotemIndex + 1);
        }
    }

    private void checkSaveSlotClick(MouseEvent e) {
        List<GameSlot> slots = Framework.getInstance().getSaveController().getGameSlots();
        GameSlot clicked = null;
        for (int i = 0; i < slots.size(); i++) {
            int cardY = SAVE_CARD_Y + i * SAVE_CARD_GAP;
            Rectangle rect = new Rectangle(SAVE_CARD_X, cardY, SAVE_CARD_W, SAVE_CARD_H);
            if (rect.contains(e.getPoint())) {
                clicked = slots.get(i);
                break;
            }
        }
        if (clicked != null) {
            for (GameSlot s : slots) {
                s.setSelected(s == clicked);
            }
        }
    }

    private boolean checkTotemSlotSelection(MouseEvent e) {
        List<TotemNode> nodes = context.getLevelManager().getUnlockedTotemNodes();
        for (int i = 0; i < Math.min(GAME_SLOT_CAP, nodes.size()); i++) {
            int cardY = TRAVEL_CARD_Y + i * TRAVEL_CARD_GAP;
            Rectangle bounds = new Rectangle(TRAVEL_CARD_X, cardY, TRAVEL_CARD_W, TRAVEL_CARD_H);
            if (bounds.contains(e.getPoint())) {
                selectedTotemIndex = i;
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the game to the selected slot, closes the overlay, and triggers a Rest time-skip transition.
     */
    private void performRestAndSave() {
        List<GameSlot> slots = Framework.getInstance().getSaveController().getGameSlots();
        boolean anySelected = slots.stream().anyMatch(GameSlot::isSelected);
        if (!anySelected) return;

        context.getWorldMemoryManager().unlockCurrentRoomTotem();
        Framework.getInstance().getSaveController().saveSlot();
        EventBus.getInstance().publish(new OverlayChangeEvent(null));

        context.getGameState().getTransitionManager().startTransition(
                TransitionDirection.FROM_TOP,
                () -> context.getWorldTransitionCoordinator().restInCurrentRoom()
        );
    }

    private void travelToSelectedTotem() {
        List<TotemNode> nodes = context.getLevelManager().getUnlockedTotemNodes();
        if (selectedTotemIndex < 0 || selectedTotemIndex >= nodes.size()) return;

        TotemNode target = nodes.get(selectedTotemIndex);
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        if (target.levelId().equalsIgnoreCase(currentLevelId)) return;

        EventBus.getInstance().publish(new OverlayChangeEvent(null));
        context.getGameState().getTransitionManager().startTransition(
                TransitionDirection.FROM_BOTTOM,
                () -> context.getWorldTransitionCoordinator().travelToTotem(target.totemId(), target.levelId(), target.totemName())
        );
    }

    public void reset() {
        context.getWorldMemoryManager().unlockCurrentRoomTotem();
        List<TotemNode> nodes = context.getLevelManager().getUnlockedTotemNodes();
        String currentLevelId = context.getLevelManager().getCurrentLevelId();
        selectedTotemIndex = 0;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).levelId().equalsIgnoreCase(currentLevelId)) {
                selectedTotemIndex = i;
                break;
            }
        }
    }

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

    public int getSelectedTotemIndex() {
        return selectedTotemIndex;
    }
}