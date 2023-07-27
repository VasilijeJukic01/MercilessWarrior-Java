package platformer.model.levels;

import platformer.utils.Utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import static platformer.constants.Constants.SCALE;

public class LevelObjectManager {

    private LevelObject[] lvlObjects;
    private BufferedImage[] models;

    // Constants
    private final String[] id = {
            "BIG_STONE1", "BIG_STONE2", "BIG_STONE3", "THORNS1", "THORNS2", "THORNS3", "PLANT1", "PLANT2",
            "PLANT3", "PLANT4", "PLANT5", "PLANT6", "PLANT7", "PLANT8", "PLANT9", "PLANT10",
            "PLANT11", "VINES1", "VINES1_BIG", "VINES2", "VINES3", "VINES4", "MOSS1", "MOSS2",
            "LEAF1", "BUSH1", "THORNS1_BIG", "VINES2_BIG", "MOSS1_BIG", "VINES5", "STONE_MOSS1", "MOSS3",
            "MOSS4", "STONE_MOSS2", "BLACK", "LEFT_END", "RIGHT_END", "LEAF1_REVERSE"
    };

    private final Map<String, Point> size = new HashMap<>() {{
        put("BIG_STONE1",           new Point((int)(32*4*SCALE), (int)(32*2*SCALE)));         //0
        put("BIG_STONE2",           new Point((int)(32*4*SCALE), (int)(32*2*SCALE)));         //1
        put("BIG_STONE3",           new Point((int)(32*4*SCALE), (int)(32*2*SCALE)));         //2
        put("THORNS1",              new Point((int)(32*6*SCALE), (int)(32*2*SCALE)));         //3
        put("THORNS2",              new Point((int)(32*4*SCALE), (int)(32*2*SCALE)));         //4
        put("THORNS3",              new Point((int)(32*4*SCALE), (int)(32*3*SCALE)));         //5
        put("PLANT1",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //6
        put("PLANT2",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //7
        put("PLANT3",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //8
        put("PLANT4",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //9
        put("PLANT5",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //10
        put("PLANT6",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //11
        put("PLANT7",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //12
        put("PLANT8",               new Point((int)(32*SCALE), (int)(32*1.5*SCALE)));         //13
        put("PLANT9",               new Point((int)(32*SCALE), (int)(32*SCALE)));             //14
        put("PLANT10",              new Point((int)(32*SCALE), (int)(32*SCALE)));             //15
        put("PLANT11",              new Point((int)(32*SCALE), (int)(32*SCALE)));             //16
        put("VINES1",               new Point((int)(32*SCALE), (int)(32*3*SCALE)));           //17
        put("VINES1_BIG",           new Point((int)(32*4*SCALE), (int)(32*8.2*SCALE)));       //18
        put("VINES2",               new Point((int)(32*1.5*SCALE), (int)(32*3*SCALE)));       //19
        put("VINES3",               new Point((int)(32*SCALE), (int)(32*3*SCALE)));           //20
        put("VINES4",               new Point((int)(32*SCALE), (int)(32*4*SCALE)));           //21
        put("MOSS1",                new Point((int)(32*SCALE), (int)(32*3*SCALE)));           //22
        put("MOSS2",                new Point((int)(32*SCALE), (int)(32*3*SCALE)));           //23
        put("LEAF1",                new Point((int)(40*SCALE), (int)(32*SCALE)));             //24
        put("BUSH1",                new Point((int)(32*4*SCALE), (int)(32*3*SCALE)));         //25
        put("THORNS1_BIG",          new Point((int)(32*8*SCALE), (int)(32*3*SCALE)));         //26
        put("VINES2_BIG",           new Point((int)(32*3*SCALE), (int)(32*5*SCALE)));         //27
        put("MOSS1_BIG",            new Point((int)(32*3*SCALE), (int)(32*8*SCALE)));         //28
        put("VINES5",               new Point((int)(32*2*SCALE), (int)(32*4.2*SCALE)));       //29
        put("STONE_MOSS1",          new Point((int)(32*2*SCALE), (int)(32*SCALE)));           //30
        put("MOSS3",                new Point((int)(32*6.1*SCALE), (int)(32*2*SCALE)));       //31
        put("MOSS4",                new Point((int)(32*9*SCALE), (int)(32*2*SCALE)));         //32
        put("STONE_MOSS2",          new Point((int)(32*2*SCALE), (int)(32*SCALE)));           //33
        put("BLACK",                new Point((int)(33*SCALE), (int)(33*SCALE)));             //34
        put("LEFT_END",             new Point((int)(32*SCALE), (int)(32*SCALE)));             //35
        put("RIGHT_END",            new Point((int)(32*SCALE), (int)(32*SCALE)));             //36
        put("LEAF1_REVERSE",        new Point((int)(40*SCALE), (int)(32*SCALE)));             //37
    }};

    private final Map<String, Point> objectData = new HashMap<>() {{
        // Data                     [YOffset, XOffset]
        put("BIG_STONE1",           new Point(8, 0));
        put("BIG_STONE2",           new Point(8, 0));
        put("BIG_STONE3",           new Point(8, 0));
        put("THORNS1",              new Point(8, 0));
        put("THORNS2",              new Point(8, 0));
        put("THORNS3",              new Point(8, 0));
        put("PLANT1",               new Point(7, 0));
        put("PLANT2",               new Point(10, 0));
        put("PLANT3",               new Point(10, 0));
        put("PLANT4",               new Point(10, 0));
        put("PLANT5",               new Point(10, 0));
        put("PLANT6",               new Point(10, 0));
        put("PLANT7",               new Point(7, 0));
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
        put("VINES5",               new Point(10, 0));
        put("STONE_MOSS1",          new Point(10, 0));
        put("MOSS3",                new Point(10, 0));
        put("MOSS4",                new Point(10, 0));
        put("STONE_MOSS2",          new Point(15, 0));
        put("BLACK",                new Point(0, 0));
        put("LEFT_END",             new Point(0, 0));
        put("RIGHT_END",            new Point(0, 0));
        put("LEAF1_REVERSE",        new Point(0, -5));
    }};

    public LevelObjectManager() {
        loadImages();
        init();
    }

    private void loadImages() {
        this.models = new BufferedImage[id.length];
        for (int i = 0; i < models.length; i++) {
            int bigFlag = 0, reverseFlag = 0;
            if (id[i].contains("_BIG")) bigFlag = 4;
            if (id[i].contains("_REVERSE")) reverseFlag = 8;
            String name = id[i].substring(0, id[i].length() - bigFlag - reverseFlag);
            models[i] = Utils.getInstance().importImage("src/main/resources/images/levels/levelObjects/"+name+".png", size.get(id[i]).x, size.get(id[i]).y);
            if (reverseFlag != 0) models[i] = Utils.getInstance().flipImage(models[i]);
        }
    }

    private void init() {
        this.lvlObjects = new LevelObject[id.length];
        for (int i = 0; i < lvlObjects.length; i++) {
            lvlObjects[i] = new LevelObject(models[i], models[i].getWidth(), models[i].getHeight());
            lvlObjects[i].setYOffset((int)(objectData.get(id[i]).x*SCALE));
            lvlObjects[i].setXOffset((int)(objectData.get(id[i]).y*SCALE));
        }
    }

    public LevelObject[] getLvlObjects() {
        return lvlObjects;
    }
}
