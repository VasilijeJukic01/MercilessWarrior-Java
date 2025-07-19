package platformer.model.gameObjects.projectiles;

import platformer.model.entities.player.Player;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import static platformer.constants.Constants.*;

public class CelestialOrb extends Projectile {

    private final double angle;
    private static final double SPEED = 1.7 * SCALE;
    private double rotationAngle = 0;
    private static final double ROTATION_SPEED = 0.05;

    public CelestialOrb(int xPos, int yPos, double angle) {
        super(PRType.CELESTIAL_ORB, null);
        super.animate = true;
        this.angle = angle;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        super.hitBox = new Rectangle2D.Double(xPos, yPos, CELESTIAL_ORB_WID / 3.0, CELESTIAL_ORB_HEI / 3.0);
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

        int x = (int) (hitBox.getCenterX() - xLevelOffset - (CELESTIAL_ORB_WID / 2.0));
        int y = (int) (hitBox.getCenterY() - yLevelOffset - (CELESTIAL_ORB_HEI / 2.0));

        Graphics2D g2d = (Graphics2D) g.create();
        AffineTransform transform = new AffineTransform();
        transform.rotate(rotationAngle, x + CELESTIAL_ORB_WID / 2.0, y + CELESTIAL_ORB_HEI / 2.0);
        g2d.setTransform(transform);
        g2d.drawImage(animArray[animIndex], x, y, CELESTIAL_ORB_WID, CELESTIAL_ORB_HEI, null);
        g2d.dispose();

        renderHitBox(g, xLevelOffset, yLevelOffset, Color.red);
    }

    @Override
    public Shape getHitBox() {
        return hitBox;
    }
}