package platformer.model.entities.effects.particles;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class SmokeParticle {

    private double x, y;
    private double xSpeed, ySpeed;
    private int width, height;
    private float alpha;
    private int animIndex, animTick;
    private final int animSpeed = 45;
    private final BufferedImage[] animation;
    private final Random rand = new Random();

    public SmokeParticle(BufferedImage[] animation) {
        this.animation = animation;
        initialize(true);
    }

    private void reset() {
        initialize(false);
    }

    private void initialize(boolean isInitial) {
        this.width = (int) ((rand.nextInt(100) + 250) * SCALE);
        this.height = (int) (this.width * (3.0 / 7.0));
        this.x = rand.nextInt(GAME_WIDTH);

        if (isInitial) this.y = rand.nextInt(GAME_HEIGHT + this.height) - this.height;
        else this.y = GAME_HEIGHT + rand.nextInt(50);

        this.xSpeed = (rand.nextDouble() - 0.5) * 0.05 * SCALE;
        this.ySpeed = -(rand.nextDouble() * 0.04 + 0.02) * SCALE;
        this.alpha = (rand.nextFloat() * 0.2f) + 0.6f;
        this.animIndex = rand.nextInt(animation.length);
    }

    public void update() {
        x += xSpeed;
        y += ySpeed;
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex = (animIndex + 1) % animation.length;
        }
        if (y < -height) reset();
    }

    public void render(Graphics2D g2d) {
        Composite originalComposite = g2d.getComposite();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0, alpha)));
        g2d.drawImage(animation[animIndex], (int)x, (int)y, width, height, null);
        g2d.setComposite(originalComposite);
    }
}