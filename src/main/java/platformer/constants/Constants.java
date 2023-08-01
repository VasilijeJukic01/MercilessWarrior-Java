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
    public static final int PLAYER_WIDTH = scale(125);
    public static final int PLAYER_HEIGHT = scale(80);
    public static final int PLAYER_X = scale(300);
    public static final int PLAYER_Y = scale(250);

    public static final int XP_BONUS_AMOUNT = 15;
    public static final double STRONG_ARMS_BONUS_COOLDOWN = -0.225;
    public static final int POWER_PILL_BONUS_POWER = 20;
    public static final int LUCKY_DROP_BONUS_COINS = 5;
    public static final int BROKEN_BONES_BONUS_ATTACK = 1;
    public static final int WARRIOR_HEART_BONUS_HEALTH = 5;
    public static final int DRAGON_FRUIT_BONUS_POWER = 26;
    public static final int ELEMENTARY_MAGIC_CRITICAL_HIT_CHANCE = 5;
    public static final int GODS_BLOOD_BONUS_HEALTH = 32;
    public static final double FURIOUS_DASH_COOLDOWN = -0.75;

    // Level Constants
    public static final int MAX_LEVELS = 3;
    public static final int EMPTY_TILE = -1;
    public static final int MAX_TILE_VALUE = 49;
    public static final int LEFT_EXIT = 35;
    public static final int RIGHT_EXIT = 36;
    public static final int PARTICLES_CAP = 50;

    public static final int LEFT_BORDER = (int)(0.2 * GAME_WIDTH);
    public static final int RIGHT_BORDER = (int)(0.8 * GAME_WIDTH);
    public static final int TOP_BORDER = (int)(0.4 * GAME_HEIGHT);
    public static final int BOTTOM_BORDER = (int)(0.6 * GAME_HEIGHT);

    // Enemy Constants
    public static final int SKELETON_WIDTH = scale(100);
    public static final int SKELETON_HEIGHT = scale(90);
    public static final int SKELETON_X_OFFSET = scale(40);
    public static final int SKELETON_Y_OFFSET = scale(24);

    public static final int GHOUL_WIDTH = scale(120);
    public static final int GHOUL_HEIGHT = scale(80);
    public static final int GHOUL_X_OFFSET = scale(50);
    public static final int GHOUL_Y_OFFSET = scale(18);

    public static final int SW_WIDTH = scale(153);
    public static final int SW_HEIGHT = scale(138);
    public static final int SW_X_OFFSET = scale(54);
    public static final int SW_Y_OFFSET = scale(63);

    // Object Constants
    public static final int HEAL_POTION_VAL = 15;
    public static final int STAMINA_POTION_VAL = 10;

    public static final int CONTAINER_WID = scale(40);
    public static final int CONTAINER_HEI = scale(30);

    public static final int POTION_WID = scale(12);
    public static final int POTION_HEI = scale(16);

    public static final int SPIKE_WID = scale(32);
    public static final int SPIKE_HEI = scale(35);

    public static final int ARROW_TRAP_WID = scale(96);
    public static final int ARROW_TRAP_HEI = scale(32);

    public static final int COIN_WID = scale(15);
    public static final int COIN_HEI = scale(15);

    public static final int SHOP_WID = scale(154);
    public static final int SHOP_HEI = scale(132);

    public static final int BLOCKER_WID = scale(96);
    public static final int BLOCKER_HEI = scale(128);

    public static final int BLACKSMITH_WID = scale(110);
    public static final int BLACKSMITH_HEI = scale(85);

    public static final int DOG_WID = scale(64);
    public static final int DOG_HEI = scale(64);

    // Projectile Constants
    public static final int ARROW_WID = scale(32);
    public static final int ARROW_HEI = scale(4);
    public static final int ARROW_SPEED = scale(0.75);

    public static final int LB_WID = scale(60);
    public static final int LB_HEI = scale(60);
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
    public static final int BTN_WID = scale(120);
    public static final int BTN_HEI = scale(42);
    public static final int SMALL_BTN_WID = scale(94);
    public static final int SMALL_BTN_HEI = scale(34);
    public static final int SOUND_BTN_SIZE = scale(30);
    public static final int CRE_BTN_SIZE = scale(30);

    public static final int SLIDER_BTN_SIZE = scale(15);
    public static final int SLIDER_WID = scale(215);
    public static final int SLIDER_HEI = scale(30);

    // UI
    public static final int FONT_MEDIUM = scale(10);
    public static final int FONT_SMALL = scale(7);
    public static final int SHOP_SLOT_MAX_ROW = 7, SHOP_SLOT_MAX_COL = 3;
    public static final int PERK_SLOT_MAX_ROW = 4, PERK_SLOT_MAX_COL = 7;
    public static final int SLOT_SPACING = scale(40);

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
