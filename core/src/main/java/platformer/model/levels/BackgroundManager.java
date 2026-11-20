package platformer.model.levels;

import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.FilePaths.*;

/**
 * Lazily loads and caches level background images on demand to minimize startup time and memory usage.
 */
public class BackgroundManager {

    private record BackgroundSpec(String path, boolean scaleToViewport) { }

    private static final Map<String, BackgroundSpec> REGISTRY = Map.ofEntries(
            Map.entry("FOREST1", new BackgroundSpec(FOREST_BG_1, true)),
            Map.entry("FOREST2", new BackgroundSpec(FOREST_BG_2, true)),
            Map.entry("FOREST3", new BackgroundSpec(FOREST_BG_3, true)),
            Map.entry("FOREST4", new BackgroundSpec(FOREST_BG_4, true)),
            Map.entry("FOREST_ENTRANCE", new BackgroundSpec(FOREST_ENTRANCE_BG, true)),
            Map.entry("FOREST_ROOTS", new BackgroundSpec(FOREST_ROOTS_BG, true)),
            Map.entry("FOREST_DEEP", new BackgroundSpec(FOREST_DEEP_BG, true)),
            Map.entry("FOREST_STORM_PEAK", new BackgroundSpec(FOREST_STORM_PEAK_BG, true)),
            Map.entry("FOREST_VILLAGE", new BackgroundSpec(FOREST_VILLAGE_BG, true)),
            Map.entry("FOREST_VILLAGE_ENTRANCE", new BackgroundSpec(FOREST_VILLAGE_ENTRANCE_BG, true)),
            Map.entry("RORIC_ARENA", new BackgroundSpec(RORIC_ARENA_BG, true)),
            Map.entry("FOREST_THORNS", new BackgroundSpec(FOREST_THORNS_BG, true)),
            Map.entry("TAVERN", new BackgroundSpec(TAVERN_BG, false)),
            Map.entry("CRYPT1", new BackgroundSpec(CRYPT1_BG, false)),
            Map.entry("CRYPT2", new BackgroundSpec(CRYPT2_BG, false)),
            Map.entry("GOBLIN_CAVE", new BackgroundSpec(GOBLIN_CAVE_BG, false))
    );

    private static final Map<String, BufferedImage> CACHE = new ConcurrentHashMap<>();

    /**
     * Retrieves a background image by its ID, loading and caching it on first access.
     */
    public BufferedImage getBackground(String id) {
        if (id == null || !REGISTRY.containsKey(id)) return getDefaultBackground();
        return CACHE.computeIfAbsent(id, this::loadFromSpec);
    }

    public BufferedImage getDefaultBackground() {
        return CACHE.computeIfAbsent("FOREST1", this::loadFromSpec);
    }

    private BufferedImage loadFromSpec(String id) {
        BackgroundSpec spec = REGISTRY.get(id);
        if (spec == null) return null;
        if (spec.scaleToViewport()) {
            return ImageUtils.importImage(spec.path(), GAME_WIDTH, GAME_HEIGHT);
        }
        else return ImageUtils.importImage(spec.path(), -1, -1);
    }
}