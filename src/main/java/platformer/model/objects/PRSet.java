package platformer.model.objects;

import platformer.model.Tiles;

public enum PRSet {

    ARROW_DEF_WID(32),
    ARROW_DEF_HEI(4),
    ARROW_WID((int)(ARROW_DEF_WID.getValue()*Tiles.SCALE.getValue())),
    ARROW_HEI((int)(ARROW_DEF_HEI.getValue()*Tiles.SCALE.getValue())),

    ARROW_SPEED((int)(0.75*Tiles.SCALE.getValue()));


    private final double value;

    PRSet(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }
}
