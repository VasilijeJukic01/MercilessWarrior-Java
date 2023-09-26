package platformer.model.inventory;

import java.awt.*;

public enum ItemRarity {
    COMMON(new Color(150, 150, 150, 100)),
    UNCOMMON(new Color(0, 170, 20, 100)),
    RARE(new Color(0, 90, 170, 100)),
    EPIC(new Color(95, 0, 170, 100)),
    LEGENDARY(new Color(190, 110, 5, 100)),
    MYTHIC(new Color(125, 0, 0, 100));

    private final Color color;

    ItemRarity(Color color) {
        this.color = color;
    }

    public Color getColor() {
        return color;
    }
}
