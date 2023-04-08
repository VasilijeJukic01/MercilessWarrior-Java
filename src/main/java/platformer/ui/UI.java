package platformer.ui;

import platformer.model.Tiles;

public enum UI {

    B_WIDTH_DEFAULT(140),
    B_HEIGHT_DEFAULT(50),

    B_WIDTH((int)(B_WIDTH_DEFAULT.getValue() * Tiles.SCALE.getValue())),
    B_HEIGHT((int)(B_HEIGHT_DEFAULT.getValue() * Tiles.SCALE.getValue())),

    SOUND_B_DEFAULT(30),
    SOUND_B_SIZE((int)(UI.SOUND_B_DEFAULT.getValue()*Tiles.SCALE.getValue())),

    CRE_B_DEFAULT(30),
    CRE_B_SIZE((int)(UI.CRE_B_DEFAULT.getValue()*Tiles.SCALE.getValue())),

    VOLUME_DEFAULT_WIDTH(15),
    VOLUME_DEFAULT_HEIGHT(15),
    SLIDER_DEFAULT_WIDTH(215),
    SLIDER_DEFAULT_HEIGHT(30),
    VOLUME_WIDTH((int)(VOLUME_DEFAULT_WIDTH.getValue()*Tiles.SCALE.getValue())),
    VOLUME_HEIGHT((int)(VOLUME_DEFAULT_HEIGHT.getValue()*Tiles.SCALE.getValue())),
    SLIDER_WIDTH((int)(SLIDER_DEFAULT_WIDTH.getValue()*Tiles.SCALE.getValue())),
    SLIDER_HEIGHT((int)(SLIDER_DEFAULT_HEIGHT.getValue()*Tiles.SCALE.getValue()));

    private final int value;

    UI(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
