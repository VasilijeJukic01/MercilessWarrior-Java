package platformer.constants;

import static platformer.launcher.Config.SCALING_FACTOR;

// Vocabulary

/*

HB - Hit Box
AB - Attack Box
HP - Health
ST - Stamina
CD - Cooldown
SW - Spear Woman
LB - Lightning Ball
FB - Fireball

 */

/**
 * Constants class contains all the constants used in the game.
 */
public final class Constants {

    // Resolution Settings
    public static final int TILES_DEFAULT_SIZE = 32;
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
    public static final int PLAYER_AB_WID = scale(20);
    public static final int PLAYER_AB_HEI = scale(35);
    public static final int PLAYER_HB_WID = scale(15);
    public static final int PLAYER_HB_HEI = scale(44);
    public static final int PLAYER_HB_OFFSET_X = scale(42);
    public static final int PLAYER_HB_OFFSET_Y = scale(16);

    public static final int XP_CAP = 10000;
    public static final int PLAYER_MAX_HP = 100;
    public static final int PLAYER_MAX_ST = 100;
    public static final double PLAYER_SPEED = dScale(0.6);
    public static final double LAVA_PLAYER_SPEED = dScale(0.3);
    public static final double PLAYER_BOOST = dScale(0.65);

    public static final double PUSH_LIMIT = -30;
    public static final double PUSH_SPEED = 0.95;
    public static final double DASH_SPEED = 6;
    public static final double FLAME_COST = -0.20;
    public static final double TRANSFORM_COST = -0.025;

    public static final double LAVA_DMG = 0.5;

    // Perks Constants
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

    // Cooldown Constants
    public static final double COOLDOWN_TICK = 0.1;
    public static final double PLAYER_ATTACK_CD = 0.75;
    public static final double PLAYER_BLOCK_CD = 1.2;
    public static final double PLAYER_DASH_CD = 1.75;
    public static final double PLAYER_SPELL_CD = 2.5;

    public static final double GHOUL_ATT_CD = 10;
    public static final double GHOUL_DASH_CD = 6;

    // Shop Constants
    public static final int HEALTH_COST = 10;
    public static final int STAMINA_COST = 12;
    public static final int HEALTH_VAL = 40;
    public static final int STAMINA_VAL = 40;

    public static final int IRON_COST = 15;
    public static final int SILVER_COST = 20;
    public static final int COPPER_COST = 10;
    public static final int AMETHYST_COST = 30;
    public static final int SONIC_QUARTZ_COST = 45;
    public static final int AZURELITE_COST = 100;
    public static final int ELECTRICITE_COST = 250;
    public static final int MAGMA_COST = 50;
    public static final int ROSALLIUM_COST = 270;

    public static final int HELMET_WARRIOR_COST = 80;
    public static final int ARMOR_WARRIOR_COST = 100;
    public static final int BRACELETS_WARRIOR_COST = 70;
    public static final int TROUSERS_WARRIOR_COST = 80;
    public static final int BOOTS_WARRIOR_COST = 70;

    // Level Constants
    public static final int MAX_LEVELS = 4;
    public static final int EMPTY_TILE = -1;
    public static final int MAX_TILE_VALUE = 49;
    public static final int LEFT_EXIT = 35;
    public static final int RIGHT_EXIT = 36;
    public static final int UPPER_EXIT = 38;
    public static final int BOTTOM_EXIT = 39;
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
    public static final int SKELETON_HB_WID = scale(21);
    public static final int SKELETON_HB_HEI = scale(42);
    public static final int SKELETON_AB_WID = scale(60);
    public static final int SKELETON_AB_HEI = scale(45);

    public static final int GHOUL_WIDTH = scale(120);
    public static final int GHOUL_HEIGHT = scale(80);
    public static final int GHOUL_X_OFFSET = scale(50);
    public static final int GHOUL_Y_OFFSET = scale(18);
    public static final int GHOUL_HB_WID = scale(21);
    public static final int GHOUL_HB_HEI = scale(42);
    public static final int GHOUL_AB_WID = scale(60);
    public static final int GHOUL_AB_HEI = scale(45);

