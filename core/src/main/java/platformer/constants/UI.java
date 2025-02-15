package platformer.constants;

import java.awt.*;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

// Vocabulary

/*

L - Leaderboard
CRE - Credits
PORT - Portrait

 */

/**
 * This class contains all the constants related to the UI.
 */
public final class UI {

    // Menu UI
    public static final int MENU_BTN_X = (int)(GAME_WIDTH / 2.3);
    public static final int MENU_BTN1_Y = scale(170);
    public static final int MENU_BTN2_Y = scale(225);
    public static final int MENU_BTN3_Y = scale(280);
    public static final int MENU_BTN4_Y = scale(335);
    public static final int MENU_LOGO_X = scale(270);
    public static final int MENU_LOGO_Y = scale(10);
    public static final int MENU_LOGO_WID = scale(300);
    public static final int MENU_LOGO_HEI = scale(150);

    public static final int L_BUTTON_X = scale(755);
    public static final int L_BUTTON_Y = scale(10);

    public static final int CRE_BUTTON_X = scale(790);
    public static final int CRE_BUTTON_Y = scale(10);

    public static final Color OVERLAY_COLOR = new Color(31, 22, 28, 245);
    public static final Color OVERLAY_SPACE_COLOR = new Color(50, 50, 50);

    // Load/Save Game UI
    public static final int LOAD_SAVE_BTN_X = scale(330);
    public static final int DELETE_SAVE_BTN_X = scale(395);
    public static final int CLOSE_SAVE_BTN_X = scale(460);

    public static final int SAVE_LOAD_TEXT_WID = scale(130);
    public static final int SAVE_LOAD_TEXT_HEI = scale(45);
    public static final int SAVE_LOAD_TEXT_X = scale(360);
    public static final int SAVE_LOAD_TEXT_Y = scale(70);

    public static final int GAME_SLOT_WID = scale(180);
    public static final int GAME_SLOT_HEI = scale(40);
    public static final int GAME_SLOT_X = scale(330);
    public static final int GAME_SLOT_Y = scale(55);
    public static final int GAME_SLOT_SPACING = scale(60);
    public static final int GAME_SLOT_CAP = 4;

    public static final Color DATABASE_SLOT_COLOR = new Color(20, 70, 20, 220);
    public static final Color SAVE_SLOT_COLOR = new Color(20, 20, 20, 220);

    public static final int SAVE_BTN_X = scale(345);
    public static final int SAVE_CLOSE_BTN_X = scale(430);
    public static final int SAVE_BTN_Y = scale(345);
    public static final int LOAD_BTN_Y = scale(350);

    // Quest UI
    public static final int QUEST_SLOT_WID = scale(180);
    public static final int QUEST_SLOT_HEI = scale(50);
    public static final int QUEST_SLOT_X = scale(330);
    public static final int QUEST_SLOT_Y = scale(45);
    public static final int QUEST_SLOT_SPACING = scale(80);
    public static final int QUEST_SLOT_CAP = 3;

    public static final int QUEST_TXT_WID = scale(180);
    public static final int QUEST_TXT_HEI = scale(40);
    public static final int QUEST_TEXT_X = scale(330);
    public static final int QUEST_TEXT_Y = scale(65);

    public static final int QUEST_BTN_X = scale(388);
    public static final int QUEST_BTN_Y = scale(350);

    public static final int QUEST_BTN_PREV_X = scale(330);
    public static final int QUEST_BTN_NEXT_X = scale(485);

    public static final Color QUEST_SLOT_COLOR = new Color(20, 20, 20, 220);
    public static final Color QUEST_SLOT_REPEATABLE_COLOR = new Color(20, 70, 68, 220);
    public static final Color QUEST_SLOT_COMPLETE = new Color(22, 70, 20, 220);

    // Controls UI
    public static final int CONTROLS_TXT_WID = scale(180);
    public static final int CONTROLS_TXT_HEI = scale(40);
    public static final int CONTROLS_TXT_X = scale(330);
    public static final int CONTROLS_TXT_Y = scale(85);

