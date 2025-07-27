package platformer.model.effects.particles;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AmbientParticleType {

    private final BufferedImage[] animations;

    public AmbientParticleType(BufferedImage[] animations) {
        this.animations = animations;
    }

    public BufferedImage[] getAnimations() {
        return animations;
    }

    public void render(Graphics g, int animIndex, int xPos, int yPos, int size) {
        try {
            g.drawImage(animations[animIndex], xPos, yPos, size, size, null);
        }
        catch (Exception ignored){}
    }

}
