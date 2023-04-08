package platformer.debug;

import java.awt.*;

public interface Debug {

    void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color);

    void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset);

}
