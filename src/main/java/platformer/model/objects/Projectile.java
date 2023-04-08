package platformer.model.objects;

import platformer.debug.DebugSettings;
import platformer.model.Tiles;
import platformer.model.entities.Direction;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public class Projectile {

    private final Rectangle2D.Double hitBox;
    private final Direction direction;
    private boolean alive = true;

    public Projectile(int xPos, int yPos, Direction direction) {
        int xOffset = (int)(-20 * Tiles.SCALE.getValue());
        int yOffset = (int)(20 * Tiles.SCALE.getValue());
        if (direction == Direction.LEFT) xOffset =  (int)(10 * Tiles.SCALE.getValue());
        this.hitBox = new Rectangle2D.Double(xPos+xOffset, yPos+yOffset, PRSet.ARROW_WID.getValue(), PRSet.ARROW_HEI.getValue());
        this.direction = direction;
    }

    public void updatePosition() {
        int k = (direction == Direction.LEFT) ? 1 : -1;
        hitBox.x += k * PRSet.ARROW_SPEED.getValue();
    }

    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    public Rectangle2D.Double getHitBox() {
        return hitBox;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public Direction getDirection() {
        return direction;
    }

}
