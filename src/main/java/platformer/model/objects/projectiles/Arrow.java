package platformer.model.objects.projectiles;

import platformer.model.entities.Direction;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

public class Arrow extends Projectile{

    public Arrow(int xPos, int yPos, Direction direction) {
        super(PRType.ARROW, direction);
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? (int)(-20 * SCALE) : (int)(10 * SCALE);
        int yOffset = (int)(20 * SCALE);
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, ARROW_WID, ARROW_HEI);
    }

}
