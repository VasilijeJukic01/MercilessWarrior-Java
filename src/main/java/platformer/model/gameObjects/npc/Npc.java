package platformer.model.gameObjects.npc;

import platformer.model.entities.Direction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

import static platformer.constants.Constants.*;

public class Npc extends GameObject {

    private static final List<NpcType> randomizedDialogues = List.of(NpcType.NIKOLAS);

    private final NpcType npcType;
    private int progression = 1;
    private int dialogueIndicator = 0;
    private Direction direction = Direction.RIGHT;

    public Npc(ObjType objType, int xPos, int yPos, NpcType npcType) {
        super(objType, xPos - (int)(35*SCALE), yPos + (int)(10*SCALE));
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
        BufferedImage image = animations[animIndex];

        if (direction == Direction.RIGHT) {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-image.getWidth(null), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            image = op.filter(image, null);
        }

        g.drawImage(image, x, y, NPC_WID, NPC_HEI, null);
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

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public NpcType getNpcType() {
        return npcType;
    }
}
