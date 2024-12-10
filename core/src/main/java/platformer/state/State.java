package platformer.state;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

/**
 * Interface for the game states.
 */
public interface State {

    /**
     * Updates the state.
     */
    void update();

    /**
     * Renders the state.
     *
     * @param g the graphics object
     */
    void render(Graphics g);

    /**
     * Handles mouse clicked event.
     *
     * @param e the mouse event
     */
    void mouseClicked(MouseEvent e);

    /**
     * Handles mouse pressed event.
     *
     * @param e the mouse event
     */
    void mousePressed(MouseEvent e);

    /**
     * Handles mouse released event.
     *
     * @param e the mouse event
     */
    void mouseReleased(MouseEvent e);

    /**
     * Handles mouse moved event.
     *
     * @param e the mouse event
     */
    void mouseMoved(MouseEvent e);

    /**
     * Handles mouse dragged event.
     *
     * @param e the mouse event
     */
    void mouseDragged(MouseEvent e);

    /**
     * Handles key pressed event.
     *
     * @param e the key event
     */
    void keyPressed(KeyEvent e);

    /**
     * Handles key released event.
     *
     * @param e the key event
     */
    void keyReleased(KeyEvent e);

    /**
     * Handles window focus gained event.
     *
     * @param e the window event
     */
    void windowFocusLost(WindowEvent e);

    /**
     * Handles reset event.
     */
    void reset();

}
