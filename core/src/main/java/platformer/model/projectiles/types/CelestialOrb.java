package platformer.model.projectiles.types;

import platformer.model.entities.player.Player;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import static platformer.constants.Constants.*;

public class CelestialOrb extends Projectile {

    private final double angle;
    private static final double SPEED = 1.7 * SCALE;
    private double rotationAngle = 0;
    private static final double ROTATION_SPEED = 0.025 * SCALE;

    public CelestialOrb(int xPos, int yPos, double angle) {
        super(PRType.CELESTIAL_ORB, null);
        super.animate = true;
        this.angle = angle;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        super.hitBox = new Rectangle2D.Double(xPos, yPos, CELESTIAL_ORB_WID /(1.5 * SCALE), CELESTIAL_ORB_HEI / (1.5 * SCALE));
    }

    @Override
    public void updatePosition(Player player) {
        hitBox.x += Math.cos(angle) * SPEED;
        hitBox.y += Math.sin(angle) * SPEED;
        rotationAngle += ROTATION_SPEED;
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage[])) return;
        BufferedImage[] animArray = (BufferedImage[]) animations;

        int centerX = (int) (hitBox.getCenterX() - xLevelOffset);
        int centerY = (int) (hitBox.getCenterY() - yLevelOffset);
        int renderWidth = CELESTIAL_ORB_WID;
        int renderHeight = CELESTIAL_ORB_HEI;

        Graphics2D g2d = (Graphics2D) g.create();
        try {
            g2d.translate(centerX, centerY);
            g2d.rotate(rotationAngle);
            g2d.drawImage(animArray[animIndex], -renderWidth / 2, -renderHeight / 2, renderWidth, renderHeight, null);
        } finally {
            g2d.dispose();
        }

        renderHitBox(g, xLevelOffset, yLevelOffset, Color.red);
    }

    @Override
    public Shape getShapeBounds() {
        return hitBox;
    }
}