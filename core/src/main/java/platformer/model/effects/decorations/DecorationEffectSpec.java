package platformer.model.effects.decorations;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

/**
 * Declarative specification for a single ambient effect attached to a decoration ID in decoration_effects.json.
 */
@Getter
@Setter
public class DecorationEffectSpec {

    private String type;
    private String color = "#FFFFFF";
    private String secondaryColor;
    private double rate = 0.02;
    private String area = "FULL";
    private Double offsetX;
    private Double offsetY;
    private double minSize = 2.0;
    private double maxSize = 4.0;
    private boolean nightOnly = false;
    private boolean dayOnly = false;

    private transient DecorationEffectType parsedType;
    private transient Color parsedColor;
    private transient Color parsedSecondaryColor;

    public DecorationEffectType getEffectType() {
        if (parsedType == null && type != null) {
            try {
                parsedType = DecorationEffectType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }
        return parsedType;
    }

    public Color getPrimaryColor() {
        if (parsedColor == null) {
            parsedColor = decodeColor(color, Color.WHITE);
        }
        return parsedColor;
    }

    public Color getSecondaryColor() {
        if (parsedSecondaryColor == null) {
            parsedSecondaryColor = (secondaryColor != null && !secondaryColor.isBlank()) ? decodeColor(secondaryColor, getPrimaryColor()) : getPrimaryColor();
        }
        return parsedSecondaryColor;
    }

    private static Color decodeColor(String hex, Color fallback) {
        if (hex == null || hex.isBlank()) return fallback;
        try {
            return Color.decode(hex.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}