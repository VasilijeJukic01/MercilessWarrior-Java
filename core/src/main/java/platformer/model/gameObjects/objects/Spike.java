package platformer.model.gameObjects.objects;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.Interactable;
import platformer.model.gameObjects.ObjType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Spike extends GameObject implements Interactable {

    private final Direction direction;

    public Spike(ObjType objType, int xPos, int yPos) {
        super(objType, xPos, yPos);

        switch (objType) {
            case SPIKE_DOWN: this.direction = Direction.DOWN; break;
            case SPIKE_LEFT: this.direction = Direction.LEFT; break;
            case SPIKE_RIGHT: this.direction = Direction.RIGHT; break;
            default: this.direction = Direction.UP; break;
        }
        calculateHitboxAndOffsets();
    }

    private void calculateHitboxAndOffsets() {
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            initHitBox(SPIKE_HB_HEI, SPIKE_HB_WID);
            xOffset = (direction == Direction.LEFT) ? (SPIKE_WID - SPIKE_HB_HEI + (int)(16 * SCALE)) : - (int)(16 * SCALE);
            yOffset = (SPIKE_HEI - SPIKE_HB_WID) / 2;
        }
        else {
            initHitBox(SPIKE_HB_WID, SPIKE_HB_HEI);
            xOffset = (SPIKE_WID - SPIKE_HB_WID) / 2;
            yOffset = (direction == Direction.UP) ? (SPIKE_HEI - SPIKE_HB_HEI + (int)(10 * SCALE)) : - (int)(10 * SCALE);
        }
        hitBox.x = this.xPos + xOffset;
        hitBox.y = this.yPos + yOffset;
    }

    @Override
    public void update() {

    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int index = (direction == Direction.UP) ? 5 : 0;

        int x = xPos, y = yPos;

        if (direction == Direction.LEFT) x += SPIKE_OFFSET_X;
        else if (direction == Direction.RIGHT) x -= SPIKE_OFFSET_X;
        else if (direction == Direction.DOWN) y -= SPIKE_OFFSET_Y;
        else if (direction == Direction.UP) y += SPIKE_OFFSET_Y;

        g.drawImage(animations[index], x - xLevelOffset, y - yLevelOffset, SPIKE_WID, SPIKE_HEI, null);
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
        player.kill();
    }

    @Override
    public void onIntersect(Player player) {
        player.kill();
    }

    @Override
    public void onExit(Player player) {

    }

    @Override
    public String getInteractionPrompt() {
        return null;
    }
}
