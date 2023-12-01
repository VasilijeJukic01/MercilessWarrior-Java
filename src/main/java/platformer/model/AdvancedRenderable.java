package platformer.model;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface AdvancedRenderable<G> {

        void render(G g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations);

        void renderHitBox(G g, int xLevelOffset, int yLevelOffset, Color color);

}
