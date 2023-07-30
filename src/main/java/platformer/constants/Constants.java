package platformer.constants;

public class Constants {

    // Resolution Settings
    public static final int TILES_DEFAULT_SIZE = 32;
    public static final int TILES_WIDTH = 26;
    public static final int TILES_HEIGHT = 14;

    public static float SCALE = 2f;
    public static int TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
    public static int GAME_WIDTH = TILES_SIZE * TILES_WIDTH;
    public static int GAME_HEIGHT = TILES_SIZE * TILES_HEIGHT;

    // Logger Constants
    public static final String ERROR_PREFIX         = "[ERROR]         ";
    public static final String WARNING_PREFIX       = "[WARNING]       ";
    public static final String NOTIFICATION_PREFIX  = "[NOTIFICATION]  ";
    public static final String INFORMATION_PREFIX   = "[INFORMATION]   ";
    public static final String DEBUG_PREFIX         = "[DEBUG]         ";

    // Player Constants
    public static final int PLAYER_DEFAULT_WIDTH = 125;
    public static final int PLAYER_DEFAULT_HEIGHT = 80;
    public static final int PLAYER_WIDTH = (int)(PLAYER_DEFAULT_WIDTH * SCALE);
    public static final int PLAYER_HEIGHT = (int)(PLAYER_DEFAULT_HEIGHT * SCALE);

    // Level Constants
    public static final int MAX_LEVELS = 3;
    public static final int EMPTY_TILE = -1;
    public static final int MAX_TILE_VALUE = 49;
    public static final int LEFT_EXIT = 35;
    public static final int RIGHT_EXIT = 36;
    public static final int PARTICLES_CAP = 50;

    // Enemy Constants
    public static final int SKELETON_DEFAULT_WIDTH = 100;
    public static final int SKELETON_DEFAULT_HEIGHT = 90;
    public static final int SKELETON_WIDTH = (int)(SKELETON_DEFAULT_WIDTH * SCALE);
    public static final int SKELETON_HEIGHT = (int)(SKELETON_DEFAULT_HEIGHT * SCALE);
    public static final int SKELETON_X_OFFSET = (int)(40*SCALE);
    public static final int SKELETON_Y_OFFSET = (int)(24*SCALE);

    public static final int GHOUL_DEFAULT_WIDTH = 120;
    public static final int GHOUL_DEFAULT_HEIGHT = 80;
    public static final int GHOUL_WIDTH = (int)(GHOUL_DEFAULT_WIDTH * SCALE);
    public static final int GHOUL_HEIGHT = (int)(GHOUL_DEFAULT_HEIGHT * SCALE);
    public static final int GHOUL_X_OFFSET = (int)(50*SCALE);
    public static final int GHOUL_Y_OFFSET = (int)(18*SCALE);

    public static final int SW_DEFAULT_WIDTH = 153;
    public static final int SW_DEFAULT_HEIGHT = 138;
    public static final int SW_WIDTH = (int)(SW_DEFAULT_WIDTH * SCALE);
    public static final int SW_HEIGHT = (int)(SW_DEFAULT_HEIGHT * SCALE);
    public static final int SW_X_OFFSET = (int)(54*SCALE);
    public static final int SW_Y_OFFSET = (int)(63*SCALE);

    // Object Constants
    public static final int HEAL_POTION_VAL = 15;
    public static final int STAMINA_POTION_VAL = 10;

    public static final int CONTAINER_WID_DEF = 40;
    public static final int CONTAINER_HEI_DEF = 30;
    public static final int CONTAINER_WID = (int)(CONTAINER_WID_DEF * SCALE);
    public static final int CONTAINER_HEI = (int)(CONTAINER_HEI_DEF * SCALE);

    public static final int POTION_WID_DEF = 12;
    public static final int POTION_HEI_DEF = 16;
    public static final int POTION_WID = (int)(POTION_WID_DEF * SCALE);
    public static final int POTION_HEI = (int)(POTION_HEI_DEF * SCALE);

    public static final int SPIKE_WID_DEF = 32;
    public static final int SPIKE_HEI_DEF = 35;
    public static final int SPIKE_WID = (int)(SPIKE_WID_DEF * SCALE);
    public static final int SPIKE_HEI = (int)(SPIKE_HEI_DEF * SCALE);

    public static final int ARROW_TRAP_WID_DEF = 96;
    public static final int ARROW_TRAP_HEI_DEF = 32;
    public static final int ARROW_TRAP_WID = (int)(ARROW_TRAP_WID_DEF * SCALE);
    public static final int ARROW_TRAP_HEI = (int)(ARROW_TRAP_HEI_DEF * SCALE);

    public static final int COIN_WID_DEF = 15;
    public static final int COIN_HEI_DEF = 15;
    public static final int COIN_WID = (int)(COIN_WID_DEF * SCALE);
    public static final int COIN_HEI = (int)(COIN_HEI_DEF * SCALE);