    public static final int CTRL_ROW_TXT_X = scale(300);
    public static final int CTRL_ROW_TXT_Y = scale(150);
    public static final int CTRL_TXT_Y_SPACING = scale(20);

    // Leaderboards UI
    public static final int BOARD_TXT_WID = scale(180);
    public static final int BOARD_TXT_HEI = scale(40);
    public static final int BOARD_TXT_X = scale(330);
    public static final int BOARD_TXT_Y = scale(85);

    public static final int BOARD_X1 = scale(335);
    public static final int BOARD_X2 = scale(430);
    public static final int BOARD_X3 = scale(485);
    public static final int BOARD_START_Y = scale(160);
    public static final int BOARD_SPACING = scale(15);

    public static final Color BOARD_COLOR_TOP = new Color(255, 185, 0);
    public static final Color BOARD_COLOR_SECOND = new Color(145, 145, 145);
    public static final Color BOARD_COLOR_THIRD = new Color(170, 85, 0);
    public static final Color BOARD_COLOR_TOP_10 = new Color(165, 0, 95);

    // HUD
    public static final Color INFO_TXT_COLOR = new Color(255, 255, 255, 150);

    public static final int PLAYER_NAME_X = scale(1.5);
    public static final int PLAYER_NAME_Y = scale(10);
    public static final int FPS_X = scale(1.5);
    public static final int FPS_Y = scale(20);
    public static final int UPS_X = scale(50);
    public static final int UPS_Y = scale(20);

    public static final int HEALTH_WID = 134;
    public static final int STAMINA_WID = 134;
    public static final int EXP_WID = 149;

    public static final int HUD_WID = scale(192);
    public static final int HUD_HEI = scale(92);
    public static final int HUD_X = scale(10);
    public static final int HUD_Y = scale(15);
    public static final int PORT_WID = scale(40);
    public static final int PORT_HEI = scale(40);
    public static final int PORT_X = scale(18);
    public static final int PORT_Y = scale(22);

    public static final int COINS_X = scale(90);
    public static final int COINS_Y = scale(82);
    public static final int LVL_X = scale(170);
    public static final int LVL_Y = scale(67);

    public static final int HP_X = scale(64.5);
    public static final int HP_Y = scale(27);
    public static final int ST_X = scale(64.5);
    public static final int ST_Y = scale(44);
    public static final int XP_X = scale(48.5);
    public static final int XP_Y = scale(61);

    public static final int HP_HEI = scale(12);
    public static final int ST_HEI = scale(12);
    public static final int XP_HEI = scale(7);

    public static final int COOLDOWN_SLOT_SIZE = scale(22.5);
    public static final int COOLDOWN_SLOT_Y = scale(80);
    public static final int COOLDOWN_SLOT_X = scale(707);
    public static final int COOLDOWN_SLOT_SPACING = scale(30);

    public static final int BOSS_BAR_WID = scale(500);
    public static final int BOSS_BAR_HEI = scale(50);
    public static final int BOSS_BAR_X = scale(180);
    public static final int BOSS_BAR_Y = scale(80);

    // Dialogue UI
    public static final int DIALOGUE_BOX_X = scale(270);
    public static final int DIALOGUE_BOX_Y = scale(350);
    public static final int DIALOGUE_BOX_WID = scale(300);
    public static final int DIALOGUE_BOX_HEI = scale(80);

    public static final int DIALOGUE_X = DIALOGUE_BOX_X + scale(20);
    public static final int DIALOGUE_Y = DIALOGUE_BOX_Y + scale(20);
    public static final int DIALOGUE_LINE = DIALOGUE_BOX_WID - scale(30);

    // Audio UI
    public static final int SFX_X = scale(525);
    public static final int SFX_Y = scale(175);
    public static final int MUSIC_X = scale(525);
    public static final int MUSIC_Y = scale(225);
    public static final int MUSIC_SLIDER_BTN_X = scale(380);
    public static final int MUSIC_SLIDER_BTN_Y = scale(224);
    public static final int SFX_SLIDER_BTN_X = scale(380);
    public static final int SFX_SLIDER_BTN_Y = scale(174);

