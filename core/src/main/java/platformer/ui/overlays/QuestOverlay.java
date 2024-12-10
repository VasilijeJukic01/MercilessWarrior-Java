package platformer.ui.overlays;

import platformer.state.GameState;
import platformer.ui.QuestSlot;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.ui.buttons.SmallButton;
import platformer.utils.Utils;


import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static platformer.constants.Constants.*;
import static platformer.constants.Constants.SMALL_BTN_SIZE;
import static platformer.constants.FilePaths.QUESTS_TXT;
import static platformer.constants.UI.*;

/**
 * QuestOverlay class is an overlay that is displayed when the player opens the quest log.
 * It allows the player to interact with their quests.
 */
public class QuestOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;
    private final MediumButton[] mediumButtons;
    private final SmallButton[] smallButtons;

    private BufferedImage questsText;

    private int currentPage = 0;

    public QuestOverlay(GameState gameState) {
        this.gameState = gameState;
        this.smallButtons = new SmallButton[2];
        this.mediumButtons = new MediumButton[1];
        init();
    }

    // Init
    private void init() {
        loadImages();
        loadButtons();
    }

    private void loadImages() {
        this.questsText = Utils.getInstance().importImage(QUESTS_TXT, QUEST_TXT_WID, QUEST_TXT_HEI);
    }

    private void loadButtons() {
        smallButtons[0] = new SmallButton(QUEST_BTN_PREV_X, QUEST_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.PREV);
        smallButtons[1] = new SmallButton(QUEST_BTN_NEXT_X, QUEST_BTN_Y, SMALL_BTN_SIZE, SMALL_BTN_SIZE, ButtonType.NEXT);

        mediumButtons[0] = new MediumButton(QUEST_BTN_X, QUEST_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.CLOSE);
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(smallButtons).forEach(SmallButton::update);
        Arrays.stream(mediumButtons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().renderOverlay(g);
        g.drawImage(questsText, QUEST_TEXT_X, QUEST_TEXT_Y, questsText.getWidth(), questsText.getHeight(), null);

        List<QuestSlot> slots = gameState.getQuestManager().getSlots();
        int start = currentPage * QUEST_SLOT_CAP;
        int end = Math.min(start + QUEST_SLOT_CAP, slots.size());

        slots.stream()
                .skip(start)
                .limit(end - start)
                .forEach(slot -> slot.render(g));

        renderButtons(g);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        setMousePressed(e, smallButtons);
        setMousePressed(e, mediumButtons);
    }

    private void setMousePressed(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gameState.getQuestManager().getSlots().forEach(s -> s.setSelected(false));
        releaseSmallButtons(e);
        releaseMediumButtons(e);
        Arrays.stream(smallButtons).forEach(AbstractButton::resetMouseSet);
        Arrays.stream(mediumButtons).forEach(AbstractButton::resetMouseSet);

        gameState.getQuestManager().getSlots().forEach(s -> s.checkSelected(e.getX(), e.getY()));
    }

    private void releaseSmallButtons(MouseEvent e) {
        for (SmallButton button : smallButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                switch (button.getButtonType()) {
                    case PREV:
                        if (currentPage > 0) currentPage--;
                        break;
                    case NEXT:
                        if ((currentPage + 1) * QUEST_SLOT_CAP < gameState.getQuestManager().getSlots().size()) currentPage++;
                        break;
                }
                break;
            }
        }
    }

    private void releaseMediumButtons(MouseEvent e) {
        for (MediumButton button : mediumButtons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (Objects.requireNonNull(button.getButtonType()) == ButtonType.CLOSE) {
                    gameState.setOverlay(null);
                }
                break;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setMouseMoved(e, smallButtons);
        setMouseMoved(e, mediumButtons);
    }

    private void setMouseMoved(MouseEvent e, AbstractButton[] buttons) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                if (currentPage > 0) currentPage--;
                break;
            case KeyEvent.VK_RIGHT:
                if ((currentPage + 1) * QUEST_SLOT_CAP < gameState.getQuestManager().getSlots().size()) currentPage++;
                break;
            default: break;
        }
    }

    private void renderButtons(Graphics g) {
        Arrays.stream(smallButtons).forEach(button -> button.render(g));
        Arrays.stream(mediumButtons).forEach(button -> button.render(g));
    }

    private boolean isMouseInButton(MouseEvent e, AbstractButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {

    }
}
