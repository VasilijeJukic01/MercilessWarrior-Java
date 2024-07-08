package platformer.model.levels;

import platformer.utils.Utils;

import java.awt.image.BufferedImage;

/**
 * This class is responsible for managing the level objects in the game.
 * It holds references to all the level objects and provides methods for loading their images.
 */
public class LevelObjectManager {

    private BufferedImage[] models;

    public LevelObjectManager() {
        loadImages();
    }

    private void loadImages() {
        this.models = new BufferedImage[LvlObjType.values().length];
        for (int i = 0; i < models.length; i++) {
            LvlObjType objType = LvlObjType.values()[i];
            String id = objType.getId();
            int bigFlag = id.contains("_BIG") ? 4 : 0;
            int reverseFlag = id.contains("_REVERSE") ? 8 : 0;
            String name = id.substring(0, id.length() - bigFlag - reverseFlag);
            models[i] = Utils.getInstance().importImage("/images/levels/levelObjects/"+name+".png", objType.getWid(), objType.getHei());
            if (reverseFlag != 0) models[i] = Utils.getInstance().flipImage(models[i]);
        }
    }

    public BufferedImage[] getModels() {
        return models;
    }

}
