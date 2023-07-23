package platformer.model.objects.projectiles;

import platformer.animation.graphics.WaveAnim;
import platformer.model.Tiles;
import platformer.model.entities.Direction;

import java.awt.geom.Rectangle2D;
import java.util.Random;

public class LightningBall extends Projectile{

    public LightningBall(int xPos, int yPos, Direction direction) {
        super(PRType.LIGHTNING_BALL, direction);
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? (int)(-40 * Tiles.SCALE.getValue()) : (int)(40 * Tiles.SCALE.getValue());
        int yOffset = (int)(15 * Tiles.SCALE.getValue());
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, PRSet.LB_WID.getValue()/3.5, PRSet.LB_HEI.getValue()/3.5);
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            Random rand = new Random();
            int dir = rand.nextInt(2);
            int period = (int)(50*Tiles.SCALE.getValue()), t = (int)(1*Tiles.SCALE.getValue());
            int d = (int)(25*Tiles.SCALE.getValue());
            super.waveMovement = new WaveAnim((int)hitBox.x, (int)hitBox.y, t, dir == 1 ? d : -d, period, t);
        }
    }
}
