package platformer.model.gameObjects;

import platformer.debug.Debug;
import platformer.debug.DebugSettings;
import platformer.model.AdvancedRenderable;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Abstract class representing a game object in the game.
 */
public abstract class GameObject implements AdvancedRenderable<Graphics>, Debug<Graphics> {

    protected ObjType objType;
    protected int xPos, yPos;
    protected int xOffset, yOffset;
    protected Rectangle2D.Double hitBox;

    protected double rotation = 0.0;
    protected double scaleX = 1.0;
    protected double scaleY = 1.0;

    protected boolean animate;
    protected boolean alive = true;
    protected int animSpeed = 20;
    protected int animTick, animIndex;
    protected boolean isOnGround;

    public GameObject(ObjType objType, int xPos, int yPos) {
        this.objType = objType;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    protected void initHitBox(double width, double height) {
        this.hitBox = new Rectangle2D.Double(xPos, yPos, width, height);
    }

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= objType.getSprites()) {
                finishAnimation();
            }
        }
    }

    public abstract void update();

    @Override
    public abstract void render(Graphics g, int xLevelOffset, int yLevelOffset, BufferedImage[] animations);

    private void finishAnimation() {
        animIndex = 0;
        if (objType == ObjType.BARREL || objType == ObjType.BOX || objType == ObjType.BRICK) {
            animate = alive = false;
        }
        else if (objType == ObjType.ARROW_TRAP_LEFT || objType == ObjType.ARROW_TRAP_RIGHT) {
            animate = false;
        }
        else if (objType == ObjType.BLOCKER) {
            if (animate) animIndex = 3;
            else animIndex = 1;
        }
    }

    @Override
    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    public void reset() {
        animIndex = animTick = 0;
        alive = true;
        isOnGround = false;
        animate = shouldAnimate();
    }

    private boolean shouldAnimate() {
        ObjType[] nonAnimatingTypes = {ObjType.BARREL, ObjType.BOX, ObjType.ARROW_TRAP_LEFT, ObjType.ARROW_TRAP_RIGHT, ObjType.BRICK};
        for (ObjType type : nonAnimatingTypes) {
            if (objType == type) return false;
        }
        return true;
    }

    // Getters & Setters
    public ObjType getObjType() {
        return objType;
    }

    public Rectangle2D.Double getHitBox() {
        return hitBox;
    }

    public boolean isAlive() {
        return alive;
    }

    public boolean isAnimate() {
        return animate;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public int getAnimTick() {
        return animTick;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setAnimate(boolean animate) {
        this.animate = animate;
    }

    public void setOnGround(boolean onGround) {
        isOnGround = onGround;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    public double getScaleX() {
        return scaleX;
    }

    public void setScaleX(double scaleX) {
        this.scaleX = scaleX;
    }

    public double getScaleY() {
        return scaleY;
    }

    public void setScaleY(double scaleY) {
        this.scaleY = scaleY;
    }
}
