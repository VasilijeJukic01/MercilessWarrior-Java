package platformer.ui.overlays.controller;

import platformer.core.GameContext;
import platformer.event.EventBus;
import platformer.event.events.ui.OverlayChangeEvent;
import platformer.ui.components.slots.QuestSlot;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.overlays.QuestOverlay;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static platformer.constants.UI.QUEST_SLOT_CAP;

/**
 * Manages the interaction logic for the quest overlay in the game.
 * It handles user input, updates the quest display, and manages pagination of quests.
 */
public class QuestViewController {

    private final GameContext context;
    private final QuestOverlay questOverlay;

    private int currentPage = 0;
    private int selectedQuest = 0;

    public QuestViewController(GameContext context, QuestOverlay questOverlay) {
        this.context = context;
        this.questOverlay = questOverlay;
    }

    // Core
    public void update() {
        List<QuestSlot> slots = context.getQuestManager().getSlots();
        if (selectedQuest >= slots.size()) selectedQuest = Math.max(0, slots.size() - 1);
    }

    // Event Handlers
    public void mouseClicked(MouseEvent e) {

    }

    public void mousePressed(MouseEvent e) {
        setMousePressed(e, questOverlay.getSmallButtons());
        setMousePressed(e, questOverlay.getMediumButtons());

        int start = currentPage * QUEST_SLOT_CAP;
        for (int i = 0; i < QUEST_SLOT_CAP; i++) {
            int slotIndex = start + i;
            if (slotIndex < context.getQuestManager().getSlots().size()) {
                QuestSlot slot = context.getQuestManager().getSlots().get(slotIndex);
                if (slot.isPointInSlot(e.getX(), e.getY())) {
                    selectedQuest = slotIndex;
                    break;
                }
            }
        }
    }

    public void mouseReleased(MouseEvent e) {
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(questOverlay.getSmallButtons()).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(questOverlay.getMediumButtons()).forEach(AbstractButton::resetMouseSet);
    }

    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, questOverlay.getSmallButtons());
        setMouseMoved(e, questOverlay.getMediumButtons());
    }

    public void keyPressed(KeyEvent e) {
        int listSize = context.getQuestManager().getSlots().size();
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                prevPage();
                break;
            case KeyEvent.VK_RIGHT:
                nextPage();
                break;
            case KeyEvent.VK_UP:
                selectedQuest = Math.max(0, selectedQuest - 1);
                break;
            case KeyEvent.VK_DOWN:
                selectedQuest = Math.min(listSize - 1, selectedQuest + 1);
                break;
        }
        int pageOfSelected = selectedQuest / QUEST_SLOT_CAP;
        if (pageOfSelected != currentPage) currentPage = pageOfSelected;
    }

    // Actions
    private void releaseSmallButtons(MouseEvent e) {
        for (AbstractButton button : questOverlay.getSmallButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (button.getButtonType() == ButtonType.PREV) prevPage();
                else if (button.getButtonType() == ButtonType.NEXT) nextPage();
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (AbstractButton button : questOverlay.getMediumButtons()) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (Objects.requireNonNull(button.getButtonType()) == ButtonType.CLOSE) {
                    EventBus.getInstance().publish(new OverlayChangeEvent(null));
                }
                break;
            }
        }
    }

    private void prevPage() {
        if (currentPage > 0) currentPage--;
    }

    private void nextPage() {
        if ((currentPage + 1) * QUEST_SLOT_CAP < context.getQuestManager().getSlots().size()) currentPage++;
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
        return button.getButtonHitBox().contains(e.getX(), e.getY());
    }

    // Getters
    public int getCurrentPage() {
        return currentPage;
    }

    public int getSelectedQuest() {
        return selectedQuest;
    }
}