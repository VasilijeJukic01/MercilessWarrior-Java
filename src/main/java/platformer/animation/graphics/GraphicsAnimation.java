package platformer.animation.graphics;

import java.awt.*;

public interface GraphicsAnimation {

    Point calculatePoint();

    void movementRender(Graphics g, boolean viewMovement);

}