    public static final int KNIGHT_WIDTH = scale(120);
    public static final int KNIGHT_HEIGHT = scale(80);
    public static final int KNIGHT_X_OFFSET = scale(50);
    public static final int KNIGHT_Y_OFFSET = scale(18);
    public static final int KNIGHT_HB_WID = scale(21);
    public static final int KNIGHT_HB_HEI = scale(42);
    public static final int KNIGHT_AB_WID = scale(60);
    public static final int KNIGHT_AB_HEI = scale(45);

    public static final int WRAITH_WIDTH = scale(200);
    public static final int WRAITH_HEIGHT = scale(130);
    public static final int WRAITH_X_OFFSET = scale(90);
    public static final int WRAITH_Y_OFFSET = scale(45);
    public static final int WRAITH_HB_WID = scale(21);
    public static final int WRAITH_HB_HEI = scale(42);
    public static final int WRAITH_AB_WID = scale(100);
    public static final int WRAITH_AB_HEI = scale(45);

    public static final int SW_WIDTH = scale(153);
    public static final int SW_HEIGHT = scale(138);
    public static final int SW_X_OFFSET = scale(54);
    public static final int SW_Y_OFFSET = scale(63);
    public static final int SW_HB_WID = scale(25);
    public static final int SW_HB_HEI = scale(50);
    public static final int SW_AB_WID = scale(96);
    public static final int SW_AB_WID_REDUCE = scale(48);
    public static final int SW_AB_HEI = scale(54);

    public static final double SKELETON_SPEED_FAST = dScale(0.35);
    public static final double GHOUL_SPEED_FAST = dScale(0.45);
    public static final double KNIGHT_SPEED_FAST = dScale(0.4);
    public static final double WRAITH_SPEED_FAST = dScale(0.5);

    private static final double RANGE = 1.25 * TILES_SIZE;
    public static final double SIGHT_RANGE = RANGE * 5;
    public static final double SKELETON_ATT_RANGE = RANGE / 1.25;
    public static final double GHOUL_ATT_RANGE = RANGE * 2;
    public static final double KNIGHT_ATT_RANGE = RANGE / 1.3;
    public static final double WRAITH_ATT_RANGE = RANGE;
    public static final double SW_ATT_RANGE = RANGE * 1.8;
    public static final double ENEMY_SPEED_SLOW = dScale(0.2);

    // Object Constants
    public static final int HEAL_POTION_VAL = 15;
    public static final int STAMINA_POTION_VAL = 10;

    public static final int CONTAINER_WID = scale(40);
    public static final int CONTAINER_HEI = scale(30);
    public static final int BOX_HB_WID = scale(25);
    public static final int BOX_HB_HEI = scale(18);
    public static final int BOX_OFFSET_X = scale(7);
    public static final int BOX_OFFSET_Y = scale(12);
    public static final int BARREL_HB_WID = scale(23);
    public static final int BARREL_HB_HEI = scale(25);
    public static final int BARREL_OFFSET_X = scale(8);
    public static final int BARREL_OFFSET_Y = scale(5);

    public static final int POTION_WID = scale(12);
    public static final int POTION_HEI = scale(16);
    public static final int POTION_HB_WID = scale(7);
    public static final int POTION_HB_HEI = scale(14);
    public static final int POTION_OFFSET_X = scale(3);
    public static final int POTION_OFFSET_Y = scale(2);

    public static final int SPIKE_WID = scale(32);
    public static final int SPIKE_HEI = scale(35);
    public static final int SPIKE_HB_WID = scale(32);
    public static final int SPIKE_HB_HEI = scale(15);
    public static final int SPIKE_OFFSET_X = scale(0);
    public static final int SPIKE_OFFSET_Y = scale(22);

    public static final int ARROW_TRAP_WID = scale(96);
    public static final int ARROW_TRAP_HEI = scale(32);
    public static final int ARROW_TRAP_HB_SIZE = scale(32);
    public static final int ARROW_TRAP_OFFSET = scale(6);

    public static final int COIN_WID = scale(15);
    public static final int COIN_HEI = scale(15);
    public static final int COIN_HB_SIZE = scale(10);
    public static final int COIN_OFFSET = scale(3);

    public static final int SHOP_WID = scale(154);
    public static final int SHOP_HEI = scale(132);
    public static final int SHOP_HB_WID = scale(154);
    public static final int SHOP_HB_HEI = scale(132);
    public static final int SHOP_OFFSET_X = scale(1);
    public static final int SHOP_OFFSET_Y = scale(1);

