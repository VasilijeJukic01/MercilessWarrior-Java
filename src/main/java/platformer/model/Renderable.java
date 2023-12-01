package platformer.model;

import java.awt.*;

public interface Renderable<G> {

    void render(G g, int xLevelOffset, int yLevelOffset);

    void renderHitBox(G g, int xLevelOffset, int yLevelOffset, Color color);

}
