package platformer.model.objects;


import platformer.debug.Debug;
import platformer.debug.DebugSettings;
import platformer.model.ModelUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

public abstract class GameObject implements Debug {

    protected Obj obj;
    protected int xPos, yPos;
    protected Rectangle2D.Double hitBox;
    protected boolean animate;
    protected boolean alive = true;
    private final int animSpeed = 20;
    protected int animTick, animIndex;
    protected int xOffset, yOffset;
    protected boolean isOnGround;

    public GameObject(Obj obj, int xPos, int yPos) {
        this.obj = obj;
        this.xPos = xPos;
        this.yPos = yPos;
    }

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= ModelUtils.getInstance().getObjectSprite(obj)) {
                animIndex = 0;
                if (obj == Obj.BARREL || obj == Obj.BOX) {
                    animate = false;
                    alive = false;
                }
                else if (obj == Obj.ARROW_TRAP_LEFT || obj == Obj.ARROW_TRAP_RIGHT) {
                    animate = false;
                }
                else if (obj == Obj.BLOCKER) {
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
        animate = obj != Obj.BARREL && obj != Obj.BOX && obj != Obj.ARROW_TRAP_LEFT &&  obj != Obj.ARROW_TRAP_RIGHT;
    }

    // Getters & Setters
    public Obj getObjType() {
        return obj;
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
