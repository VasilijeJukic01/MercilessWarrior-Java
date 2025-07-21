package platformer.model.effects.particles;

import java.awt.*;
import java.util.Random;

import static platformer.constants.AnimConstants.DEFAULT_PARTICLE_FRAMES;
import static platformer.constants.Constants.PARTICLE_SHIFT;

/**
 * This class represents a Particle in the game.
 * <p>
 * It is a Flyweight class, meaning it only contains the intrinsic state (the state that remains the same across all instances).
 * The extrinsic state (the state that varies across instances) should be passed in as arguments to the methods that use it.
 * This approach is used to save memory when a large number of similar objects need to be created.
 */
public class AmbientParticle {

    private final Random rand = new Random();
    private final int size;
    private double xPos, yPos;
    private int animSpeed = rand.nextInt(100-50)+50;
    private int animTick = 0, animIndex = 0;
    boolean xDir = rand.nextBoolean(), yDir = rand.nextBoolean();

    private final AmbientParticleType ambientParticleType;

    public AmbientParticle(AmbientParticleType ambientParticleType, int size, int xPos, int yPos) {
        this.ambientParticleType = ambientParticleType;
        this.size = size;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            xDir = rand.nextBoolean();
            yDir = rand.nextBoolean();
            animSpeed = rand.nextInt(100-50)+50;
            animIndex++;
            if (animIndex >= DEFAULT_PARTICLE_FRAMES) animIndex = 0;
            animTick = 0;
        }
    }

    public void update() {
        updateAnimation();
        xPos += xDir ? PARTICLE_SHIFT : -PARTICLE_SHIFT;
        yPos += yDir ? PARTICLE_SHIFT : -PARTICLE_SHIFT;
    }

    public void render(Graphics g) {
        ambientParticleType.render(g, animIndex, (int)xPos, (int)yPos, size);
    }

}