    public static final int BLOCKER_WID = scale(96);
    public static final int BLOCKER_HEI = scale(128);
    public static final int BLOCKER_HB_WID = scale(32);
    public static final int BLOCKER_HB_HEI = scale(32 * 3.5);
    public static final int BLOCKER_OFFSET_X = scale(32);
    public static final int BLOCKER_OFFSET_Y = scale(22);

    public static final int BLACKSMITH_WID = scale(110);
    public static final int BLACKSMITH_HEI = scale(85);
    public static final int BLACKSMITH_HB_WID = scale(43);
    public static final int BLACKSMITH_HB_HEI = scale(45);
    public static final int BLACKSMITH_OFFSET_X = scale(32);
    public static final int BLACKSMITH_OFFSET_Y = scale(20);

    public static final int DOG_WID = scale(64);
    public static final int DOG_HEI = scale(64);
    public static final int DOG_HB_WID = scale(32);
    public static final int DOG_HB_HEI = scale(32);
    public static final int DOG_OFFSET_X = scale(18);
    public static final int DOG_OFFSET_Y = scale(12);

    public static final int SAVE_TOTEM_WID = scale(50);
    public static final int SAVE_TOTEM_HEI = scale(49);
    public static final int SAVE_TOTEM_HB_WID = scale(32);
    public static final int SAVE_TOTEM_HB_HEI = scale(32);
    public static final int SAVE_TOTEM_OFFSET_X = scale(10);
    public static final int SAVE_TOTEM_OFFSET_Y = scale(9);

    public static final int SMASH_TRAP_WID = scale(50);
    public static final int SMASH_TRAP_HEI = scale(76);
    public static final int SMASH_TRAP_HB_WID = scale(20);
    public static final int SMASH_TRAP_HB_HEI = scale(80);
    public static final int SMASH_TRAP_OFFSET_X = scale(15);
    public static final int SMASH_TRAP_OFFSET_Y = scale(0);

    public static final int CANDLE_WID = scale(25);
    public static final int CANDLE_HEI = scale(69);
    public static final int CANDLE_HB_WID = scale(27);
    public static final int CANDLE_HB_HEI = scale(27);
    public static final int CANDLE_OFFSET_X = scale(-8);
    public static final int CANDLE_OFFSET_Y = scale(12);

    public static final int LOOT_WID = scale(32);
    public static final int LOOT_HEI = scale(24);
    public static final int LOOT_HB_WID = scale(27);
    public static final int LOOT_HB_HEI = scale(27);
    public static final int LOOT_OFFSET_X = scale(1);
    public static final int LOOT_OFFSET_Y = scale(-5);

    public static final int TABLE_WID = scale(48);
    public static final int TABLE_HEI = scale(32);
    public static final int TABLE_HB_WID = scale(40);
    public static final int TABLE_HB_HEI = scale(32);
    public static final int TABLE_OFFSET_X = scale(5);
    public static final int TABLE_OFFSET_Y = scale(-5);

    public static final int BOARD_WID = scale(64);
    public static final int BOARD_HEI = scale(64);
    public static final int BOARD_HB_WID = scale(56);
    public static final int BOARD_HB_HEI = scale(60);
    public static final int BOARD_OFFSET_X = scale(5);
    public static final int BOARD_OFFSET_Y = scale(-5);

    public static final int NPC_WID = scale(40);
    public static final int NPC_HEI = scale(52);
    public static final int NPC_HB_WID = scale(90);
    public static final int NPC_HB_HEI = scale(50);
    public static final int NPC_OFFSET_X = scale(-25);
    public static final int NPC_OFFSET_Y = scale(-7);

    public static final int LAVA_WID = scale(32);
    public static final int LAVA_HEI = scale(32);
    public static final int LAVA_HB_WID = scale(32);
    public static final int LAVA_HB_HEI = scale(25);
    public static final int LAVA_OFFSET_X = scale(0);
    public static final int LAVA_OFFSET_Y = scale(10);

    public static final int BRICK_WID = scale(68);
    public static final int BRICK_HEI = scale(73);
    public static final int BRICK_HB_WID = scale(36);
    public static final int BRICK_HB_HEI = scale(68);
    public static final int BRICK_OFFSET_X = scale(16);
    public static final int BRICK_OFFSET_Y = scale(32);

