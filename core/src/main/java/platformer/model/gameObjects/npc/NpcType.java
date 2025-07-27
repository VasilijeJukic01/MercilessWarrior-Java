package platformer.model.gameObjects.npc;

import java.awt.*;

import static platformer.constants.AnimConstants.RORIC_H;
import static platformer.constants.AnimConstants.RORIC_W;
import static platformer.constants.Constants.SCALE;

public enum NpcType {
    // Format: NpcType(sprites, color, name, dialogueBehavior, wid, hei, hitboxWidth, hitboxHeight, xOffset, yOffset, spriteW, spriteH)
    ANITA(4, new Color(0xd491ed), "Anita Manita", DialogueBehavior.SEQUENTIAL,
            scale(40), scale(52), scale(80), scale(45), scale(-20), scale(4), 32, 32
    ),
    NIKOLAS(4, new Color(0xe06b51), "Nikolas The Red Itch", DialogueBehavior.RANDOM,
            scale(40), scale(52), scale(80), scale(45), scale(-20), scale(4), 32, 32
    ),
    SIR_DEJANOVIC(4, new Color(0x67abeb), "Sir Dejanovic", DialogueBehavior.RANDOM,
            scale(40), scale(52), scale(80), scale(45), scale(-20), scale(4), 32, 32
    ),
    KRYSANTHE(8, new Color(0xFF70EE), "Krysanthe", DialogueBehavior.RANDOM,
            scale(110), scale(90), scale(80), scale(42), scale(10), scale(60), 220, 160
    ),
    RORIC(12, new Color(0x9CDD4A), "Roric", DialogueBehavior.SEQUENTIAL,
    scale(250), scale(150), scale(80), scale(50), scale(90), scale(96), RORIC_W, RORIC_H);

    private final int sprites;
    private final String name;
    private final Color nameColor;
    private final DialogueBehavior dialogueBehavior;
    private final int wid, hei;
    private final int hitboxWidth, hitboxHeight;
    private final int xOffset, yOffset;
    private final int spriteW, spriteH;

    NpcType(int sprites, Color color, String name, DialogueBehavior dialogueBehavior, int wid, int hei, int hitboxWidth, int hitboxHeight, int xOffset, int yOffset, int spriteW, int spriteH) {
        this.sprites = sprites;
        this.nameColor = color;
        this.name = name;
        this.dialogueBehavior = dialogueBehavior;
        this.wid = wid;
        this.hei = hei;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.spriteW = spriteW;
        this.spriteH = spriteH;
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

    public DialogueBehavior getDialogueBehavior() {
        return dialogueBehavior;
    }

    public int getWid() {
        return wid;
    }

    public int getHei() {
        return hei;
    }

    public int getHitboxWidth() {
        return hitboxWidth;
    }

    public int getHitboxHeight() {
        return hitboxHeight;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getSpriteW() {
        return spriteW;
    }

    public int getSpriteH() {
        return spriteH;
    }

    private static int scale(int value) {
        return (int) (value * SCALE);
    }
}
