package platformer.controller;

import platformer.view.GamePanel;
import platformer.core.Game;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.GAME_HEIGHT;

/**
 * The GameMouseListener class is responsible for handling mouse events in the game.
 */
public class GameMouseListener implements MouseListener, MouseMotionListener {

    private final GamePanel gamePanel;

    public GameMouseListener(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    /**
     * Transforms a mouse event's coordinates if the game is in fullscreen mode
     * @param e The original mouse event
     * @return A new mouse event with transformed coordinates or the original if not in fullscreen
     */
    private MouseEvent transformMouseEvent(MouseEvent e) {
        Game game = gamePanel.getGame();
        if (game.isFullScreen()) {
            Dimension currentSize = gamePanel.getCurrentSize();

            double scaleX = (double) currentSize.width / GAME_WIDTH;
            double scaleY = (double) currentSize.height / GAME_HEIGHT;

            int transformedX = (int) (e.getX() / scaleX);
            int transformedY = (int) (e.getY() / scaleY);

            return new MouseEvent(
                e.getComponent(), e.getID(), e.getWhen(), e.getModifiersEx(),
                transformedX, transformedY, e.getClickCount(), e.isPopupTrigger(), e.getButton()
            );
        }
        return e;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gamePanel.getGame().mouseClicked(transformMouseEvent(e));
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gamePanel.getGame().mousePressed(transformMouseEvent(e));
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gamePanel.getGame().mouseReleased(transformMouseEvent(e));
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gamePanel.getGame().mouseDragged(transformMouseEvent(e));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gamePanel.getGame().mouseMoved(transformMouseEvent(e));
    }
}
