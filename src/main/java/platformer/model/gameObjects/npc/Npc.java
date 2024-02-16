package platformer.model.gameObjects.npc;

import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.*;

public class Npc extends GameObject {

    private static final List<NpcType> randomizedDialogues = List.of(NpcType.NIKOLAS);

    private final NpcType npcType;
    private int progression = 1;
    private int dialogueIndicator = 0;

    public Npc(ObjType objType, int xPos, int yPos, NpcType npcType) {
        super(objType, xPos, yPos + (int)(10*SCALE));
        super.animSpeed = 34;
        this.npcType = npcType;
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(NPC_HB_WID, NPC_HB_HEI);
        super.xOffset = NPC_OFFSET_X;
        super.yOffset = NPC_OFFSET_Y;
    }

    // Core
    @Override
    public void update() {
        if (animate) updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset;
        g.drawImage(animations[animIndex], x, y, NPC_WID, NPC_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public void progress() {
        this.progression++;
    }

    public void increaseDialogueIndicator() {
        if (dialogueIndicator + 1 > progression) return;
        this.dialogueIndicator++;
    }

    public int getDialogueIndicator() {
        if (randomizedDialogues.contains(npcType)) return -1;
        return dialogueIndicator;
    }

    public int getProgression() {
        return progression;
    }

    public NpcType getNpcType() {
        return npcType;
    }
}
