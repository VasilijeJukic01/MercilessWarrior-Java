package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.CorruptedKnight;
import platformer.model.entities.enemies.EnemyType;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Renders the Corrupted Knight with a pulsing purple void aura and dark-violet shadow afterimages during lunges.
 */
public class CorruptedKnightRenderer implements EnemyRenderer<CorruptedKnight> {

    private final BufferedImage[][] animations;

    public CorruptedKnightRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.CORRUPTED_KNIGHT);
    }

    @Override
    public void render(Graphics g, CorruptedKnight ck, int xLevelOffset, int yLevelOffset) {
        int fC = ck.getFlipCoefficient(), fS = ck.getFlipSign();
        int worldX = (int) ck.getHitBox().x - KNIGHT_X_OFFSET + fC;
        int worldY = (int) ck.getHitBox().y - KNIGHT_Y_OFFSET + (int) ck.getPushOffset() + 1;

        AfterimageTrail trail = ck.getAfterimageTrail();
        if (ck.isShadowStepping()) {
            trail.record(worldX, worldY, fS, ck.getEnemyAction().ordinal(), ck.getAnimIndex());
            trail.render(g, animations, KNIGHT_WIDTH, KNIGHT_HEIGHT, xLevelOffset, yLevelOffset, 0.35f);
        }
        else trail.clear();

        renderVoidAura(g, ck, xLevelOffset, yLevelOffset);

        int x = worldX - xLevelOffset;
        int y = worldY - yLevelOffset;
        drawEnemySprite(g, ck, animations[ck.getEnemyAction().ordinal()][ck.getAnimIndex()], x, y, KNIGHT_WIDTH * fS, KNIGHT_HEIGHT);
        ck.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
        ck.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderVoidAura(Graphics g, CorruptedKnight ck, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double pulse = 0.8 + 0.2 * Math.sin(System.currentTimeMillis() * 0.006);
        int cx = (int) (ck.getHitBox().getCenterX() - xLevelOffset);
        int cy = (int) (ck.getHitBox().getCenterY() - yLevelOffset + 4 * SCALE);
        int r = (int) (24 * SCALE * pulse);

        RadialGradientPaint aura = new RadialGradientPaint(
                new Point2D.Float(cx, cy),
                r,
                new float[]{0.0f, 0.6f, 1.0f},
                new Color[]{new Color(145, 35, 235, 95), new Color(85, 15, 145, 45), new Color(40, 0, 80, 0)}
        );
        g2d.setPaint(aura);
        g2d.fillOval(cx - r, cy - r, r * 2, r * 2);
        g2d.dispose();
    }
}