package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Generic data-driven renderer for any enemy defined in enemies.json.
 * Uses {@link EnemyRenderer#renderStandard} to render animations, parry rims, and 3D stun stars.
 */
public class StandardEnemyRenderer<T extends Enemy> implements EnemyRenderer<T> {

    private final BufferedImage[][] animations;
    private final Color debugHitboxColor;

    public StandardEnemyRenderer(EnemyType type) {
        this(type, Color.BLUE);
    }

    public StandardEnemyRenderer(EnemyType type, Color debugHitboxColor) {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(type);
        this.debugHitboxColor = debugHitboxColor;
    }

    @Override
    public void render(Graphics g, T enemy, int xLevelOffset, int yLevelOffset) {
        renderStandard(g, enemy, animations, xLevelOffset, yLevelOffset, debugHitboxColor);
    }
}