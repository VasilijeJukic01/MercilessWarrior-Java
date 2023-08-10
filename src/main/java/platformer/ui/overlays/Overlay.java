package platformer.ui.overlays;

import java.awt.*;
import java.awt.event.MouseEvent;

public interface Overlay {

    void mouseDragged(MouseEvent e);

    void mousePressed(MouseEvent e);

    void mouseReleased(MouseEvent e);

    void mouseMoved(MouseEvent e);

    void update();

    void render(Graphics g);

    void reset();

}
