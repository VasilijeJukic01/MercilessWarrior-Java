package platformer.model.entities.enemies.renderer;

import platformer.animation.Animation;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricState;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class RoricRenderer implements EnemyRenderer<Roric> {

    private final BufferedImage[][] animations;
    private final BufferedImage[] auraAnimations;
    private double pulseTimer = 0.0;
    private double auraRotationAngle = 0;
    private static final double AURA_ROTATION_SPEED = 0.001;

    public RoricRenderer(BufferedImage[][] animations) {
        this.animations = animations;
        this.auraAnimations = Animation.getInstance().loadRoricAuraEffect();
    }

    @Override
    public void render(Graphics g, Roric roric, int xLevelOffset, int yLevelOffset) {
        if (!roric.isVisible()) return;
        int fC = roric.getFlipCoefficient(), fS = roric.getFlipSign();
        int x = (int) roric.getHitBox().x - RORIC_X_OFFSET - xLevelOffset + fC;
        int y = (int) roric.getHitBox().y - RORIC_Y_OFFSET - yLevelOffset + (int)roric.getPushOffset() + 1;
        if (roric.getDirection() == Direction.RIGHT) x -= (int)(25 * SCALE);
        else if (fS == -1) x -= (int) (21 * SCALE);
        g.drawImage(animations[roric.getEnemyAction().ordinal()][roric.getAnimIndex()], x, y, RORIC_WIDTH * fS, RORIC_HEIGHT, null);
        if (roric.getState() == RoricState.CELESTIAL_RAIN) renderAura(g, roric, xLevelOffset, yLevelOffset);
        roric.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        roric.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderAura(Graphics g, Roric roric, int xLevelOffset, int yLevelOffset) {
        pulseTimer += 0.05;
        if (pulseTimer > Math.PI * 2) pulseTimer -= Math.PI * 2;

        // Sine modulation
        double pulse = (1 + Math.sin(pulseTimer)) / 2.0;
        double scale = 0.9 + (pulse * 0.2);
        float alpha = (float) (0.6 + (pulse * 0.4));

        int auraAnimIndex = (int) ((System.currentTimeMillis() / 100) % auraAnimations.length);
        BufferedImage auraFrame = auraAnimations[auraAnimIndex];
        int scaledWidth = (int) (auraFrame.getWidth() * scale);
        int scaledHeight = (int) (auraFrame.getHeight() * scale);
        int centerX = (int) (roric.getHitBox().getCenterX() - xLevelOffset);
        int centerY = (int) (roric.getHitBox().getCenterY() - yLevelOffset);

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g2d.translate(centerX, centerY);
            g2d.rotate(auraRotationAngle);
            g2d.drawImage(auraFrame, -scaledWidth / 2, -scaledHeight / 2, scaledWidth, scaledHeight, null);

        } finally {
            g2d.dispose();
        }
        auraRotationAngle += AURA_ROTATION_SPEED;
        if (auraRotationAngle > Math.PI * 2) auraRotationAngle -= Math.PI * 2;
    }
}
