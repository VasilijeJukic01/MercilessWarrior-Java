package platformer.model.objects.projectiles;

import platformer.debug.DebugSettings;
import platformer.model.ModelUtils;
import platformer.model.entities.Direction;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class Projectile {

    protected final PRType prType;
    protected Rectangle2D.Double hitBox;
    protected final Direction direction;
    protected boolean alive = true;
    protected boolean animate;
    private final int animSpeed = 20;
    protected int animTick, animIndex;

    public Projectile(PRType prType, Direction direction) {
        this.direction = direction;
        this.prType = prType;
    }

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= ModelUtils.getInstance().getProjectileSprite(prType)) {
                animIndex = 0;
            }
        }
    }

    public void updatePosition() {
        int k = (direction == Direction.LEFT) ? 1 : -1;
        if (prType == PRType.ARROW) hitBox.x += k * PRSet.ARROW_SPEED.getValue();
        else if (prType == PRType.LIGHTNING_BALL) hitBox.x += k * PRSet.LB_SPEED.getValue();
        if (animate) updateAnimation();
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

    public PRType getPrType() {
        return prType;
    }

    public int getAnimIndex() {
        return animIndex;
    }
}
