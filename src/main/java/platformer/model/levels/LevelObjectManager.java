package platformer.model.levels;

import platformer.model.Tiles;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class LevelObjectManager {

    private LevelObject[] lvlObjects;
    private BufferedImage[] models;

    // Constants
    private final String[] id = {
            "BIG_STONE1", "BIG_STONE2", "BIG_STONE3", "THORNS1", "THORNS2", "THORNS3", "PLANT1", "PLANT2",
            "PLANT3", "PLANT4", "PLANT5", "PLANT6", "VINES1"
    };

    private final Map<String, Point> size = new HashMap<>() {{
        put("BIG_STONE1", new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));
        put("BIG_STONE2", new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));
        put("BIG_STONE3", new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));
        put("THORNS1", new Point((int)(32*6*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));
        put("THORNS2", new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));
        put("THORNS3", new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));
        put("PLANT1", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("PLANT2", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("PLANT3", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("PLANT4", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("PLANT5", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("PLANT6", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));
        put("VINES1", new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));
    }};

    private final Map<String, Point> objectData = new HashMap<>() {{
        // Data [Offset, Another Layer]
        put("BIG_STONE1", new Point(8, 0));
        put("BIG_STONE2", new Point(8, 0));
        put("BIG_STONE3", new Point(8, 0));
        put("THORNS1",    new Point(8, 1));
        put("THORNS2",    new Point(8, 1));
        put("THORNS3",    new Point(8, 0));
        put("PLANT1",     new Point(10, 0));
        put("PLANT2",     new Point(10, 0));
        put("PLANT3",     new Point(10, 0));
        put("PLANT4",     new Point(10, 0));
        put("PLANT5",     new Point(10, 0));
        put("PLANT6",     new Point(10, 0));
        put("VINES1",     new Point(10, 1));
    }};

    public LevelObjectManager() {
        loadImages();
        init();
    }

    private void loadImages() {
        this.models = new BufferedImage[id.length];
        for (int i = 0; i < models.length; i++) {
            models[i] = Utils.getInstance().importImage("src/main/resources/images/levels/levelObjects/"+id[i]+".png", size.get(id[i]).x, size.get(id[i]).y);
        }
    }

    private void init() {
        this.lvlObjects = new LevelObject[id.length];
        for (int i = 0; i < lvlObjects.length; i++) {
            lvlObjects[i] = new LevelObject(0, i, models[i], models[i].getWidth(), models[i].getHeight());
            lvlObjects[i].setYOffset((int)(objectData.get(id[i]).x*Tiles.SCALE.getValue()));
            lvlObjects[i].setAnotherLayer(objectData.get(id[i]).y == 1);
        }
    }

    public LevelObject[] getLvlObjects() {
        return lvlObjects;
    }
}
