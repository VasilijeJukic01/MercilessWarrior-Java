package platformer.model.entities.effects;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.AnimConstants.DEFAULT_PARTICLE_FRAMES;
import static platformer.constants.Constants.PARTICLE_SHIFT;

@SuppressWarnings("FieldCanBeLocal")
public class Particle {

    private final BufferedImage[] animations;
    private final Random rand = new Random();
    private final int size;
    private double xPos, yPos;
    private int animSpeed = rand.nextInt(100-50)+50;
    private int animTick = 0, animIndex = 0;
    boolean xDir = rand.nextBoolean(), yDir = rand.nextBoolean();

    public Particle(BufferedImage[] animations, int xPos, int yPos) {
        this.animations = animations;
        this.size = animations[0].getWidth();
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
        try {
            g.drawImage(animations[animIndex], (int)xPos, (int)yPos, size, size, null);
        }
        catch (Exception ignored){}
    }

}
