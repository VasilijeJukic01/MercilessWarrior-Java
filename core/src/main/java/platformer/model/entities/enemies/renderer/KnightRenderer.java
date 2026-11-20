package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Knight;

import java.awt.*;
import java.awt.image.BufferedImage;

public class KnightRenderer implements EnemyRenderer<Knight> {

    private final BufferedImage[][] animations;

    public KnightRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.KNIGHT);
    }

    @Override
    public void render(Graphics g, Knight k, int xLevelOffset, int yLevelOffset) {
        renderStandard(g, k, animations, xLevelOffset, yLevelOffset, Color.BLUE);
    }
}