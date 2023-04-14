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
            "PLANT3", "PLANT4", "PLANT5", "PLANT6", "PLANT7", "PLANT8", "PLANT9", "PLANT10",
            "PLANT11", "VINES1", "VINES1_BIG", "VINES2", "VINES3", "VINES4", "MOSS1", "MOSS2",
            "LEAF1", "BUSH1", "THORNS1_BIG", "VINES2_BIG", "MOSS1_BIG", "GROUND1", "GROUND2", "MOSS3",
            "MOSS4"
    };

    private final Map<String, Point> size = new HashMap<>() {{
        put("BIG_STONE1",           new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //0
        put("BIG_STONE2",           new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //1
        put("BIG_STONE3",           new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //2
        put("THORNS1",              new Point((int)(32*6*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //3
        put("THORNS2",              new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //4
        put("THORNS3",              new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));         //5
        put("PLANT1",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //6
        put("PLANT2",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //7
        put("PLANT3",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //8
        put("PLANT4",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //9
        put("PLANT5",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //10
        put("PLANT6",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //11
        put("PLANT7",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //12
        put("PLANT8",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //13
        put("PLANT9",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //14
        put("PLANT10",              new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //15
        put("PLANT11",              new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //16
        put("VINES1",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //17
        put("VINES1_BIG",           new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*8*Tiles.SCALE.getValue())));         //18
        put("VINES2",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //19
        put("VINES3",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //20
        put("VINES4",               new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //21
        put("MOSS1",                new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //22
        put("MOSS2",                new Point((int)(32*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));           //23
        put("LEAF1",                new Point((int)(40*Tiles.SCALE.getValue()), (int)(32*Tiles.SCALE.getValue())));             //24
        put("BUSH1",                new Point((int)(32*4*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));         //25
        put("THORNS1_BIG",          new Point((int)(32*8*Tiles.SCALE.getValue()), (int)(32*3*Tiles.SCALE.getValue())));         //26
        put("VINES2_BIG",           new Point((int)(32*3*Tiles.SCALE.getValue()), (int)(32*5*Tiles.SCALE.getValue())));         //27
        put("MOSS1_BIG",            new Point((int)(32*3*Tiles.SCALE.getValue()), (int)(32*8*Tiles.SCALE.getValue())));         //28
        put("GROUND1",              new Point((int)(32*6*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //29
        put("GROUND2",              new Point((int)(32*6*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //30
        put("MOSS3",                new Point((int)(32*6.1*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));       //31
        put("MOSS4",                new Point((int)(32*9*Tiles.SCALE.getValue()), (int)(32*2*Tiles.SCALE.getValue())));         //32
    }};

    private final Map<String, Point> objectData = new HashMap<>() {{
        // Data                     [YOffset, Layer]
        put("BIG_STONE1",           new Point(8, 1));
        put("BIG_STONE2",           new Point(8, 1));
        put("BIG_STONE3",           new Point(8, 1));
        put("THORNS1",              new Point(8, 0));
        put("THORNS2",              new Point(8, 0));
        put("THORNS3",              new Point(8, 0));
        put("PLANT1",               new Point(10, 0));
        put("PLANT2",               new Point(10, 0));
        put("PLANT3",               new Point(10, 0));
        put("PLANT4",               new Point(10, 0));
        put("PLANT5",               new Point(10, 0));
        put("PLANT6",               new Point(10, 0));
        put("PLANT7",               new Point(10, 0));
        put("PLANT8",               new Point(10, 0));
        put("PLANT9",               new Point(10, 0));
        put("PLANT10",              new Point(10, 0));
        put("PLANT11",              new Point(10, 0));
        put("VINES1",               new Point(-10, 0));
        put("VINES1_BIG",           new Point(-10, 0));
        put("VINES2",               new Point(-10, 0));
        put("VINES3",               new Point(-10, 0));
        put("VINES4",               new Point(-10, 0));
        put("MOSS1",                new Point(-10, 0));
        put("MOSS2",                new Point(-10, 0));
        put("LEAF1",                new Point(-10, 0));
        put("BUSH1",                new Point(10, 0));
        put("THORNS1_BIG",          new Point(-20, 0));
        put("VINES2_BIG",           new Point(-50, 0));
        put("MOSS1_BIG",            new Point(-10, 0));
        put("GROUND1",              new Point(-10, 2));
        put("GROUND2",              new Point(-10, 2));
        put("MOSS3",                new Point(10, 2));
        put("MOSS4",                new Point(10, 2));
    }};

    public LevelObjectManager() {
        loadImages();
        init();
    }

    private void loadImages() {
        this.models = new BufferedImage[id.length];
        for (int i = 0; i < models.length; i++) {
            int bigFlag = 0;
            if (id[i].contains("_BIG")) bigFlag = 4;
            models[i] = Utils.getInstance().importImage("src/main/resources/images/levels/levelObjects/"+id[i].substring(0, id[i].length() - bigFlag)+".png", size.get(id[i]).x, size.get(id[i]).y);
        }
    }

    private void init() {
        this.lvlObjects = new LevelObject[id.length];
        for (int i = 0; i < lvlObjects.length; i++) {
            lvlObjects[i] = new LevelObject(0, i, models[i], models[i].getWidth(), models[i].getHeight());
            lvlObjects[i].setYOffset((int)(objectData.get(id[i]).x*Tiles.SCALE.getValue()));
            lvlObjects[i].setLayer(objectData.get(id[i]).y);
        }
    }

    public LevelObject[] getLvlObjects() {
        return lvlObjects;
    }
}
