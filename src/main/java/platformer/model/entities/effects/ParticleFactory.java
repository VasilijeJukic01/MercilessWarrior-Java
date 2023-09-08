package platformer.model.entities.effects;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ParticleFactory {

    private final Map<String, ParticleType> cache = new HashMap<>();

    public ParticleType getParticleImage(String key, BufferedImage[] animations) {
        if (!cache.containsKey(key)) {
            cache.put(key, new ParticleType(animations));
        }
        return cache.get(key);
    }

}
