package platformer.model.levels;

import platformer.utils.Utils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.Constants.GAME_HEIGHT;
import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.FilePaths.*;

/**
 * BackgroundManager is responsible for loading and managing background images used in the game.
 */
public class BackgroundManager {

    private final Map<String, BufferedImage> backgrounds;
    private final BufferedImage defaultBackground;

    public BackgroundManager() {
        this.backgrounds = new HashMap<>();
        loadBackgrounds();
        this.defaultBackground = backgrounds.getOrDefault("FOREST1", null);
    }

    private void loadBackgrounds() {
        backgrounds.put("FOREST1", Utils.getInstance().importImage(FOREST_BG_1, GAME_WIDTH, GAME_HEIGHT));
        backgrounds.put("FOREST2", Utils.getInstance().importImage(FOREST_BG_2, GAME_WIDTH, GAME_HEIGHT));
        backgrounds.put("FOREST3", Utils.getInstance().importImage(FOREST_BG_3, GAME_WIDTH, GAME_HEIGHT));
        backgrounds.put("FOREST4", Utils.getInstance().importImage(FOREST_BG_4, GAME_WIDTH, GAME_HEIGHT));
    }

    /**
     * Retrieves a background image by its ID.
     *
     * @param id The ID of the background (e.g., "FOREST", "CAVE").
     * @return The corresponding BufferedImage, or the default background if not found.
     */
    public BufferedImage getBackground(String id) {
        return backgrounds.getOrDefault(id, defaultBackground);
    }

    public BufferedImage getDefaultBackground() {
        return defaultBackground;
    }
}
