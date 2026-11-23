package platformer.model.entities.enemies.renderer;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.loader.EnemyDefinition;
import platformer.model.entities.enemies.types.Goblin;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.SCALE;

/**
 * Renders any of the 5 Goblin Pack variants with parry rim outlines, stun stars, and comical fear visuals.
 */
public class GoblinRenderer implements EnemyRenderer<Goblin> {

    private final BufferedImage[][] animations;

    public GoblinRenderer(EnemyType type) {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(type);
    }

    @Override
    public void render(Graphics g, Goblin goblin, int xLevelOffset, int yLevelOffset) {
        EnemyDefinition def = goblin.getDefinition();
        int fC = goblin.getFlipCoefficient(), fS = goblin.getFlipSign();
        int trembleX = goblin.isCowering() ? (int) Math.round(Math.sin(System.currentTimeMillis() * 0.06) * (1.2 * SCALE)) : 0;

        int x = (int) goblin.getHitBox().x - def.getXOffset() - xLevelOffset + fC + trembleX;
        int y = (int) goblin.getHitBox().y - def.getYOffset() - yLevelOffset + (int) goblin.getPushOffset() + 1;

        drawEnemySprite(g, goblin, animations[goblin.getEnemyAction().ordinal()][goblin.getAnimIndex()], x, y, def.getDrawWidth() * fS, def.getDrawHeight());
        if (goblin.isTerrified() && goblin.getEnemyAction() != Anim.DEATH && (goblin.getEnemyAction() == Anim.RUN || goblin.isCowering())) {
            renderFearIndicator(g, goblin, xLevelOffset, yLevelOffset);
        }

        goblin.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.GREEN);
        goblin.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderFearIndicator(Graphics g, Goblin goblin, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double bob = Math.sin(System.currentTimeMillis() * 0.014) * (2.0 * SCALE);
        int cx = (int) Math.round(goblin.getHitBox().getCenterX() - xLevelOffset);
        int topY = (int) Math.round(goblin.getHitBox().y - yLevelOffset - (12 * SCALE) + bob);

        // Panic exclamation mark
        g2d.setColor(new Color(255, 230, 80, 240));
        g2d.fillRoundRect(cx - (int) (1.2 * SCALE), topY - (int) (4 * SCALE), (int) (2.4 * SCALE), (int) (6.5 * SCALE), 2, 2);
        g2d.fillOval(cx - (int) (1.3 * SCALE), topY + (int) (3.8 * SCALE), (int) (2.6 * SCALE), (int) (2.6 * SCALE));

        g2d.dispose();
    }
}