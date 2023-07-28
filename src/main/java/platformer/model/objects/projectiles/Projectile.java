package platformer.model.objects.projectiles;

import platformer.animation.graphics.GraphicsAnimation;
import platformer.animation.graphics.WaveAnim;
import platformer.debug.DebugSettings;
import platformer.model.ModelUtils;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

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
            if (animIndex >= ModelUtils.getInstance().getProjectileSprite(prType)) {
                animIndex = 0;
            }
        }
    }

    private List<Point> points = new ArrayList<>();

    public void updatePosition(Player player) {
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
            default: break;
        }

        if (prType == PRType.ARROW) {
            hitBox.x += X * ARROW_SPEED;
            hitBox.y += Y * ARROW_SPEED;
        }
        else if (prType == PRType.LIGHTNING_BALL) {
            double speed = LB_SPEED_SLOW;
            if (direction == Direction.LEFT || direction == Direction.RIGHT) {
                ((WaveAnim)waveMovement).setDirection(direction);
                Point p = waveMovement.calculatePoint();
                hitBox.x = p.x;
                hitBox.y = p.y;
            }
            else if (direction == Direction.TRACK) {
                speed = LB_SPEED_FAST;
                double dx = player.getHitBox().x - hitBox.x;
                double dy = player.getHitBox().y - hitBox.y;
                double d = Math.sqrt(dx * dx + dy * dy);
                if (d > (50*SCALE) && following) {
                    trackX = dx / d;
                    trackY = dy / d;
                }
                else following = false;
                hitBox.x += trackX * speed;
                hitBox.y += trackY * speed;
            }
            else {
                // Oscillation parameters
                double oscillationAmplitude = 1.2; // Adjust the amplitude of the oscillation
                double oscillationFrequency = 0.5; // Adjust the frequency of the oscillation

                double time = System.currentTimeMillis() / 1000.0;
                double oscillationOffset = oscillationAmplitude * Math.sin(2 * Math.PI * oscillationFrequency * time);

                X += oscillationOffset;
                hitBox.x += X * speed;
                hitBox.y += Y * speed;
                Point p = new Point((int)hitBox.x, (int)hitBox.y);
                try {
                    if (!points.contains(p)) points.add(p);
                }
                catch (Exception ignored) {}
            }

        }
        if (animate) updateAnimation();
    }

    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
        g.setColor(Color.RED);
        try {
            for (Point point : points) {
                g.drawRect(point.x, point.y, 2, 2);
            }
        }
        catch (Exception ignored) {}

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
