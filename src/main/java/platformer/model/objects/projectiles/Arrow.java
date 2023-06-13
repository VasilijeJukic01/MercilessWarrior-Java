package platformer.model.objects.projectiles;

import platformer.model.Tiles;
import platformer.model.entities.Direction;

import java.awt.geom.Rectangle2D;

public class Arrow extends Projectile{

    public Arrow(int xPos, int yPos, Direction direction) {
        super(PRType.ARROW, direction);
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? (int)(-20 * Tiles.SCALE.getValue()) : (int)(10 * Tiles.SCALE.getValue());
        int yOffset = (int)(20 * Tiles.SCALE.getValue());
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, PRSet.ARROW_WID.getValue(), PRSet.ARROW_HEI.getValue());
    }

}
