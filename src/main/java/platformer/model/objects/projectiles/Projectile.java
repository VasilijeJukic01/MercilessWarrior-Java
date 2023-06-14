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
        double X = 0, Y = 0;
        switch (direction) {
            case LEFT: X = 1; Y = 0; break;
            case RIGHT: X = -1; Y = 0; break;
            case UP: X = 0;  Y = -1; break;
            case DOWN: X = 0;  Y = 1; break;
            case DEGREE_60: X = 0.5; Y = 1; break;
            case DEGREE_45: X = 1; Y = 1; break;
            case DEGREE_30: X = 1.6; Y = 1; break;
            case N_DEGREE_45: X = -1; Y = 1; break;
            case N_DEGREE_30: X = -1.6; Y = 1; break;
            case N_DEGREE_60: X = -0.5; Y = 1; break;
        }

        if (prType == PRType.ARROW) {
            hitBox.x += X * PRSet.ARROW_SPEED.getValue();
            hitBox.y += Y * PRSet.ARROW_SPEED.getValue();
        }
        else if (prType == PRType.LIGHTNING_BALL) {
            int speed = (int)PRSet.LB_SPEED_MEDIUM.getValue();
            if (direction == Direction.LEFT || direction == Direction.RIGHT)
                speed = (int)PRSet.LB_SPEED_FAST.getValue();
            hitBox.x += X * speed;
            hitBox.y += Y * speed;
        }
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