    public static final int SHOP_WID_DEF = 154;
    public static final int SHOP_HEI_DEF = 132;
    public static final int SHOP_WID = (int)(SHOP_WID_DEF * SCALE);
    public static final int SHOP_HEI = (int)(SHOP_HEI_DEF * SCALE);

    public static final int BLOCKER_WID_DEF = 96;
    public static final int BLOCKER_HEI_DEF = 128;
    public static final int BLOCKER_WID = (int)(BLOCKER_WID_DEF * SCALE);
    public static final int BLOCKER_HEI = (int)(BLOCKER_HEI_DEF * SCALE);

    public static final int BLACKSMITH_WID_DEF = 110;
    public static final int BLACKSMITH_HEI_DEF = 85;
    public static final int BLACKSMITH_WID = (int)(BLACKSMITH_WID_DEF * SCALE);
    public static final int BLACKSMITH_HEI = (int)(BLACKSMITH_HEI_DEF * SCALE);

    public static final int DOG_WID_DEF = 64;
    public static final int DOG_HEI_DEF = 64;
    public static final int DOG_WID = (int)(DOG_WID_DEF * SCALE);
    public static final int DOG_HEI = (int)(DOG_HEI_DEF * SCALE);

    // Projectile Constants
    public static final int ARROW_DEF_WID = 32;
    public static final int ARROW_DEF_HEI = 4;
    public static final int ARROW_WID = (int)(ARROW_DEF_WID * SCALE);
    public static final int ARROW_HEI = (int)(ARROW_DEF_HEI * SCALE);
    public static final int ARROW_SPEED = (int)(0.75 * SCALE);

    public static final int LB_DEF_WID = 60;
    public static final int LB_DEF_HEI = 60;
    public static final int LB_WID = (int)(LB_DEF_WID * SCALE);
    public static final int LB_HEI = (int)(LB_DEF_HEI * SCALE);
    public static final int LB_SPEED_FAST = (int)(1.0 * SCALE);
    public static final int LB_SPEED_SLOW = (int)(0.5 * SCALE);

    // Spell Constants
    public static final int LIGHTNING_DEFAULT_WIDTH = (int)(0.75 * TILES_SIZE);
    public static final int LIGHTNING_DEFAULT_HEIGHT = 4 * TILES_SIZE;
    public static final int LIGHTNING_WIDTH = (int)(LIGHTNING_DEFAULT_WIDTH * SCALE);
    public static final int LIGHTNING_HEIGHT = (int)(LIGHTNING_DEFAULT_HEIGHT * SCALE);

    public static final int FLASH_DEFAULT_WIDTH = (int)(0.75 * TILES_SIZE);
    public static final int FLASH_DEFAULT_HEIGHT = 3 * TILES_SIZE;
    public static final int FLASH_WIDTH = (int)(FLASH_DEFAULT_WIDTH * SCALE);
    public static final int FLASH_HEIGHT = (int)(FLASH_DEFAULT_HEIGHT * SCALE);

    // Button Constants
    public static final int BTN_WID_DEFAULT = 120;
    public static final int BTN_HEI_DEFAULT = 42;
    public static final int SMALL_BTN_WID_DEFAULT = 94;
    public static final int SMALL_BTN_HEI_DEFAULT = 34;
    public static final int SOUND_BTN_DEFAULT = 30;
    public static final int CRE_BTN_DEFAULT = 30;

    public static final int BTN_WID = (int)(BTN_WID_DEFAULT * SCALE);
    public static final int BTN_HEI = (int)(BTN_HEI_DEFAULT * SCALE);
    public static final int SMALL_BTN_WID = (int)(SMALL_BTN_WID_DEFAULT * SCALE);
    public static final int SMALL_BTN_HEI = (int)(SMALL_BTN_HEI_DEFAULT * SCALE);
    public static final int SOUND_BTN_SIZE = (int)(SOUND_BTN_DEFAULT * SCALE);
    public static final int CRE_BTN_SIZE = (int)(CRE_BTN_DEFAULT * SCALE);

    public static final int SLIDER_BTN_WID_DEFAULT = 15;
    public static final int SLIDER_BTN_HEI_DEFAULT = 15;
    public static final int SLIDER_WID_DEFAULT = 215;
    public static final int SLIDER_HEI_DEFAULT = 30;

    public static final int SLIDER_BTN_WID = (int)(SLIDER_BTN_WID_DEFAULT * SCALE);
    public static final int SLIDER_BTN_HEI = (int)(SLIDER_BTN_HEI_DEFAULT * SCALE);
    public static final int SLIDER_WID = (int)(SLIDER_WID_DEFAULT * SCALE);
    public static final int SLIDER_HEI = (int)(SLIDER_HEI_DEFAULT * SCALE);

    public static void setResolution(float scale) {
        SCALE = scale;
        TILES_SIZE = (int)(TILES_DEFAULT_SIZE * SCALE);
        GAME_WIDTH = TILES_SIZE * TILES_WIDTH;
        GAME_HEIGHT = TILES_SIZE * TILES_HEIGHT;
    }

}