    public static final int MUSIC_SLIDER_X = scale(360);
    public static final int MUSIC_SLIDER_Y = scale(225);
    public static final int SFX_SLIDER_X = scale(360);
    public static final int SFX_SLIDER_Y = scale(175);

    // Overlay UI
    public static final int OVERLAY_WID = scale(300);
    public static final int OVERLAY_HEI = scale(350);
    public static final int OVERLAY_X = scale(270);
    public static final int OVERLAY_Y = scale(50);

    // Pause/Options UI
    public static final int OPTIONS_TEXT_WID = scale(180);
    public static final int OPTIONS_TEXT_HEI = scale(40);
    public static final int PAUSE_TEXT_WID = scale(180);
    public static final int PAUSE_TEXT_HEI = scale(40);
    public static final int SFX_TEXT_WID = scale(50);
    public static final int SFX_TEXT_HEI = scale(25);
    public static final int MUSIC_TEXT_WID = scale(70);
    public static final int MUSIC_TEXT_HEI = scale(25);
    public static final int VOLUME_TEXT_WID = scale(110);
    public static final int VOLUME_TEXT_HEI = scale(30);

    public static final int CONTINUE_BTN_X = scale(330);
    public static final int CONTINUE_BTN_Y = scale(350);
    public static final int RETRY_BTN_X = scale(405);
    public static final int RETRY_BTN_Y = scale(350);
    public static final int EXIT_BTN_X = scale(480);
    public static final int EXIT_BTN_Y = scale(350);

    public static final int OPTIONS_TEXT_X = scale(330);
    public static final int OPTIONS_TEXT_Y = scale(85);
    public static final int PAUSE_TEXT_X = scale(330);
    public static final int PAUSE_TEXT_Y = scale(85);
    public static final int SFX_TEXT_X = scale(280);
    public static final int SFX_TEXT_Y = scale(180);
    public static final int MUSIC_TEXT_X = scale(280);
    public static final int MUSIC_TEXT_Y = scale(230);
    public static final int VOLUME_TEXT_X = scale(365);
    public static final int VOLUME_TEXT_Y = scale(140);

    // Game Over UI
    public static final int DEAD_TEXT_WID = scale(180);
    public static final int DEAD_TEXT_HEI = scale(40);
    public static final int RESPAWN_TEXT_WID = scale(110);
    public static final int RESPAWN_TEXT_HEI = scale(30);
    public static final int MENU_TEXT_WID = scale(90);
    public static final int MENU_TEXT_HEI = scale(30);

    public static final int RETRY_X = scale(340);
    public static final int RETRY_Y = scale(320);
    public static final int MENU_X = scale(480);
    public static final int MENU_Y = scale(320);

    public static final int DEAD_TEXT_X = scale(330);
    public static final int DEAD_TEXT_Y = scale(120);
    public static final int RESPAWN_TEXT_X = scale(300);
    public static final int RESPAWN_TEXT_Y = scale(250);
    public static final int MENU_TEXT_X = scale(450);
    public static final int MENU_TEXT_Y = scale(250);

    // Shop UI
    public static final int SHOP_PANEL_WID = scale(202);
    public static final int SHOP_PANEL_HEI = scale(240);
    public static final int SHOP_TEXT_WID = scale(180);
    public static final int SHOP_TEXT_HEI = scale(60);
    public static final int SLOT_SIZE = scale(40);

    public static final int SHOP_TEXT_X = scale(330);
    public static final int SHOP_TEXT_Y = scale(45);
    public static final int BUY_BTN_X = scale(166);
    public static final int BUY_BTN_Y = scale(358);
    public static final int SELL_BTN_X = scale(616);
    public static final int SELL_BTN_Y = scale(358);
    public static final int LEAVE_BTN_X = scale(365);
    public static final int LEAVE_BTN_Y = scale(360);

