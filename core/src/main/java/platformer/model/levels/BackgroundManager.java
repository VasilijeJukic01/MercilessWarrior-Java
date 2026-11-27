package platformer.model.levels;

import platformer.animation.AssetManager;
import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.FilePaths.*;

/**
 * Lazily loads backgrounds into a bounded LRU cache to keep VRAM/heap usage constant across many biomes.
 */
public class BackgroundManager {

    private static final int MAX_CACHED_BACKGROUNDS = 6;
    private static final String BACKGROUNDS_DIR = "/images/levels/backgrounds/";

    private record BackgroundSpec(String path, boolean scaleToViewport) {}

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
            Map.entry("FOREST_HUB", new BackgroundSpec(FOREST_HUB_BG, true)),
            Map.entry("FOREST_CONNECTOR", new BackgroundSpec(FOREST_CONNECTOR_BG, true)),
            Map.entry("FOREST_RUINS", new BackgroundSpec(FOREST_RUINS_BG, true)),
            Map.entry("FOREST_CAVES", new BackgroundSpec(FOREST_CAVES_BG, true)),
            Map.entry("THORNS_TRIALS", new BackgroundSpec(THORNS_TRIALS_BG, true)),
            Map.entry("THORNS_PIT", new BackgroundSpec(THORNS_PIT_BG, true)),

            Map.entry("TAVERN", new BackgroundSpec(TAVERN_BG, false)),
            Map.entry("CRYPT1", new BackgroundSpec(CRYPT1_BG, false)),
            Map.entry("CRYPT2", new BackgroundSpec(CRYPT2_BG, false)),
            Map.entry("GOBLIN_CAVE", new BackgroundSpec(GOBLIN_CAVE_BG, false)),
            Map.entry("CAVE1", new BackgroundSpec(CAVE1_BG, false))
    );

    private static final Map<String, BufferedImage> CACHE = Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, BufferedImage> eldest) {
                    return size() > MAX_CACHED_BACKGROUNDS;
                }
            }
    );

    public BufferedImage getBackground(String id) {
        return getBackground(id, true);
    }

    /**
     * Retrieves a background image by ID. Supports both explicit REGISTRY entries and automatic convention lookup at /images/levels/backgrounds/<id>.png for future biomes & interiors.
     */
    public BufferedImage getBackground(String id, boolean defaultScaleToViewport) {
        if (id == null || id.isBlank()) return getDefaultBackground();

        String cacheKey = id + ":" + defaultScaleToViewport;
        BufferedImage cached = CACHE.get(cacheKey);
        if (cached != null) return cached;

        BackgroundSpec spec = REGISTRY.get(id);
        if (spec == null) {
            String autoPath = BACKGROUNDS_DIR + id + ".png";
            if (!AssetManager.getInstance().hasAsset(autoPath)) {
                autoPath = BACKGROUNDS_DIR + id.toLowerCase() + ".png";
            }
            if (!AssetManager.getInstance().hasAsset(autoPath)) {
                return getDefaultBackground();
            }
            spec = new BackgroundSpec(autoPath, defaultScaleToViewport);
        }

        BufferedImage img = spec.scaleToViewport() ? ImageUtils.importImage(spec.path(), GAME_WIDTH, GAME_HEIGHT) : ImageUtils.importImage(spec.path(), -1, -1);

        if (img != null) {
            CACHE.put(cacheKey, img);
            return img;
        }
        return getDefaultBackground();
    }

    public BufferedImage getDefaultBackground() {
        return getBackground("FOREST1", true);
    }
}