package platformer.model.objects.projectiles;

import platformer.model.Tiles;

public enum PRSet {

    ARROW_DEF_WID(32),
    ARROW_DEF_HEI(4),
    ARROW_WID((int)(ARROW_DEF_WID.getValue()*Tiles.SCALE.getValue())),
    ARROW_HEI((int)(ARROW_DEF_HEI.getValue()*Tiles.SCALE.getValue())),

    ARROW_SPEED(0.75*Tiles.SCALE.getValue()),

    LB_DEF_WID(60),
    LB_DEF_HEI(60),
    LB_WID((int)(LB_DEF_WID.getValue()*Tiles.SCALE.getValue())),
    LB_HEI((int)(LB_DEF_HEI.getValue()*Tiles.SCALE.getValue())),

    LB_SPEED_FAST((1.0*Tiles.SCALE.getValue())),
    LB_SPEED_MEDIUM((0.5*Tiles.SCALE.getValue()));

    private final double value;

    PRSet(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
