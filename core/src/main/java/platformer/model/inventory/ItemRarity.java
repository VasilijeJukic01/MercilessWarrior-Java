package platformer.model.inventory;

import java.awt.*;

public enum ItemRarity {
    COMMON(new Color(150, 150, 150, 100), new Color(255, 255, 255)),
    UNCOMMON(new Color(0, 170, 20, 100), new Color(90, 255, 90)),
    RARE(new Color(0, 90, 170, 100), new Color(100, 180, 255)),
    EPIC(new Color(95, 0, 170, 100), new Color(200, 120, 255)),
    LEGENDARY(new Color(245, 105, 5, 100), new Color(255, 150, 50)),
    MYTHIC(new Color(125, 0, 0, 100), new Color(255, 80, 80));

    private final Color color;
    private final Color textColor;

    ItemRarity(Color color, Color textColor) {
        this.color = color;
        this.textColor = textColor;
    }

    public Color getColor() {
        return color;
    }

    public Color getTextColor() {
        return textColor;
    }
}
