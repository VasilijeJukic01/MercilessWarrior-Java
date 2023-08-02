package platformer.model.spells;

import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.debug.Debug;
import platformer.debug.DebugSettings;
import platformer.model.ModelUtils;

import java.awt.*;
import java.awt.geom.Rectangle2D;

@SuppressWarnings("FieldCanBeLocal")
public abstract class Spell implements Debug {

    protected final SpellType spellType;
    protected int xPos, yPos;
    protected int width, height;
    protected Rectangle2D.Double hitBox;
    private final int animSpeed = 20;
    protected int animTick, animIndex;
    protected boolean active = true;

    public Spell(SpellType spellType, int xPos, int yPos, int width, int height) {
        this.spellType = spellType;
        this.xPos = xPos;
        this.yPos = yPos;
        this.width = width;
        this.height = height;
    }

    protected void initHitBox(double width, double height) {
        this.hitBox = new Rectangle2D.Double(xPos, yPos, width, height);
    }

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= ModelUtils.getInstance().getSpellSprite(spellType)) {
                animIndex = 0;
                finishAnimation();
            }
            else if (spellType == SpellType.FLASH && active && animIndex == 9) Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_3);
            else if (spellType == SpellType.LIGHTNING && active && animIndex == 1) Audio.getInstance().getAudioPlayer().playSound(Sound.LIGHTNING_1);
        }
    }

    private void finishAnimation() {
        if (spellType == SpellType.FLAME_1 || spellType == SpellType.LIGHTNING || spellType == SpellType.FLASH) {
            active = false;
        }
    }

    protected void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    protected void renderHitBox(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        if (!DebugSettings.getInstance().isDebugMode()) return;
        g.setColor(color);
        g.drawRect((int)hitBox.x-xLevelOffset, (int)hitBox.y-yLevelOffset, (int)hitBox.width, (int)hitBox.height);
    }

    protected void reset() {
        active = false;
        animTick = animIndex = 0;
    }

    public Rectangle2D.Double getHitBox() {
        return hitBox;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getAnimIndex() {
        return animIndex;
    }

    public SpellType getSpellType() {
        return spellType;
    }

    public void setAnimIndex(int animIndex) {
        this.animIndex = animIndex;
    }
}
