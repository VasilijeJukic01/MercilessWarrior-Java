package platformer.constants;

import static platformer.constants.Constants.SCALE;

public class UI {

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

    public static final int COINS_X = scale(493);
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

    private static int scale(double value) {
        return (int)(value * SCALE);
    }

}
