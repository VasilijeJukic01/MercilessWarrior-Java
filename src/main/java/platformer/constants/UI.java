package platformer.constants;

import static platformer.constants.Constants.GAME_WIDTH;
import static platformer.constants.Constants.SCALE;

public class UI {

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

    // Controls UI
    public static final int KEY_SIZE = scale(20);
    public static final int CONTROLS_TXT_WID = scale(180);
    public static final int CONTROLS_TXT_HEI = scale(40);
    public static final int CONTROLS_TXT_X = scale(330);
    public static final int CONTROLS_TXT_Y = scale(85);

    public static final int CTRL_ROW_TXT_X = scale(300);

    public static final int CTRL_ROW1_TXT_Y = scale(150);
    public static final int CTRL_ROW2_TXT_Y = scale(170);
    public static final int CTRL_ROW3_TXT_Y = scale(190);
    public static final int CTRL_ROW4_TXT_Y = scale(210);
    public static final int CTRL_ROW5_TXT_Y = scale(230);
    public static final int CTRL_ROW6_TXT_Y = scale(250);
    public static final int CTRL_ROW7_TXT_Y = scale(270);
    public static final int CTRL_ROW8_TXT_Y = scale(290);

    public static final int K1_X = scale(390), K_ROW1 = scale(135);
    public static final int K2_X = scale(410);
    public static final int K3_X = scale(335), K_ROW2 = scale(155);
    public static final int K4_X = scale(345), K_ROW3 = scale(175);
    public static final int K5_X = scale(345), K_ROW4 = scale(195);
    public static final int K6_X = scale(370), K_ROW5 = scale(215);
    public static final int K7_X = scale(330), K_ROW6 = scale(235);
    public static final int K8_X = scale(360), K_ROW7 = scale(255);
    public static final int K9_X = scale(370), K_ROW8 = scale(275);
    public static final int K10_X = scale(398);

    // HUD
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

    public static final int COOLDOWN_TXT_X = scale(710);
    public static final int COOLDOWN_TXT_Y = scale(10);

    public static final int BOSS_BAR_WID = scale(500);
    public static final int BOSS_BAR_HEI = scale(50);
    public static final int BOSS_BAR_X = scale(180);
    public static final int BOSS_BAR_Y = scale(80);

    // Audio UI
    public static final int SFX_X = scale(450);
    public static final int SFX_Y = scale(148);
    public static final int MUSIC_X = scale(450);
    public static final int MUSIC_Y = scale(198);
    public static final int SLIDER_BTN_X = scale(330);
    public static final int SLIDER_BTN_Y = scale(290);

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
    public static final int SFX_TEXT_WID = scale(60);
    public static final int SFX_TEXT_HEI = scale(30);
    public static final int MUSIC_TEXT_WID = scale(90);
    public static final int MUSIC_TEXT_HEI = scale(30);
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
    public static final int SFX_TEXT_X = scale(325);
    public static final int SFX_TEXT_Y = scale(150);
    public static final int MUSIC_TEXT_X = scale(325);
    public static final int MUSIC_TEXT_Y = scale(200);
    public static final int VOLUME_TEXT_X = scale(365);
    public static final int VOLUME_TEXT_Y = scale(260);

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
    public static final int SHOP_OVERLAY_WID = scale(400);
    public static final int SHOP_OVERLAY_HEI = scale(340);
    public static final int SHOP_TEXT_WID = scale(180);
    public static final int SHOP_TEXT_HEI = scale(60);
    public static final int SLOT_SIZE = scale(40);

    public static final int SHOP_OVERLAY_X = scale(220);
    public static final int SHOP_OVERLAY_Y = scale(50);
    public static final int SHOP_TEXT_X = scale(330);
    public static final int SHOP_TEXT_Y = scale(80);
    public static final int BUY_BTN_X = scale(300);
    public static final int BUY_BTN_Y = scale(330);
    public static final int LEAVE_BTN_X = scale(440);
    public static final int LEAVE_BTN_Y = scale(330);
    public static final int SLOT_X = scale(290);
    public static final int SLOT_Y = scale(160);
    public static final int COST_TEXT_X = scale(530);
    public static final int COST_TEXT_Y = scale(145);

    public static final int ITEM_SIZE = scale(20);
    public static final int ITEM_OFFSET_X = scale(10);
    public static final int ITEM_OFFSET_Y = scale(8);
    public static final int ITEM_COUNT_OFFSET_X = scale(18);
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

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
