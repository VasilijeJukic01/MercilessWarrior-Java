package platformer.model.entities;

import platformer.animation.Anim;
import platformer.debug.Debug;
import platformer.debug.DebugSettings;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class Entity implements Debug {

    protected int xPos, yPos;
    protected int width, height;
    protected Rectangle2D.Double hitBox;

    protected Anim entityState = Anim.IDLE;
    protected int flipCoefficient = 0, flipSign = 1;
    protected boolean inAir;

    protected int maxHealth;
    protected double currentHealth;
    protected boolean attackCheck;
    protected Rectangle2D.Double attackBox;

    protected Direction pushDirection;
    protected double pushOffset;
    protected Direction pushOffsetDirection = Direction.UP;

    public Entity(int xPos, int yPos, int width, int height, int maxHealth) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public Entity(int xPos, int yPos, int width, int height) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }

    protected void initHitBox(double width, double height) {
        this.hitBox = new Rectangle2D.Double(xPos, yPos, width, height);
    }

    // Push
    protected void updatePushOffset() {
        double speed = 0.95;
        double limit = -30;

        if (pushOffsetDirection == Direction.UP) {
            pushOffset -= speed;
            if (pushOffset <= limit) pushOffsetDirection = Direction.DOWN;
        }
        else {
            pushOffset += speed;
            if (pushOffset >= 0) pushOffset = 0;
        }
    }

    protected void pushBack(Direction pushDirection, int[][] lvlData, double speed, double enemySpeed) {
        double xSpeed;
        if (pushDirection == Direction.LEFT) xSpeed = -enemySpeed;
        else xSpeed = enemySpeed;

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * speed, hitBox.y, hitBox.width, hitBox.height, lvlData)) {
            hitBox.x += xSpeed * speed;
        }
    }

    // Render Core
    public void renderAttackBox(Graphics g, int xOffset, int yOffset) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(Color.RED);
        g.drawRect((int)attackBox.x-xOffset, (int)attackBox.y-yOffset, (int)attackBox.width, (int)attackBox.height);
    }

    protected void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    public Rectangle2D.Double getHitBox() {
        return hitBox;
    }

    public int getFlipSign() {
        return flipSign;
    }

    public int getFlipCoefficient() {
        return flipCoefficient;
    }

    public void setPushDirection(Direction pushDirection) {
        this.pushDirection = pushDirection;
    }

}