    public static final int PREV_BUY_BTN_X = scale(90);
    public static final int NEXT_BUY_BTN_X = scale(267);
    public static final int PREV_SELL_BTN_X = scale(540);
    public static final int NEXT_SELL_BTN_X = scale(716);
    public static final int SMALL_SHOP_BTN_Y = scale(355);

    public static final int SHOP_BUY_OVERLAY_X = scale(90);
    public static final int SHOP_BUY_OVERLAY_Y = scale(100);
    public static final int SHOP_BUY_SLOT_X = scale(110);
    public static final int SHOP_BUY_SLOT_Y = scale(120);

    public static final int SHOP_SELL_OVERLAY_X = scale(540);
    public static final int SHOP_SELL_OVERLAY_Y = scale(100);
    public static final int SHOP_SELL_SLOT_X = scale(560);
    public static final int SHOP_SELL_SLOT_Y = scale(120);

    public static final int COST_TEXT_X = scale(310);
    public static final int POCKET_TEXT_X = scale(470);
    public static final int COST_TEXT_Y = scale(150);
    public static final int SHOP_ITEM_NAME_X = scale(310);
    public static final int SHOP_ITEM_NAME_Y = scale(170);
    public static final int SHOP_ITEM_DESC_X = scale(310);
    public static final int SHOP_ITEM_DESC_Y = scale(190);

    public static final int ITEM_SIZE = scale(20);
    public static final int ITEM_OFFSET_X = scale(10);
    public static final int ITEM_OFFSET_Y = scale(8);
    public static final int ITEM_COUNT_OFFSET_X = scale(15);
    public static final int ITEM_COUNT_OFFSET_Y = scale(26);

    public static final int PERKS_OVERLAY_WID = scale(700);
    public static final int PERKS_OVERLAY_HEI = scale(410);

    public static final int PERKS_OVERLAY_X = scale(65);
    public static final int PERKS_OVERLAY_Y = scale(30);
    public static final int PERKS_TEXT_X = scale(330);
    public static final int PERKS_TEXT_Y = scale(60);
    public static final int BUY_PERK_BTN_X = scale(300);
    public static final int BUY_PERK_BTN_Y = scale(380);
    public static final int LEAVE_PERK_BTN_X = scale(440);
    public static final int LEAVE_PERK_BTN_Y = scale(380);
    public static final int PERK_SLOT_X = scale(110);
    public static final int PERK_SLOT_Y = scale(140);
    public static final int TOKENS_TEXT_X = scale(550);
    public static final int TOKENS_TEXT_Y = scale(150);
    public static final int PERK_NAME_X = scale(550);
    public static final int PERK_NAME_Y = scale(240);
    public static final int PERK_COST_X = scale(550);
    public static final int PERK_COST_Y = scale(255);
    public static final int PERK_DESC_X = scale(550);
    public static final int PERK_DESC_Y = scale(275);

    public static final int PERK_SLOT_SPACING = scale(60);

    public static final int SHOP_SLOT_CAP = 5;
    public static final int CRAFT_SLOT_CAP = 5;
    public static final int INVENTORY_SLOT_CAP = 5;

    public static final Color PERK_SLOT_LOCK_COL = new Color(0, 0, 0, 200);
    public static final Color PERK_SLOT_UPGRADE_COL = new Color(255, 100, 0, 100);

    // Inventory UI
    public static final int INV_OVERLAY_WID = scale(705);
    public static final int INV_OVERLAY_HEI = scale(380);
    public static final int INV_OVERLAY_X = scale(65);
    public static final int INV_OVERLAY_Y = scale(30);
    public static final int INV_TEXT_WID = scale(220);
    public static final int INV_TEXT_HEI = scale(40);
    public static final int INV_TEXT_X = scale(310);
    public static final int INV_TEXT_Y = scale(45);

    public static final int BACKPACK_WID = scale(240);
    public static final int BACKPACK_HEI = scale(240);
    public static final int BACKPACK_X = scale(120);
    public static final int BACKPACK_Y = scale(100);
    public static final int BACKPACK_SLOT_X = scale(140);
    public static final int BACKPACK_SLOT_Y = scale(120);

