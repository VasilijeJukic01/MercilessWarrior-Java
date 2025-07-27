package platformer.model.effects.particles;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class for creating ParticleType objects.
 * <p>
 * Part of the Flyweight design pattern.
 */
public class AmbientParticleFactory {

    private final Map<String, AmbientParticleType> cache = new HashMap<>();

    public AmbientParticleType getParticleImage(String key, BufferedImage[] animations) {
        if (!cache.containsKey(key)) {
            cache.put(key, new AmbientParticleType(animations));
        }
        return cache.get(key);
    }

}
