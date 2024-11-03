package platformer.ui.overlays;

import platformer.state.GameState;
import platformer.ui.buttons.AbstractButton;
import platformer.ui.buttons.ButtonType;
import platformer.ui.buttons.MediumButton;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Objects;

import static platformer.constants.Constants.SMALL_BTN_HEI;
import static platformer.constants.Constants.SMALL_BTN_WID;
import static platformer.constants.FilePaths.QUESTS_TXT;
import static platformer.constants.UI.*;

/**
 * QuestOverlay class is an overlay that is displayed when the player opens the quest log.
 * It allows the player to interact with their quests.
 */
public class QuestOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;
    private final MediumButton[] buttons;

    private BufferedImage questsText;

    public QuestOverlay(GameState gameState) {
        this.gameState = gameState;
        this.buttons = new MediumButton[1];
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
        buttons[0] = new MediumButton(QUEST_BTN_X, QUEST_BTN_Y, SMALL_BTN_WID, SMALL_BTN_HEI, ButtonType.CLOSE);
    }

    // Core
    @Override
    public void update() {
        Arrays.stream(buttons).forEach(MediumButton::update);
    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().renderOverlay(g);
        g.drawImage(questsText, QUEST_TEXT_X, QUEST_TEXT_Y, questsText.getWidth(), questsText.getHeight(), null);
        gameState.getQuestManager().getSlots().forEach(slot -> slot.render(g));
        renderButtons(g);
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMousePressed(true));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        for (MediumButton button : buttons) {
            if (isMouseInButton(e, button) && button.isMousePressed()) {
                if (Objects.requireNonNull(button.getButtonType()) == ButtonType.CLOSE) {
                    gameState.setOverlay(null);
                }
                break;
            }
        }
        Arrays.stream(buttons).forEach(AbstractButton::resetMouseSet);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Arrays.stream(buttons).forEach(button -> button.setMouseOver(false));

        Arrays.stream(buttons)
                .filter(button -> isMouseInButton(e, button))
                .findFirst()
                .ifPresent(button -> button.setMouseOver(true));
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    private void renderButtons(Graphics g) {
        Arrays.stream(buttons).forEach(button -> button.render(g));
    }

    private boolean isMouseInButton(MouseEvent e, MediumButton mediumButton) {
        return mediumButton.getButtonHitBox().contains(e.getX(), e.getY());
    }

    @Override
    public void reset() {

    }
}
