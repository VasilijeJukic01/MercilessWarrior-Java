package platformer.ui;

import static platformer.constants.Constants.SCALE;

public enum UI {

    B_WIDTH_DEFAULT(120),
    B_HEIGHT_DEFAULT(42),

    SB_WIDTH_DEFAULT(94),
    SB_HEIGHT_DEFAULT(34),

    B_WIDTH((int)(B_WIDTH_DEFAULT.getValue() * SCALE)),
    B_HEIGHT((int)(B_HEIGHT_DEFAULT.getValue() * SCALE)),

    SB_WIDTH((int)(SB_WIDTH_DEFAULT.getValue() * SCALE)),
    SB_HEIGHT((int)(SB_HEIGHT_DEFAULT.getValue() * SCALE)),

    SOUND_B_DEFAULT(30),
    SOUND_B_SIZE((int)(UI.SOUND_B_DEFAULT.getValue()*SCALE)),

    CRE_B_DEFAULT(30),
    CRE_B_SIZE((int)(UI.CRE_B_DEFAULT.getValue()*SCALE)),

    VOLUME_DEFAULT_WIDTH(15),
    VOLUME_DEFAULT_HEIGHT(15),
    SLIDER_DEFAULT_WIDTH(215),
    SLIDER_DEFAULT_HEIGHT(30),
    VOLUME_WIDTH((int)(VOLUME_DEFAULT_WIDTH.getValue()*SCALE)),
    VOLUME_HEIGHT((int)(VOLUME_DEFAULT_HEIGHT.getValue()*SCALE)),
    SLIDER_WIDTH((int)(SLIDER_DEFAULT_WIDTH.getValue()*SCALE)),
    SLIDER_HEIGHT((int)(SLIDER_DEFAULT_HEIGHT.getValue()*SCALE));

    private final int value;

    UI(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
