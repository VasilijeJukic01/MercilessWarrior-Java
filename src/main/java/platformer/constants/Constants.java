package platformer.constants;

import static launcher.Config.SCALING_FACTOR;

public class Constants {

    // Resolution Settings
    private static final int TILES_DEFAULT_SIZE = 32;
    public static final int TILES_WIDTH = 26;
    public static final int TILES_HEIGHT = 14;

    public static float SCALE = SCALING_FACTOR;
    public static int TILES_SIZE = scale(TILES_DEFAULT_SIZE);
    public static int GAME_WIDTH = TILES_SIZE * TILES_WIDTH;
    public static int GAME_HEIGHT = TILES_SIZE * TILES_HEIGHT;

    // Logger Constants
    public static final String ERROR_PREFIX         = "[ERROR]         ";
    public static final String WARNING_PREFIX       = "[WARNING]       ";
    public static final String NOTIFICATION_PREFIX  = "[NOTIFICATION]  ";
    public static final String INFORMATION_PREFIX   = "[INFORMATION]   ";
    public static final String DEBUG_PREFIX         = "[DEBUG]         ";

    // Player Constants
    private static final int PLAYER_DEFAULT_WIDTH = 125;
    private static final int PLAYER_DEFAULT_HEIGHT = 80;
    public static final int PLAYER_WIDTH = scale(PLAYER_DEFAULT_WIDTH);
    public static final int PLAYER_HEIGHT = scale(PLAYER_DEFAULT_HEIGHT);

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
    public static final int SKELETON_WIDTH = scale(SKELETON_DEFAULT_WIDTH);
    public static final int SKELETON_HEIGHT = scale(SKELETON_DEFAULT_HEIGHT);
    public static final int SKELETON_X_OFFSET = scale(40);
    public static final int SKELETON_Y_OFFSET = scale(24);

    public static final int GHOUL_DEFAULT_WIDTH = 120;
    public static final int GHOUL_DEFAULT_HEIGHT = 80;
    public static final int GHOUL_WIDTH = scale(GHOUL_DEFAULT_WIDTH);
    public static final int GHOUL_HEIGHT = scale(GHOUL_DEFAULT_HEIGHT);
    public static final int GHOUL_X_OFFSET = scale(50);
    public static final int GHOUL_Y_OFFSET = scale(18);

    public static final int SW_DEFAULT_WIDTH = 153;
    public static final int SW_DEFAULT_HEIGHT = 138;
    public static final int SW_WIDTH = scale(SW_DEFAULT_WIDTH);
    public static final int SW_HEIGHT = scale(SW_DEFAULT_HEIGHT);
    public static final int SW_X_OFFSET = scale(54);
    public static final int SW_Y_OFFSET = scale(63);

    // Object Constants
    public static final int HEAL_POTION_VAL = 15;
    public static final int STAMINA_POTION_VAL = 10;

    public static final int CONTAINER_WID_DEF = 40;
    public static final int CONTAINER_HEI_DEF = 30;
    public static final int CONTAINER_WID = scale(CONTAINER_WID_DEF);
    public static final int CONTAINER_HEI = scale(CONTAINER_HEI_DEF);

    public static final int POTION_WID_DEF = 12;
    public static final int POTION_HEI_DEF = 16;
    public static final int POTION_WID = scale(POTION_WID_DEF);
    public static final int POTION_HEI = scale(POTION_HEI_DEF);

    public static final int SPIKE_WID_DEF = 32;
    public static final int SPIKE_HEI_DEF = 35;
    public static final int SPIKE_WID = scale(SPIKE_WID_DEF);
    public static final int SPIKE_HEI = scale(SPIKE_HEI_DEF);

    public static final int ARROW_TRAP_WID_DEF = 96;
    public static final int ARROW_TRAP_HEI_DEF = 32;
    public static final int ARROW_TRAP_WID = scale(ARROW_TRAP_WID_DEF);
    public static final int ARROW_TRAP_HEI = scale(ARROW_TRAP_HEI_DEF);

    public static final int COIN_WID_DEF = 15;
    public static final int COIN_HEI_DEF = 15;
    public static final int COIN_WID = scale(COIN_WID_DEF);
    public static final int COIN_HEI = scale(COIN_HEI_DEF);

