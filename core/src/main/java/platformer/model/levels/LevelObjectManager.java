package platformer.model.levels;

import platformer.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.FilePaths.DECORATION_SPRITES;

/**
 * This class is responsible for managing the level objects in the game.
 * It holds references to all the level objects and provides methods for loading their images.
 */
public class LevelObjectManager {

    private final Map<String, BufferedImage> models = new HashMap<>();

    public LevelObjectManager() {

    }

    /**
     * Retrieves the image for a given decoration ID from a specific tileset folder.
     * If the image is not already in the cache, it attempts to load it, caches it, and returns it.
     *
     * @param id The ID of the decoration.
     * @param tilesetName The name of the tileset, which corresponds to the subfolder.
     * @param filename The specific filename to use, which may differ from the ID.
     * @param isFlipped If true, the loaded image will be flipped horizontally.
     * @return The BufferedImage for the decoration, or null if it cannot be loaded.
     */
    public BufferedImage getModel(String id, String tilesetName, String filename, boolean isFlipped) {
        String cacheKey = tilesetName + ":" + id + (isFlipped ? ":flipped" : "");
        if (models.containsKey(cacheKey)) return models.get(cacheKey);

        String fileToLoad = (filename != null && !filename.isEmpty()) ? filename : id;
        String imagePath = DECORATION_SPRITES + tilesetName.toLowerCase() + "/" + fileToLoad + ".png";
        BufferedImage image = ImageUtils.importImage(imagePath, -1, -1);

        if (image != null) {
            if (isFlipped) image = ImageUtils.flipImage(image);
            models.put(cacheKey, image);
        }

        return image;
    }

}
