package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Ghoul;

import java.awt.*;
import java.awt.image.BufferedImage;

public class GhoulRenderer implements EnemyRenderer<Ghoul> {

    private final BufferedImage[][] animations;

    public GhoulRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.GHOUL);
    }

    @Override
    public void render(Graphics g, Ghoul gh, int xLevelOffset, int yLevelOffset) {
        renderStandard(g, gh, animations, xLevelOffset, yLevelOffset, Color.BLUE);
    }
}