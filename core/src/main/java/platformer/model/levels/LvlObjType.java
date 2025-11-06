package platformer.model.levels;

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.constants.LevelObjectsConstants.*;

/**
 * Enum class that represents the different types of level objects that can be placed in the level.
 */
public enum LvlObjType {
    BIG_STONE1("BIG_STONE1",        TILE_SIZE_4, TILE_SIZE_2, 8, 0),
    BIG_STONE2("BIG_STONE2",        TILE_SIZE_4, TILE_SIZE_2, 8, 0),
    BIG_STONE3("BIG_STONE3",        TILE_SIZE_4, TILE_SIZE_2, 8, 0),
    THORNS1("THORNS1",              TILE_SIZE_6, TILE_SIZE_2, 8, 0),
    THORNS2("THORNS2",              TILE_SIZE_4, TILE_SIZE_2, 8, 0),
    THORNS3("THORNS3",              TILE_SIZE_4, TILE_SIZE_3, 8, 0),
    PLANT1("PLANT1",                TILES_SIZE, TILES_SIZE, 7, 0),
    PLANT2("PLANT2",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT3("PLANT3",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT4("PLANT4",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT5("PLANT5",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT6("PLANT6",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT7("PLANT7",                TILES_SIZE, TILES_SIZE, 7, 0),
    PLANT8("PLANT8",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT9("PLANT9",                TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT10("PLANT10",              TILES_SIZE, TILES_SIZE, 10, 0),
    PLANT11("PLANT11",              TILES_SIZE, TILES_SIZE, 10, 0),
    VINES1("VINES1",                TILES_SIZE, TILE_SIZE_3, -10, 0),
    VINES1_BIG("VINES1_BIG",        TILE_SIZE_4, TILE_SIZE_8, -10, 0),
    VINES2("VINES2",                TILE_SIZE_1HALF, TILE_SIZE_3, -10, 0),
    VINES3("VINES3",                TILES_SIZE, TILE_SIZE_3, -10, 0),
    VINES4("VINES4",                TILES_SIZE, TILE_SIZE_4, -10, 0),
    MOSS1("MOSS1",                  TILES_SIZE, TILE_SIZE_3, -10, 0),
    MOSS2("MOSS2",                  TILES_SIZE, TILE_SIZE_3, -10, 0),
    LEAF1("LEAF1",                  BIG_TILE_SIZE, TILES_SIZE, -10, 0),
    BUSH1("BUSH1",                  TILE_SIZE_3, TILE_SIZE_2, 10, 0),
    THORNS1_BIG("THORNS1_BIG",      TILE_SIZE_8, TILE_SIZE_3, -20, 0),
    VINES2_BIG("VINES2_BIG",        TILE_SIZE_3, TILE_SIZE_5, -50, 0),
    MOSS1_BIG("MOSS1_BIG",          TILE_SIZE_3, TILE_SIZE_8, -10, 0),
    VINES5("VINES5",                TILE_SIZE_2, TILE_SIZE_4, 10, 0),
    STONE_MOSS1("STONE_MOSS1",      TILE_SIZE_2, TILES_SIZE, 10, 0),
    MOSS3("MOSS3",                  TILE_SIZE_6, TILE_SIZE_2, 10, 0),
    MOSS4("MOSS4",                  TILE_SIZE_9, TILE_SIZE_2, 10, 0),
    STONE_MOSS2("STONE_MOSS2",      TILE_SIZE_2, TILES_SIZE, 15, 0),
    BLACK("BLACK",                  TILES_SIZE, TILES_SIZE, 0, 0),
    LEFT_END("LEFT_END",            TILES_SIZE, TILES_SIZE, 0, 0),
    RIGHT_END("RIGHT_END",          TILES_SIZE, TILES_SIZE, 0, 0),
    LEAF1_REVERSE("LEAF1_REVERSE",  BIG_TILE_SIZE, TILES_SIZE, 0, -5),
    UPPER_END("UPPER_END",          TILES_SIZE, TILES_SIZE, 0, 5),
    BOTTOM_END("BOTTOM_END",        TILES_SIZE, TILES_SIZE, 0, 0),
    BIG_STONE4("BIG_STONE4",        TILE_SIZE_4, TILE_SIZE_2, 12, 0),
    BIG_STONE5("BIG_STONE5",        TILE_SIZE_4, TILE_SIZE_2, 12, 0),
    BUSH2("BUSH1",                  TILE_SIZE_3, TILE_SIZE_2, 8, 0),
    BUSH3("BUSH2",                  TILE_SIZE_3, TILE_SIZE_2, 8, 0),
    BUSH4("BUSH3",                  TILE_SIZE_2, TILE_SIZE_2, 8, 0),
    FLOWERS1("FLOWERS1",                  TILE_SIZE_3, TILE_SIZE_3, -25, 0),
    FLOWERS2("FLOWERS2",                  TILE_SIZE_3, TILE_SIZE_3, -25, 0),
    LEAF2("LEAF2",                  BIG_TILE_SIZE, TILES_SIZE, 8, 0),
    LOG1("LOG1",        TILE_SIZE_2, TILE_SIZE_2, -16, -0),
    LOG2("LOG2",        TILE_SIZE_2, TILE_SIZE_2, -16, 0),
    LOG3("LOG3",        TILE_SIZE_2, TILE_SIZE_2, -9, 0),
    MOSS5("MOSS5",                  TILES_SIZE, TILES_SIZE, 10, 0),
    MOSS6("MOSS6",                  TILE_SIZE_2, TILE_SIZE_8, 10, 0),
    PILLAR1("PILLAR1",                  TILE_SIZE_2, TILE_SIZE_4, 14, -19),
    ROCK1("ROCK1",        TILE_SIZE_3, TILE_SIZE_2, 7, 0),
    SHROOM1("SHROOM1",                TILES_SIZE, TILES_SIZE, 15, 2),
    SHROOM2("SHROOM2",                TILES_SIZE, TILES_SIZE, 15, 2),
    SHROOM3("SHROOM3",                TILES_SIZE, TILES_SIZE, 15, 2),
    SHROOM4("SHROOM4",                TILE_SIZE_2, TILE_SIZE_2, 15, 2),
    TREE1("TREE1",                  TILE_SIZE_3, TILE_SIZE_5, 14, -7),
    TREE2("TREE2",                  TILE_SIZE_3, TILE_SIZE_5, 14, -7),
    TREE3("TREE3",                  TILE_SIZE_3, TILE_SIZE_4, 14, -7),
    PLANT12("PLANT12",                TILES_SIZE, TILES_SIZE, 8, 2),
    PLANT13("PLANT13",                TILES_SIZE, TILES_SIZE, 8, 2),
    CRYSTAL1("CRYSTAL1",                TILES_SIZE, TILES_SIZE, 8, 2),
    CRYSTAL2("CRYSTAL2",                TILES_SIZE, TILES_SIZE, 8, 2);

    private final String id;
    private final int wid, hei;
    private final int xOffset, yOffset;

    LvlObjType(String id, int wid, int hei, int xOffset, int yOffset) {
        this.id = id;
        this.wid = wid;
        this.hei = hei;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public String getId() {
        return id;
    }

    public int getWid() {
        return wid;
    }

    public int getHei() {
        return hei;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }
}
