package platformer.model.objects;


import platformer.debug.Debug;
import platformer.debug.DebugSettings;
import platformer.model.ModelUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class GameObject implements Debug {

    protected ObjType objType;
    protected int xPos, yPos;
    protected Rectangle2D.Double hitBox;
    protected boolean animate;
    protected boolean alive = true;
    private final int animSpeed = 20;
    protected int animTick, animIndex;
    protected int xOffset, yOffset;
    protected boolean isOnGround;

    public GameObject(ObjType objType, int xPos, int yPos) {
        this.objType = objType;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= ModelUtils.getInstance().getObjectSprite(objType)) {
                animIndex = 0;
                if (objType == ObjType.BARREL || objType == ObjType.BOX) {
                    animate = false;
                    alive = false;
                }
                else if (objType == ObjType.ARROW_TRAP_LEFT || objType == ObjType.ARROW_TRAP_RIGHT) {
                    animate = false;
                }
                else if (objType == ObjType.BLOCKER) {
                    if (animate) animIndex = 3;
                    else animIndex = 1;
                }
            }
        }
    }

    protected void initHitBox(double width, double height) {
        this.hitBox = new Rectangle2D.Double(xPos, yPos, width, height);
    }

    public void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    public void reset() {
        animIndex = animTick = 0;
        alive = true;
        isOnGround = false;
        animate = objType != ObjType.BARREL && objType != ObjType.BOX && objType != ObjType.ARROW_TRAP_LEFT &&  objType != ObjType.ARROW_TRAP_RIGHT;
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

}
