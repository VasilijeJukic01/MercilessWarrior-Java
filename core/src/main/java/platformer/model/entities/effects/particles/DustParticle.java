package platformer.model.entities.effects.particles;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;

/**
 * Represents a dust particle effect in the game.
 * This class handles the creation, updating, and rendering of dust particles.
 */
public class DustParticle {

    private Rectangle2D.Double particleShape;

    private final double xSpeed, ySpeed;
    private final double gravity;
    private boolean active = true;

    private final float initialAlpha = 0.7f;
    private float currentAlpha = initialAlpha;
    private final float alphaFadeSpeed;
    private Color particleColor = DUST_COLOR;

    public DustParticle(int x, int y, int size, DustType type, int playerFlipSign) {
        Random rand = new Random();
        this.particleShape = new Rectangle2D.Double(x, y, size, size);

        if (type == DustType.IMPACT) {
            this.ySpeed = -1 * (rand.nextDouble() + 0.3) * SCALE;
            this.xSpeed = (rand.nextDouble() - 0.5) * 0.8 * SCALE;
            this.gravity = 0.08 * SCALE;
            this.alphaFadeSpeed = 0.02f;
        }
        else if (type == DustType.DASH) {
            int streakWidth = (int)((rand.nextInt(10) + 15) * SCALE);
            int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
            this.particleShape = new Rectangle2D.Double(x, y, streakWidth, streakHeight);
            this.particleColor = DUST_COLOR_DASH;
            this.ySpeed = (rand.nextDouble() - 0.5) * 0.15 * SCALE;
            this.xSpeed = -playerFlipSign * (rand.nextDouble() * 1.5 + 0.5) * SCALE;
            this.gravity = 0;
            this.alphaFadeSpeed = 0.05f;
        }
        else if (type == DustType.RUNNING) {
            this.ySpeed = -0.4 * (rand.nextDouble()) * SCALE;
            this.xSpeed = (rand.nextDouble() - 0.5) * 0.1 * SCALE;
            this.gravity = 0.03 * SCALE;
            this.alphaFadeSpeed = 0.015f;
        }
        else  if (type == DustType.CRITICAL_HIT) {
            int streakWidth = (int)((rand.nextInt(10) + 20) * SCALE);
            int streakHeight = (int)((rand.nextInt(2) + 1) * SCALE);
            this.particleShape = new Rectangle2D.Double(x, y, streakWidth, streakHeight);
            this.particleColor = new Color(255, 60, 30);

            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = (rand.nextDouble() * 3.0 + 2.0) * SCALE;
            this.xSpeed = Math.cos(angle) * speed;
            this.ySpeed = Math.sin(angle) * speed;

            this.gravity = 0.05 * SCALE;
            this.alphaFadeSpeed = 0.04f;
            this.currentAlpha = 1.0f;
        }
        else {
            this.particleShape = new Rectangle2D.Double(x, y, size, size);
            this.particleColor = new Color(255, 255, 200);
            this.currentAlpha = 1.0f;
            this.alphaFadeSpeed = 0.035f;

            double angle = rand.nextDouble() * 2 * Math.PI;
            double speed = (rand.nextDouble() * 2.5 + 1.5) * SCALE;
            this.xSpeed = Math.cos(angle) * speed;
            this.ySpeed = Math.sin(angle) * speed;
            this.gravity = 0.08 * SCALE;
        }
    }

    public void update() {
        if (!active) return;

        particleShape.x += xSpeed;
        particleShape.y += ySpeed;
        particleShape.y += gravity;
        currentAlpha -= alphaFadeSpeed;
        if (currentAlpha <= 0) {
            currentAlpha = 0;
            active = false;
        }
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(particleColor.getRed()/255f, particleColor.getGreen()/255f, particleColor.getBlue()/255f, currentAlpha));
        g2d.fillRect((int) (particleShape.x - xLevelOffset), (int) (particleShape.y - yLevelOffset), (int) particleShape.width, (int) particleShape.height);
    }

    public boolean isActive() {
        return active;
    }
}
