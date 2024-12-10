package platformer.controller;

import platformer.view.GamePanel;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * The GameMouseListener class is responsible for handling mouse events in the game.
 */
public class GameMouseListener implements MouseListener, MouseMotionListener {

    private final GamePanel gamePanel;

    public GameMouseListener(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        gamePanel.getGame().mouseClicked(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        gamePanel.getGame().mousePressed(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gamePanel.getGame().mouseReleased(e);
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        gamePanel.getGame().mouseDragged(e);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        gamePanel.getGame().mouseMoved(e);
    }

}
