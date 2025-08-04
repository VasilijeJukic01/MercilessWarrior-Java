package platformer.model.projectiles.types;

import platformer.model.entities.Direction;
import platformer.model.projectiles.PRType;
import platformer.model.projectiles.Projectile;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Fireball extends Projectile {

    public Fireball(int xPos, int yPos, Direction direction) {
        super(PRType.FIREBALL, direction);
        super.animate = true;
        initHitBox(xPos, yPos);
    }

    private void initHitBox(int xPos, int yPos) {
        int xOffset = (direction == Direction.RIGHT) ? -FB_OFFSET_X : FB_OFFSET_Y;
        super.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+FB_OFFSET_Y, FB_WID/3.5, FB_HEI/3.5);
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations) {
        if (!(animations instanceof BufferedImage[])) return;
        BufferedImage[] animArray = (BufferedImage[]) animations;
        int fS = 1, fC = 0;
        if (direction == Direction.RIGHT) {
            fS = -1;
            fC = FB_WID;
        }
        int x = (int)(hitBox.x - xLevelOffset + fC - 15 * SCALE);
        int y = (int)(hitBox.y - yLevelOffset - 18 * SCALE);
        g.drawImage(animArray[animIndex], x, y, fS * FB_WID, FB_HEI, null);
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.BLUE);
    }

    @Override
    public Shape getShapeBounds() {
        return super.hitBox;
    }

}
