package platformer.debug;

import java.awt.*;

public interface Debug<G> {

    void hitBoxRenderer(G g, int xLevelOffset, int yLevelOffset, Color color);

    void attackBoxRenderer(G g, int xLevelOffset, int yLevelOffset);

}
