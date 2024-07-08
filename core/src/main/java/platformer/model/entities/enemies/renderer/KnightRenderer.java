package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Knight;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class KnightRenderer implements EnemyRenderer<Knight> {

    private final BufferedImage[][] animations;

    public KnightRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, Knight k, int xLevelOffset, int yLevelOffset) {
        int fC = k.getFlipCoefficient(), fS = k.getFlipSign();
        int x = (int) k.getHitBox().x - KNIGHT_X_OFFSET - xLevelOffset + fC;
        int y = (int) k.getHitBox().y - KNIGHT_Y_OFFSET - yLevelOffset + (int)k.getPushOffset() + 1;
        g.drawImage(animations[k.getEnemyAction().ordinal()][k.getAnimIndex()], x, y, KNIGHT_WIDTH * fS, KNIGHT_HEIGHT, null);
        k.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        k.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

}
