package platformer.model.objects;

import platformer.model.Tiles;

public enum ObjValue {
    HEAL_POTION_VAL(15),
    STAMINA_POTION_VAL(10),

    CONTAINER_WID_DEF(40),
    CONTAINER_HEI_DEF(30),
    CONTAINER_WID((int)(CONTAINER_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    CONTAINER_HEI((int)(CONTAINER_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    POTION_WID_DEF(12),
    POTION_HEI_DEF(16),
    POTION_WID((int)(POTION_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    POTION_HEI((int)(POTION_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    SPIKE_WID_DEF(32),
    SPIKE_HEI_DEF(35),
    SPIKE_WID((int)(SPIKE_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    SPIKE_HEI((int)(SPIKE_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    ARROW_LAUNCHER_WID_DEF(32),
    ARROW_LAUNCHER_HEI_DEF(32),
    ARROW_LAUNCHER_WID((int)(ARROW_LAUNCHER_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    ARROW_LAUNCHER_HEI((int)(ARROW_LAUNCHER_HEI_DEF.getValue()*Tiles.SCALE.getValue()));

    private final int value;

    ObjValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
