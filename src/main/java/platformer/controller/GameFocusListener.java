package platformer.controller;

import platformer.core.Game;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

/**
 * The GameFocusListener class is responsible for handling the focus of the game window.
 */
public class GameFocusListener implements WindowFocusListener {

    private final Game game;

    public GameFocusListener(Game game) {
        this.game = game;
    }

    @Override
    public void windowGainedFocus(WindowEvent e) {

    }

    @Override
    public void windowLostFocus(WindowEvent e) {
        game.windowFocusLost(e);
    }

}