    public static final int EQUIPMENT_WID = scale(170);
    public static final int EQUIPMENT_HEI = scale(150);
    public static final int EQUIPMENT_X = scale(380);
    public static final int EQUIPMENT_Y = scale(100);
    public static final int EQUIPMENT_SLOT_X = scale(400);
    public static final int EQUIPMENT_SLOT_Y = scale(115);
    public static final int EQUIPMENT_SLOT_SPACING = scale(95);

    public static final int INV_PLAYER_WID = scale(40);
    public static final int INV_PLAYER_HEI = scale(70);
    public static final int INV_PLAYER_X = scale(446);
    public static final int INV_PLAYER_Y = scale(140);

    public static final int INV_ITEM_NAME_X = scale(380);
    public static final int INV_ITEM_NAME_Y = scale(300);
    public static final int INV_ITEM_VALUE_X = scale(500);
    public static final int INV_ITEM_VALUE_Y = scale(300);
    public static final int INV_ITEM_DESC_X = scale(380);
    public static final int INV_ITEM_DESC_Y = scale(320);

    public static final int INV_BONUS_X = scale(600);
    public static final int INV_BONUS_Y = scale(110);
    public static final int INV_BONUS_SPACING = scale(16);

    public static final int PREV_BTN_X = scale(120);
    public static final int NEXT_BTN_X = scale(330);
    public static final int USE_BTN_X = scale(153);
    public static final int EQUIP_BTN_X = scale(213);
    public static final int DROP_BTN_X = scale(273);
    public static final int INV_SMALL_BTN_Y = scale(355);
    public static final int INV_MEDIUM_BTN_Y = scale(358);

    public static final int UNEQUIP_BTN_X = scale(440);
    public static final int UNEQUIP_BTN_Y = scale(260);

    // Looting UI
    public static final int LOOT_SLOT_X = scale(320);
    public static final int LOOT_SLOT_Y = scale(120);

    public static final int LOOT_BTN_BTN_Y = scale(348);
    public static final int TAKE_BTN_X = scale(318);
    public static final int TAKE_ALL_BTN_X = scale(388);
    public static final int CLOSE_BTN_X = scale(458);

    // Crafting UI
    public static final int CRAFT_VAL_TEXT_X = scale(400);
    public static final int CRAFT_VAL_TEXT_Y = scale(150);
    public static final int CRAFT_VAL_ITEM_NAME_X = scale(400);
    public static final int CRAFT_VAL_ITEM_NAME_Y = scale(130);
    public static final int CRAFT_VAL_ITEM_DESC_X = scale(400);
    public static final int CRAFT_VAL_ITEM_DESC_Y = scale(170);

    // Minimap UI
    public static final int MAP_OVERLAY_WID = scale(700);
    public static final int MAP_OVERLAY_HEI = scale(410);

    public static final int MAP_OVERLAY_X = scale(65);
    public static final int MAP_OVERLAY_Y = scale(30);

    public static final int RADAR_WID = scale(125);
    public static final int RADAR_HEI = scale(65);

    public static final int RADAR_X = scale(700);
    public static final int RADAR_Y = scale(7);

    // Credits UI
    public static final int CREDITS_TXT_WID = scale(180);
    public static final int CREDITS_TXT_HEI = scale(40);
    public static final int CREDITS_TXT_X = scale(330);
    public static final int CREDITS_TXT_Y = scale(85);

    public static final int CREDITS_POSITION_X = scale(310);
    public static final int CREDITS_POSITION_Y = scale(150);
    public static final int CREDITS_SPACING = scale(20);

    public static final Color CREDITS_COLOR = new Color(20, 103, 59, 255);

    // Tutorial UI
    public static final int TUTORIAL_IMAGE_X = scale(150);
    public static final int TUTORIAL_IMAGE_Y = scale(155);

    public static final int TUTORIAL_TXT_X = scale(350);
    public static final int TUTORIAL_TXT_Y = scale(170);

    public static final int TUTORIAL_EXIT_X = scale(185);
    public static final int TUTORIAL_EXIT_Y = scale(270);

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
