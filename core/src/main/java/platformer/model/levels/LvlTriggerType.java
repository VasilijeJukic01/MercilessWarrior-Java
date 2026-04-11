package platformer.model.levels;

import java.awt.*;

/**
 * Defines the various types of spatial logic triggers available in the game.
 * <p>
 * Triggers are invisible zones placed via the Level Editor's Logic Layer.
 * They dictate world flow and AI behavior.
 */
public enum LvlTriggerType {
    // ID, Line Color, Display Name
    FOLLOWER_BOUNDARY(0, new Color(0, 150, 255, 100), "AI BOUNDARY"),
    KILL_ZONE(1, new Color(255, 0, 0, 100), "KILL ZONE"),
    LOAD_LEFT_LEVEL(2, new Color(0, 255, 0, 100), "LOAD LEFT"),
    LOAD_RIGHT_LEVEL(3, new Color(0, 255, 0, 100), "LOAD RIGHT"),
    LOAD_UP_LEVEL(4, new Color(0, 255, 0, 100), "LOAD UP"),
    LOAD_DOWN_LEVEL(5, new Color(0, 255, 0, 100), "LOAD DOWN"),
    SPAWN_A(6, new Color(255, 255, 255, 150), "SPAWN A"),
    SPAWN_B(7, new Color(200, 200, 200, 150), "SPAWN B"),
    SPAWN_C(8, new Color(150, 150, 150, 150), "SPAWN C"),
    SPAWN_D(9, new Color(100, 100, 100, 150), "SPAWN D"),
    MAX(10, Color.BLACK, "");

    private final int id;
    private final Color color;
    private final String label;

    LvlTriggerType(int id, Color color, String label) {
        this.id = id;
        this.color = color;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public Color getColor() {
        return color;
    }

    public String getLabel() {
        return label;
    }
}