package platformer.model.projectiles;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class RoricArrow extends Projectile {

    private final double speed;

    public RoricArrow(int xPos, int yPos, Direction direction, double speedMultiplier) {
        super(PRType.RORIC_ARROW, direction);
        this.speed = RORIC_ARROW_SPEED * speedMultiplier;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? RORIC_ARROW_OFFSET_X_RIGHT : RORIC_ARROW_OFFSET_X_LEFT;
        super.hitBox = new Rectangle2D.Double(xPos + xOffset, yPos + RORIC_ARROW_OFFSET_Y, ARROW_WID, ARROW_HEI);
    }

    @Override
    public void updatePosition(Player player) {
        double xDirection = (direction == Direction.LEFT) ? -1 : 1;
        hitBox.x += xDirection * speed;
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage)) return;

        int flipSign = 1;
        int flipWidth = 0;
        if (direction == Direction.LEFT) {
            flipSign = -1;
            flipWidth = ARROW_WID;
        }

        int x = (int) hitBox.x - xLevelOffset + flipWidth;
        int y = (int) hitBox.y - yLevelOffset;

        g.drawImage((BufferedImage) animations, x, y, ARROW_WID * flipSign, ARROW_HEI, null);
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.MAGENTA);
    }

    @Override
    public Shape getShapeBounds() {
        return super.hitBox;
    }

}