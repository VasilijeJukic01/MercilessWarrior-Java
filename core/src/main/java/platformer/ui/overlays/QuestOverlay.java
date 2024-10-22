package platformer.ui.overlays;

import platformer.state.GameState;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import static platformer.constants.UI.*;

public class QuestOverlay implements Overlay<MouseEvent, KeyEvent, Graphics> {

    private final GameState gameState;

    public QuestOverlay(GameState gameState) {
        this.gameState = gameState;
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g) {
        OverlayLayer.getInstance().renderOverlay(g);
        g.setColor(QUEST_SLOT_COLOR);
        for (int i = 1; i <= QUEST_SLOT_CAP; i++) {
            g.fillRect(QUEST_SLOT_X, QUEST_SLOT_Y + i * QUEST_SLOT_SPACING, QUEST_SLOT_WID, QUEST_SLOT_HEI);
        }
    }

    @Override
    public void reset() {

    }
}
