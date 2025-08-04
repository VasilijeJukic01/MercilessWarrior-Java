package platformer.model.projectiles.types;

import platformer.animation.SpriteManager;
import platformer.animation.graphics.WaveAnim;
import platformer.model.entities.Direction;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class LightningBall extends Projectile {

    private final BufferedImage[] lightningBallAnims;
    private final BufferedImage[] energyBallAnims;

    public LightningBall(int xPos, int yPos, Direction direction) {
        super(PRType.LIGHTNING_BALL, direction);
        this.lightningBallAnims = SpriteManager.getInstance().getLightningBallAnimations();
        this.energyBallAnims = SpriteManager.getInstance().getEnergyBallAnimations();
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? -LB_OFFSET_X : LB_OFFSET_X;
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+LB_OFFSET_Y, LB_WID/ (1.75 * SCALE), LB_HEI/ (1.75 * SCALE));
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
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        BufferedImage[] animArray = (direction == Direction.LEFT || direction == Direction.RIGHT) ? this.lightningBallAnims : this.energyBallAnims;
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

    @Override
    public Shape getShapeBounds() {
        return super.hitBox;
    }

}
