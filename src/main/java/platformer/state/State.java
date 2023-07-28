package platformer.state;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public interface State {
    void update();

    void render(Graphics g);

    void mousePressed(MouseEvent e);

    void mouseReleased(MouseEvent e);

    void mouseMoved(MouseEvent e);

    void mouseDragged(MouseEvent e);

    void keyPressed(KeyEvent e);

    void keyReleased(KeyEvent e);

    void windowFocusLost(WindowEvent e);

    void setPaused(boolean value);

    void setGameOver(boolean value);

    void setDying(boolean value);

    void reset();
}
