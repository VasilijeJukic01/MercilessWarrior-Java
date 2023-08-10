package platformer.model.gameObjects.projectiles;

import platformer.animation.graphics.GraphicsAnimation;
import platformer.animation.graphics.WaveAnim;
import platformer.debug.DebugSettings;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

public abstract class Projectile {

    protected GraphicsAnimation waveMovement;

    protected final PRType prType;
    protected Rectangle2D.Double hitBox;
    protected final Direction direction;
    protected boolean alive = true;
    protected boolean animate;
    private final int animSpeed = 20;
    protected int animTick, animIndex;

    // Special
    private boolean following = true;
    private double trackX, trackY;

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

        if (prType == PRType.ARROW) {
            updateArrowHitBox(X, Y);
        }
        else if (prType == PRType.LIGHTNING_BALL) {
            if (direction == Direction.LEFT || direction == Direction.RIGHT)
                updateWaveProjectile();
            else if (direction == Direction.TRACK)
                updateTrackingProjectile(player);
            else updateOscillationProjectile(X, Y);
        }

        if (animate) updateAnimation();
    }

    private void updateArrowHitBox(double X, double Y) {
        hitBox.x += X * ARROW_SPEED;
        hitBox.y += Y * ARROW_SPEED;
    }

    private void updateWaveProjectile() {
        ((WaveAnim)waveMovement).setDirection(direction);
        Point p = waveMovement.calculatePoint();
        hitBox.x = p.x;
        hitBox.y = p.y;
    }

    private void updateTrackingProjectile(Player player) {
        double dx = player.getHitBox().x + player.getHitBox().width - hitBox.x;
        double dy = player.getHitBox().y + player.getHitBox().height - hitBox.y;
        double d = Math.sqrt(dx * dx + dy * dy);

        if (d > TRACKING_PROJECTILE_DISTANCE && following) {
            trackX = dx / d;
            trackY = dy / d;
        }
        else following = false;

        hitBox.x += trackX * LB_SPEED_FAST;
        hitBox.y += trackY * LB_SPEED_FAST;
    }

    private void updateOscillationProjectile(double X, double Y) {
        // Oscillation parameters
        double oscillationAmplitude = 1.2; // Adjust the amplitude of the oscillation
        double oscillationFrequency = 0.5; // Adjust the frequency of the oscillation

        double time = System.currentTimeMillis() / 1000.0;
        double oscillationOffset = oscillationAmplitude * Math.sin(2 * Math.PI * oscillationFrequency * time);

        X += oscillationOffset;
        hitBox.x += X * LB_SPEED_SLOW;
        hitBox.y += Y * LB_SPEED_SLOW;
    }

    public abstract void render(Graphics g, int xLevelOffset, int yLevelOffset, Object animations);

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

    public int getAnimIndex() {
        return animIndex;
    }
}
