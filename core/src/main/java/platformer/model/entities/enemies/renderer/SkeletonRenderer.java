package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Skeleton;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SkeletonRenderer implements EnemyRenderer<Skeleton> {

    private final BufferedImage[][] animations;

    public SkeletonRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.SKELETON);
    }

    @Override
    public void render(Graphics g, Skeleton s, int xLevelOffset, int yLevelOffset) {
        renderStandard(g, s, animations, xLevelOffset, yLevelOffset, Color.BLUE);
    }
}
