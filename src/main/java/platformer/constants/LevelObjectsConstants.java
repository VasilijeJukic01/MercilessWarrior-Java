package platformer.constants;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_DEFAULT_SIZE;

public final class LevelObjectsConstants {

    public static final int TILE_SIZE_1HALF = scale(TILES_DEFAULT_SIZE*1.5);
    public static final int TILE_SIZE_2 = scale(TILES_DEFAULT_SIZE*2);
    public static final int TILE_SIZE_3 = scale(TILES_DEFAULT_SIZE*3);
    public static final int TILE_SIZE_4 = scale(TILES_DEFAULT_SIZE*4);
    public static final int TILE_SIZE_5 = scale(TILES_DEFAULT_SIZE*5);
    public static final int TILE_SIZE_6 = scale(TILES_DEFAULT_SIZE*6);
    public static final int TILE_SIZE_8 = scale(TILES_DEFAULT_SIZE*8);
    public static final int TILE_SIZE_9 = scale(TILES_DEFAULT_SIZE*9);

    public static final int BIG_TILE_SIZE = scale(40);

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