    public static final int SHOP_WID_DEF = 154;
    public static final int SHOP_HEI_DEF = 132;
    public static final int SHOP_WID = scale(SHOP_WID_DEF);
    public static final int SHOP_HEI = scale(SHOP_HEI_DEF);

    public static final int BLOCKER_WID_DEF = 96;
    public static final int BLOCKER_HEI_DEF = 128;
    public static final int BLOCKER_WID = scale(BLOCKER_WID_DEF);
    public static final int BLOCKER_HEI = scale(BLOCKER_HEI_DEF);

    public static final int BLACKSMITH_WID_DEF = 110;
    public static final int BLACKSMITH_HEI_DEF = 85;
    public static final int BLACKSMITH_WID = scale(BLACKSMITH_WID_DEF);
    public static final int BLACKSMITH_HEI = scale(BLACKSMITH_HEI_DEF);

    public static final int DOG_WID_DEF = 64;
    public static final int DOG_HEI_DEF = 64;
    public static final int DOG_WID = scale(DOG_WID_DEF);
    public static final int DOG_HEI = scale(DOG_HEI_DEF);

    // Projectile Constants
    public static final int ARROW_DEF_WID = 32;
    public static final int ARROW_DEF_HEI = 4;
    public static final int ARROW_WID = scale(ARROW_DEF_WID);
    public static final int ARROW_HEI = scale(ARROW_DEF_HEI);
    public static final int ARROW_SPEED = scale(0.75);

    public static final int LB_DEF_WID = 60;
    public static final int LB_DEF_HEI = 60;
    public static final int LB_WID = scale(LB_DEF_WID);
    public static final int LB_HEI = scale(LB_DEF_HEI);
    public static final int LB_SPEED_FAST = scale(1);
    public static final int LB_SPEED_SLOW = scale(0.5);

    // Spell Constants
    public static final int LIGHTNING_DEFAULT_WIDTH = (int)(0.75 * TILES_SIZE);
    public static final int LIGHTNING_DEFAULT_HEIGHT = 4 * TILES_SIZE;
    public static final int LIGHTNING_WIDTH = scale(LIGHTNING_DEFAULT_WIDTH);
    public static final int LIGHTNING_HEIGHT = scale(LIGHTNING_DEFAULT_HEIGHT);

    public static final int FLASH_DEFAULT_WIDTH = (int)(0.75 * TILES_SIZE);
    public static final int FLASH_DEFAULT_HEIGHT = 3 * TILES_SIZE;
    public static final int FLASH_WIDTH = scale(FLASH_DEFAULT_WIDTH);
    public static final int FLASH_HEIGHT = scale(FLASH_DEFAULT_HEIGHT);

    // Button Constants
    public static final int BTN_WID_DEFAULT = 120;
    public static final int BTN_HEI_DEFAULT = 42;
    public static final int SMALL_BTN_WID_DEFAULT = 94;
    public static final int SMALL_BTN_HEI_DEFAULT = 34;
    public static final int SOUND_BTN_DEFAULT = 30;
    public static final int CRE_BTN_DEFAULT = 30;

    public static final int BTN_WID = scale(BTN_WID_DEFAULT);
    public static final int BTN_HEI = scale(BTN_HEI_DEFAULT);
    public static final int SMALL_BTN_WID = scale(SMALL_BTN_WID_DEFAULT);
    public static final int SMALL_BTN_HEI = scale(SMALL_BTN_HEI_DEFAULT);
    public static final int SOUND_BTN_SIZE = scale(SOUND_BTN_DEFAULT);
    public static final int CRE_BTN_SIZE = scale(CRE_BTN_DEFAULT);

    public static final int SLIDER_BTN_WID_DEFAULT = 15;
    public static final int SLIDER_BTN_HEI_DEFAULT = 15;
    public static final int SLIDER_WID_DEFAULT = 215;
    public static final int SLIDER_HEI_DEFAULT = 30;

    public static final int SLIDER_BTN_WID = scale(SLIDER_BTN_WID_DEFAULT);
    public static final int SLIDER_BTN_HEI = scale(SLIDER_BTN_HEI_DEFAULT);
    public static final int SLIDER_WID = scale(SLIDER_WID_DEFAULT);
    public static final int SLIDER_HEI = scale(SLIDER_HEI_DEFAULT);

    // UI
    public static final int FONT_MEDIUM = scale(10);
    public static final int FONT_SMALL = scale(7);

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
