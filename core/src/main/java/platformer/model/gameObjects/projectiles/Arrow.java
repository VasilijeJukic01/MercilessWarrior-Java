package platformer.model.gameObjects.projectiles;

import platformer.model.entities.Direction;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Arrow extends Projectile {

    public Arrow(int xPos, int yPos, Direction direction) {
        super(PRType.ARROW, direction);
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? ARROW_OFFSET_X_RIGHT : ARROW_OFFSET_X_LEFT;
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+ARROW_OFFSET_Y, ARROW_WID, ARROW_HEI);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage)) return;
        int fS = 1, fC = 0;
        if (direction == Direction.LEFT) {
            fS = -1;
            fC = (int) (32 * SCALE);
        }
        int x = (int)hitBox.x - xLevelOffset + fC;
        int y = (int)hitBox.y - yLevelOffset;
        g.drawImage((BufferedImage)animations, x, y, fS * ARROW_WID, ARROW_HEI, null);
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }
}
