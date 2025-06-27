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
        else {
            this.ySpeed = -0.4 * (rand.nextDouble()) * SCALE;
            this.xSpeed = (rand.nextDouble() - 0.5) * 0.1 * SCALE;
            this.gravity = 0.03 * SCALE;
            this.alphaFadeSpeed = 0.015f;
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
