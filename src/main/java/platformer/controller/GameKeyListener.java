package platformer.controller;

import platformer.view.GamePanel;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The GameKeyListener class is responsible for handling key events in the game.
 */
public class GameKeyListener implements KeyListener {

    private final GamePanel gamePanel;

    public GameKeyListener(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        gamePanel.getGame().keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        gamePanel.getGame().keyReleased(e);
    }

}
