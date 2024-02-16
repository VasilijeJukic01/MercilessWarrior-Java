package platformer.model.gameObjects.npc;

import java.awt.*;

public enum NpcType {
    ANITA(4, new Color(0xd491ed), "Anita Manita"),
    NIKOLAS(4, new Color(0xe06b51), "Nikolas The Red Itch");

    private final int sprites;
    private final String name;
    private final Color nameColor;

    NpcType(int sprites, Color color, String name) {
        this.sprites = sprites;
        this.nameColor = color;
        this.name = name;
    }

    public int getSprites() {
        return sprites;
    }

    public String getName() {
        return name;
    }

    public Color getNameColor() {
        return nameColor;
    }

}
