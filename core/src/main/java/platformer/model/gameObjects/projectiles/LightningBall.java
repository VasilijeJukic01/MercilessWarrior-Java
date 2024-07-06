package platformer.model.gameObjects.projectiles;

import platformer.animation.graphics.WaveAnim;
import platformer.model.entities.Direction;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class LightningBall extends Projectile {

    public LightningBall(int xPos, int yPos, Direction direction) {
        super(PRType.LIGHTNING_BALL, direction);
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? -LB_OFFSET_X : LB_OFFSET_X;
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+LB_OFFSET_Y, LB_WID/3.5, LB_HEI/3.5);
        setWaveMovement();
    }

    private void setWaveMovement() {
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            Random rand = new Random();
            int dir = rand.nextInt(2);
            int d = dir == 1 ? LB_D : -LB_D;
            super.waveMovement = new WaveAnim((int)hitBox.x, (int)hitBox.y, LB_T, d, LB_PERIOD, LB_T);
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage[])) return;
        BufferedImage[] animArray = (BufferedImage[]) animations;
        int fS = 1, fC = 0;
        if (direction == Direction.RIGHT) {
            fS = -1;
            fC = LB_WID;
        }
        int x = (int)(hitBox.x - xLevelOffset + fC - 22 * SCALE);
        int y = (int)(hitBox.y - yLevelOffset - 20 * SCALE);
        g.drawImage(animArray[animIndex], x, y, fS * LB_WID, LB_HEI, null);
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }

}