    // Projectile Constants
    public static final int ARROW_WID = scale(32);
    public static final int ARROW_HEI = scale(4);
    public static final double ARROW_SPEED = dScale(0.9);
    public static final int ARROW_OFFSET_X_RIGHT = scale(-20);
    public static final int ARROW_OFFSET_X_LEFT = scale(10);
    public static final int ARROW_OFFSET_Y = scale(20);

    public static final int LB_WID = scale(60);
    public static final int LB_HEI = scale(60);
    public static final double LB_SPEED_FAST = dScale(1);
    public static final double LB_SPEED_SLOW = dScale(0.5);
    public static final int LB_OFFSET_X = scale(40);
    public static final int LB_OFFSET_Y = scale(15);

    public static final int LB_PERIOD = scale(50);
    public static final int LB_T = scale(1);
    public static final int LB_D = scale(25);

    public static final int FB_WID = scale(50);
    public static final int FB_HEI = scale(50);
    public static final int FB_OFFSET_X = scale(40);
    public static final int FB_OFFSET_Y = scale(15);
    public static final double FIREBALL_SPEED = dScale(1.5);

    public static final int TRACKING_PROJECTILE_DISTANCE = scale(50);
    public static final int ENEMY_PROJECTILE_DMG = 5;
    public static final int FIREBALL_PROJECTILE_DMG = 10;
    public static final int PLAYER_PROJECTILE_DMG = 10;

    // Spell Constants
    public static final int FLAME_WID = scale(45);
    public static final int FLAME_HEI = scale(35);
    public static final int FLAME_OFFSET_X = scale(55);
    public static final int FLAME_OFFSET_Y = scale(1);

    public static final int LIGHTNING_WIDTH = scale(48);
    public static final int LIGHTNING_HEIGHT = scale(256);
    public static final int LIGHTNING_OFFSET_X = (int)(LIGHTNING_WIDTH / 3.8);

    public static final int FLASH_WIDTH = scale(48);
    public static final int FLASH_HEIGHT = scale(192);
    public static final int FLASH_OFFSET_X = (int)(FLASH_WIDTH / 2.4);

    // Button Constants
    public static final int BIG_BTN_WID = scale(120);
    public static final int BIG_BTN_HEI = scale(42);
    public static final int MEDIUM_BTN_WID = scale(94);
    public static final int MEDIUM_BTN_HEI = scale(34);
    public static final int SMALL_BTN_WID = scale(65);
    public static final int SMALL_BTN_HEI = scale(25);
    public static final int TINY_BTN_WID = scale(50);
    public static final int TINY_BTN_HEI = scale(20);
    public static final int SOUND_BTN_SIZE = scale(30);
    public static final int CRE_BTN_SIZE = scale(30);
    public static final int SMALL_BTN_SIZE = scale(25);

    public static final int SLIDER_BTN_SIZE = scale(15);
    public static final int SLIDER_WID = scale(150);
    public static final int SLIDER_HEI = scale(30);
    public static final int SLIDER_MIN_VALUE = scale(365);
    public static final int SLIDER_MAX_VALUE = scale(507);

    // UI
    public static final int FONT_BIG = scale(20);
    public static final int FONT_DIALOGUE = scale(12);
    public static final int FONT_MEDIUM = scale(10);
    public static final int FONT_LIGHT = scale(8);
    public static final int FONT_SMALL = scale(7);
    public static final int SHOP_SLOT_MAX_ROW = 4, SHOP_SLOT_MAX_COL = 5;
    public static final int PERK_SLOT_MAX_ROW = 4, PERK_SLOT_MAX_COL = 7;
    public static final int INVENTORY_SLOT_MAX_ROW = 5, INVENTORY_SLOT_MAX_COL = 5;
    public static final int EQUIPMENT_SLOT_MAX_ROW = 2, EQUIPMENT_SLOT_MAX_COL = 3;
    public static final int SLOT_SPACING = scale(40);

    // Effect Constants
    public static final double PARTICLE_SHIFT = 0.1;

    public static final int PLAYER_LIGHT_RADIUS = scale(75);
    public static final int CANDLE_LIGHT_RADIUS = scale(100);

    public static final double DUST1_WID = dScale(40);
    public static final double DUST1_HEI = dScale(80);
    public static final double DUST1_OFFSET_X = dScale(15);
    public static final double DUST1_OFFSET_Y = dScale(10);

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

    private static double dScale(double value) {
        return value * SCALE;
    }

}
