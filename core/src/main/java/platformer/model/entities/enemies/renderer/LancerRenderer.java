package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.boss.Lancer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class LancerRenderer implements EnemyRenderer<Lancer> {

    private final BufferedImage[][] animations;

    public LancerRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, Lancer lancer, int xLevelOffset, int yLevelOffset) {
        int fC = lancer.getFlipCoefficient(), fS = lancer.getFlipSign();
        int x = (int) lancer.getHitBox().x - LANCER_X_OFFSET - xLevelOffset + fC;
        int y = (int) lancer.getHitBox().y - LANCER_Y_OFFSET - yLevelOffset + (int) lancer.getPushOffset() + 1;
        if (fS == -1) x -= 21*SCALE;
        g.drawImage(animations[lancer.getEnemyAction().ordinal()][lancer.getAnimIndex()], x, y, LANCER_WIDTH * fS, LANCER_HEIGHT, null);
        lancer.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        lancer.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }
}
