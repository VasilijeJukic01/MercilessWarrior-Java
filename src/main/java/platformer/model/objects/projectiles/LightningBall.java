package platformer.model.objects.projectiles;

import platformer.model.Tiles;
import platformer.model.entities.Direction;

import java.awt.geom.Rectangle2D;

public class LightningBall extends Projectile{


    public LightningBall(int xPos, int yPos, Direction direction) {
        super(PRType.LIGHTNING_BALL, direction);
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? (int)(-40 * Tiles.SCALE.getValue()) : (int)(40 * Tiles.SCALE.getValue());
        int yOffset = (int)(15 * Tiles.SCALE.getValue());
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, PRSet.LB_WID.getValue()/3, PRSet.LB_HEI.getValue()/3.5);
    }
}
