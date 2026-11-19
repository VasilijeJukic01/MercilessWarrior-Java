package platformer.model.projectiles;

import platformer.debug.DebugSettings;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

public abstract class Projectile implements DamageSource {

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
            if (animIndex >= prType.getSprites()) {
                animIndex = 0;
            }
        }
    }

    public void updatePosition(Player player) {
        double X = 0, Y = 0;
        switch (direction) {
            case LEFT:
                X = 1; Y = 0;
                break;
            case RIGHT:
                X = -1; Y = 0;
                break;
            case UP:
                X = 0;  Y = -1;
                break;
            case DOWN:
                X = 0;  Y = 1;
                break;
            case DEGREE_60:
                X = 0.5; Y = 1;
                break;
            case DEGREE_45:
                X = 1; Y = 1;
                break;
            case DEGREE_30:
                X = 1.6; Y = 1;
                break;
            case N_DEGREE_45:
                X = -1; Y = 1;
                break;
            case N_DEGREE_30:
                X = -1.6; Y = 1;
                break;
            case N_DEGREE_60:
                X = -0.5; Y = 1;
                break;
            default: break;
        }

        if (prType == PRType.ARROW) updateArrowHitBox(X, Y);
        else updateFireballHitBox(X, Y);

        if (animate) updateAnimation();
    }

    public void updatePosition(Player player, ObjectManager objectManager, int[][] levelData) {
        // This method can be overridden by subclasses to implement specific behavior
    }

    private void updateArrowHitBox(double X, double Y) {
        hitBox.x += X * ARROW_SPEED;
        hitBox.y += Y * ARROW_SPEED;
    }

    private void updateFireballHitBox(double X, double Y) {
        hitBox.x += X * FIREBALL_SPEED;
        hitBox.y += Y * FIREBALL_SPEED;
    }

    public abstract void render(Graphics g, int xLevelOffset, int yLevelOffset);

    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    public abstract Shape getShapeBounds();

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

    public int getAnimIndex() {
        return animIndex;
    }
}
