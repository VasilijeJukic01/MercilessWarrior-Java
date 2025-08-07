package platformer.model.gameObjects.npc;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Npc extends GameObject implements Interactable {

    private final NpcType npcType;
    private int progression = 1;
    private int dialogueIndicator = 0;
    private Direction direction = Direction.RIGHT;

    public Npc(ObjType objType, int xPos, int yPos, NpcType npcType) {
        super(objType, xPos - (int)(25*SCALE), yPos + npcType.getHei() - TILES_SIZE);
        configureAnimSpeed();
        this.npcType = npcType;
        generateHitBox();
    }

    private void generateHitBox() {
        super.animate = true;
        initHitBox(npcType.getHitboxWidth(), npcType.getHitboxHeight());
        configureHitboxOffsets();
        super.xOffset = npcType.getXOffset();
        super.yOffset = npcType.getYOffset();
    }

    private void configureAnimSpeed() {
        if (npcType == NpcType.RORIC) super.animSpeed = 20;
        else super.animSpeed = 34;
    }

    private void configureHitboxOffsets() {
        if (npcType != NpcType.RORIC) return;
        hitBox.y -= (int)(100 * SCALE);
    }

    @Override
    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= npcType.getSprites()) animIndex = 0;
        }
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

            int hitBoxCenterX = (int) (hitBox.x + hitBox.width / 2);
            x = hitBoxCenterX - (npcType.getWid() / 2) - xLevelOffset;

            // Bad sprite alignment fix >.<
            if (npcType == NpcType.KRYSANTHE) x += (int) (20 * SCALE);
        }

        g.drawImage(image, x, y, npcType.getWid(), npcType.getHei(), null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    @Override
    public void onEnter(Player player) {
        // No specific action on enter
    }

    @Override
    public void onIntersect(Player player) {
        if (player.getHitBox().x + player.getHitBox().width < getHitBox().x + getHitBox().width / 1.2) {
            setDirection(Direction.RIGHT);
        } else {
            setDirection(Direction.LEFT);
        }
    }

    @Override
    public void onExit(Player player) {

    }

    // TODO: Refactor later
    @Override
    public String getInteractionPrompt() {
        String typeName = getNpcType().name();
        String pascalCaseName = typeName.substring(0, 1).toUpperCase() + typeName.substring(1).toLowerCase();
        if (typeName.equals("SIR_DEJANOVIC")) pascalCaseName = "SirDejanovic";
        return "Npc" + pascalCaseName;
    }

    public DialogueBehavior getDialogueBehavior() {
        return npcType.getDialogueBehavior();
    }

    public int getDialogueIndicator() {
        return dialogueIndicator;
    }

    public void progress() {
        this.progression++;
    }

    public void increaseDialogueIndicator() {
        if (dialogueIndicator + 1 > progression) return;
        this.dialogueIndicator++;
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
