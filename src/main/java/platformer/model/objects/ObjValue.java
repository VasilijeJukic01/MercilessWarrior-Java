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
    ARROW_LAUNCHER_HEI((int)(ARROW_LAUNCHER_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    COIN_WID_DEF(15),
    COIN_HEI_DEF(15),
    COIN_WID((int)(COIN_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    COIN_HEI((int)(COIN_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    SHOP_WID_DEF(154),
    SHOP_HEI_DEF(132),
    SHOP_WID((int)(SHOP_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    SHOP_HEI((int)(SHOP_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    BLOCKER_WID_DEF(96),
    BLOCKER_HEI_DEF(128),
    BLOCKER_WID((int)(BLOCKER_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    BLOCKER_HEI((int)(BLOCKER_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    BLACKSMITH_WID_DEF(110),
    BLACKSMITH_HEI_DEF(85),
    BLACKSMITH_WID((int)(BLACKSMITH_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    BLACKSMITH_HEI((int)(BLACKSMITH_HEI_DEF.getValue()*Tiles.SCALE.getValue())),

    DOG_WID_DEF(64),
    DOG_HEI_DEF(64),
    DOG_WID((int)(DOG_WID_DEF.getValue()*Tiles.SCALE.getValue())),
    DOG_HEI((int)(DOG_HEI_DEF.getValue()*Tiles.SCALE.getValue()));

    private final int value;

    ObjValue(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
