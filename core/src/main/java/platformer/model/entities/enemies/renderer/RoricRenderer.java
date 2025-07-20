package platformer.model.entities.enemies.renderer;

import platformer.animation.Animation;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.enemies.boss.roric.RoricState;

import java.awt.*;
import java.awt.geom.AffineTransform;
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
        int originalWidth = auraFrame.getWidth();
        int originalHeight = auraFrame.getHeight();
        int scaledWidth = (int) (originalWidth * scale);
        int scaledHeight = (int) (originalHeight * scale);
        int auraX = (int) (roric.getHitBox().getCenterX() - xLevelOffset - (scaledWidth / 2.0));
        int auraY = (int) (roric.getHitBox().getCenterY() - yLevelOffset - (scaledHeight / 2.0));

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        // Rotation
        AffineTransform transform = new AffineTransform();
        transform.rotate(auraRotationAngle, roric.getHitBox().getCenterX() - xLevelOffset, roric.getHitBox().getCenterY() - yLevelOffset);
        g2d.setTransform(transform);
        g2d.drawImage(auraFrame, auraX, auraY, scaledWidth, scaledHeight, null);
        g2d.dispose();

        auraRotationAngle += AURA_ROTATION_SPEED;
        if (auraRotationAngle > Math.PI * 2) auraRotationAngle -= Math.PI * 2;
    }
}
