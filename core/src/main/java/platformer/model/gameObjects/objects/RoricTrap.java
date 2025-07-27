package platformer.model.gameObjects.objects;

import platformer.model.entities.Direction;
import platformer.model.gameObjects.GameObject;
import platformer.model.gameObjects.ObjType;
import java.awt.*;
import java.awt.image.BufferedImage;
import static platformer.constants.Constants.*;

public class RoricTrap extends GameObject {

    private final Direction direction;

    private int health = 10;
    public static final int TRAP_DAMAGE = 10;

    public RoricTrap(int xPos, int yPos, Direction direction) {
        super(ObjType.RORIC_TRAP, xPos, yPos);
        this.direction = direction;
        initHitBox(RORIC_TRAP_HB_WID, RORIC_TRAP_HB_HEI);
        super.yOffset = (int)(35 * SCALE);
        hitBox.y += yOffset;
        super.animate = true;
    }

    @Override
    public void update() {
        if (animate) {
            updateAnimation();
            if (animIndex >= objType.getSprites() - 1) {
                animate = false;
            }
        }
    }

    public void hit(int damage) {
        this.health -= damage;
        if (health <= 0) {
            this.alive = false;
        }
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations) {
        int x = (int)hitBox.x - xOffset - xLevelOffset;
        int y = (int)hitBox.y - yOffset - yLevelOffset;
        int xOffset = (int)(-70 * SCALE);

        int width = RORIC_TRAP_WID;
        if (direction == Direction.LEFT) {
            x += width;
            xOffset = (int)(-58 * SCALE);
            width *= -1;
        }

        g.drawImage(animations[animIndex], x + xOffset, y, width, RORIC_TRAP_HEI, null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.PINK);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}