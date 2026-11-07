package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;
import platformer.physics.CollisionDetector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Lava extends GameObject implements Interactable {

    private boolean solidLeft = false;
    private boolean solidRight = false;

    public Lava(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        super.animSpeed = 14;
        super.animIndex = new Random().nextInt(15);
        initHitBox(LAVA_HB_WID, LAVA_HB_HEI);
        super.xOffset = LAVA_OFFSET_X;
        super.yOffset = LAVA_OFFSET_Y;
        hitBox.y += yOffset;
    }

    @Override
    public void update() {
        updateAnimation();
    }

    @Override
    public void update(int[][] levelData) {
        update();
        int tileX = xPos / TILES_SIZE;
        int tileY = yPos / TILES_SIZE;
        if (tileY >= 0 && tileY < levelData[0].length) {
            solidLeft = (tileX - 1 >= 0) && CollisionDetector.isTileSolid(tileX - 1, tileY, levelData);
            solidRight = (tileX + 1 < levelData.length) && CollisionDetector.isTileSolid(tileX + 1, tileY, levelData);
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int) hitBox.x - xOffset - xLevelOffset;
        int y = (int) hitBox.y - yOffset - yLevelOffset + (int) (3 * SCALE);

        int drawX = solidLeft ? x - (int) (4 * SCALE) : x;
        int drawW = LAVA_WID + (solidLeft ? (int) (4 * SCALE) : 0) + (solidRight ? (int) (4 * SCALE) : 0);

        g.drawImage(animations[animIndex], drawX, y, drawW, LAVA_HEI, null);

        Graphics2D g2d = (Graphics2D) g.create();

        int fadeHeight = (int) (LAVA_HEI * 0.45);
        int fadeY = y + LAVA_HEI - fadeHeight;
        GradientPaint bottomFade = new GradientPaint(0, fadeY, new Color(0, 0, 0, 0), 0, y + LAVA_HEI, Color.BLACK);
        g2d.setPaint(bottomFade);
        g2d.fillRect(drawX, fadeY, drawW, fadeHeight);

        int sideFadeWidth = (int) (10 * SCALE);
        int surfaceOffset = (int) (10 * SCALE);
        int surfaceY = y + surfaceOffset;
        int surfaceHei = LAVA_HEI - surfaceOffset;

        if (solidLeft) {
            GradientPaint leftFade = new GradientPaint(drawX, 0, Color.BLACK, drawX + sideFadeWidth, 0, new Color(0, 0, 0, 0));
            g2d.setPaint(leftFade);
            g2d.fillRect(drawX, surfaceY, sideFadeWidth, surfaceHei);
        }
        if (solidRight) {
            int rightEdge = drawX + drawW;
            GradientPaint rightFade = new GradientPaint(rightEdge - sideFadeWidth, 0, new Color(0, 0, 0, 0), rightEdge, 0, Color.BLACK);
            g2d.setPaint(rightFade);
            g2d.fillRect(rightEdge - sideFadeWidth, surfaceY, sideFadeWidth, surfaceHei);
        }

        g2d.dispose();
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
        player.addAction(PlayerAction.LAVA);
    }

    @Override
    public void onIntersect(Player player) {
        if (!player.checkAction(PlayerAction.LAVA)) {
            player.addAction(PlayerAction.LAVA);
        }
    }

    @Override
    public void onExit(Player player) {
        player.removeAction(PlayerAction.LAVA);
    }

    @Override
    public String getInteractionPrompt() {
        return null; // No prompt for lava
    }
}

