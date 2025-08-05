package platformer.model.gameObjects.objects;

import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class JumpPad extends GameObject implements Interactable {

    private boolean used = false;
    private final double launchSpeed = -6.0 * SCALE;

    public JumpPad(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);
        initHitBox(JUMP_PAD_HB_WID, JUMP_PAD_HB_HEI);
        super.xOffset = JUMP_PAD_OFFSET_X;
        super.yOffset = JUMP_PAD_OFFSET_Y;
        hitBox.x += xOffset;
        hitBox.y += yOffset;
    }

    @Override
    public void update() {
        if (used) {
            animTick++;
            if (animTick >= animSpeed) {
                animTick = 0;
                animIndex++;
                if (animIndex >= 20) {
                    animIndex = 0;
                    used = false;
                }
            }
        }
        else {
            animTick++;
            if (animTick >= animSpeed) {
                animTick = 0;
                animIndex++;
                if (animIndex >= 10) animIndex = 0;
            }
        }
    }

    public void launchPlayer(Player player) {
        used = true;
        animIndex = 10;
        player.launch(launchSpeed);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset - (int) (15 * SCALE);
        int y = (int)hitBox.y - yOffset - yLevelOffset - (int) (2 * SCALE);
        g.drawImage(animations[animIndex], x, y, JUMP_PAD_WID, JUMP_PAD_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
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
        launchPlayer(player);
    }

    @Override
    public void onIntersect(Player player) {

    }

    @Override
    public void onExit(Player player) {

    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }

    public boolean isUsed() {
        return used;
    }
}
