package platformer.model.effects.particles;

import platformer.model.entities.Entity;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents a single particle instance in the game.
 * This class follows a "Context" role in the Strategy design pattern. It holds the state of a particle but delegates all of its
 * behavioral logic (initialization, updates, rendering) to a concrete {@link ParticleBehavior} implementation.
 */
@Getter
@Setter
public class DustParticle {

    private final ParticleBehavior behavior;
    private final Entity target;
    private final DustType type;

    // Core Particle State
    private Rectangle2D.Double particleShape;
    private double xSpeed, ySpeed, gravity;
    private float currentAlpha, alphaFadeSpeed, initialAlpha;
    private Color particleColor;
    private boolean active = true;

    // State for Complex Behaviors
    private Point2D.Double offset;
    private double angle;
    private double pulsePhase;
    private int lineLength;
    private double lineAngle;

    public DustParticle(int x, int y, int size, DustType type, ParticleBehavior behavior, Entity target, int playerFlipSign) {
        this.particleShape = new Rectangle2D.Double(x, y, size, size);
        this.type = type;
        this.behavior = behavior;
        this.target = target;
        this.initialAlpha = 0.7f;
        this.currentAlpha = initialAlpha;

        behavior.init(this, new Random());
    }

    /**
     * Updates the particle's state for one frame. Delegates the specific
     * movement logic to its behavior and then applies the universal fade-out effect.
     */
    public void update() {
        if (!active) return;
        behavior.update(this);
        currentAlpha -= alphaFadeSpeed;
        if (currentAlpha <= 0) {
            currentAlpha = 0;
            active = false;
        }
    }

    /**
     * Renders the particle. Delegates the rendering logic to its behavior strategy.
     *
     * @param g The graphics context.
     * @param xLevelOffset The horizontal camera offset.
     * @param yLevelOffset The vertical camera offset.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(particleColor.getRed() / 255f, particleColor.getGreen() / 255f, particleColor.getBlue() / 255f, currentAlpha));
        behavior.render(this, g2d, xLevelOffset, yLevelOffset);
    }

    /**
     * A helper method for rendering, providing a translated shape based on camera offsets.
     */
    public Shape getTranslatedShape(int xLevelOffset, int yLevelOffset) {
        return new Rectangle2D.Double(particleShape.x - xLevelOffset, particleShape.y - yLevelOffset, particleShape.width, particleShape.height);
    }
}
