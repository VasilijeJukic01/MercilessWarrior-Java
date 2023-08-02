package platformer.model.levels;

import platformer.utils.Utils;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;

import static platformer.constants.Constants.SCALE;

public class LevelObjectManager {

    private LevelObject[] lvlObjects;
    private BufferedImage[] models;

    private final List<LvlObjType> objData;

    public LevelObjectManager() {
        this.objData = Arrays.asList(LvlObjType.values());
        loadImages();
        init();
    }

    private void loadImages() {
        this.models = new BufferedImage[objData.size()];
        for (int i = 0; i < models.length; i++) {
            LvlObjType objType = objData.get(i);
            String id = objType.getId();
            int bigFlag = id.contains("_BIG") ? 4 : 0;
            int reverseFlag = id.contains("_REVERSE") ? 8 : 0;
            String name = id.substring(0, id.length() - bigFlag - reverseFlag);
            models[i] = Utils.getInstance().importImage("/images/levels/levelObjects/"+name+".png", objType.getWid(), objType.getHei());
            if (reverseFlag != 0) models[i] = Utils.getInstance().flipImage(models[i]);
        }
    }

    private void init() {
        this.lvlObjects = new LevelObject[objData.size()];
        for (int i = 0; i < lvlObjects.length; i++) {
            LvlObjType lvlObj = objData.get(i);
            lvlObjects[i] = new LevelObject(models[i], models[i].getWidth(), models[i].getHeight());
            lvlObjects[i].setYOffset((int)(lvlObj.getXOffset() * SCALE));
            lvlObjects[i].setXOffset((int)(lvlObj.getYOffset() * SCALE));
        }
    }

    public LevelObject[] getLvlObjects() {
        return lvlObjects;
    }
}
