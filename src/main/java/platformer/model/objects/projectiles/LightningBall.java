package platformer.model.objects.projectiles;

import platformer.animation.graphics.WaveAnim;
import platformer.model.entities.Direction;

import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;

public class LightningBall extends Projectile{

    public LightningBall(int xPos, int yPos, Direction direction) {
        super(PRType.LIGHTNING_BALL, direction);
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? (int)(-40 * SCALE) : (int)(40 * SCALE);
        int yOffset = (int)(15 * SCALE);
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, LB_WID/3.5, LB_HEI/3.5);
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            Random rand = new Random();
            int dir = rand.nextInt(2);
            int period = (int)(50*SCALE), t = (int)(1*SCALE);
            int d = (int)(25*SCALE);
            super.waveMovement = new WaveAnim((int)hitBox.x, (int)hitBox.y, t, dir == 1 ? d : -d, period, t);
        }
    }
}
