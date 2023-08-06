package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.Skeleton;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.Constants.SKELETON_HEIGHT;

public class SkeletonRenderer implements EnemyRenderer<Skeleton> {

    private final BufferedImage[][] animations;

    public SkeletonRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, Skeleton s, int xLevelOffset, int yLevelOffset) {
        int fC = s.getFlipCoefficient(), fS = s.getFlipSign();
        int x = (int) s.getHitBox().x - SKELETON_X_OFFSET - xLevelOffset + fC;
        int y = (int) s.getHitBox().y - SKELETON_Y_OFFSET - yLevelOffset + (int)s.getPushOffset() + 1;
        g.drawImage(animations[s.getEnemyAction().ordinal()][s.getAnimIndex()], x, y, SKELETON_WIDTH * fS, SKELETON_HEIGHT, null);
        s.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        s.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }
}
