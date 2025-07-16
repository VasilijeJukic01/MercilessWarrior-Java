package platformer.model.entities.enemies.renderer;

import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class RoricRenderer implements EnemyRenderer<Roric> {

    private final BufferedImage[][] animations;

    public RoricRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, Roric roric, int xLevelOffset, int yLevelOffset) {
        int fC = roric.getFlipCoefficient(), fS = roric.getFlipSign();
        int x = (int) roric.getHitBox().x - RORIC_X_OFFSET - xLevelOffset + fC;
        int y = (int) roric.getHitBox().y - RORIC_Y_OFFSET - yLevelOffset + (int)roric.getPushOffset() + 1;
        if (roric.getDirection() == Direction.RIGHT) x -= (int)(25 * SCALE);
        else if (fS == -1) x -= (int) (21 * SCALE);
        g.drawImage(animations[roric.getEnemyAction().ordinal()][roric.getAnimIndex()], x, y, RORIC_WIDTH * fS, RORIC_HEIGHT, null);
        roric.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        roric.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }
}
