package platformer.model.spells.types;

import platformer.animation.SpriteManager;
import platformer.model.entities.player.Player;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Flash extends Spell {

    private boolean hasHitPlayer = false;

    public Flash(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos+(TILES_SIZE/3), yPos, FLASH_WIDTH, FLASH_HEIGHT);
        super.setActive(false);
        initHitBox(width/5.0, height);
    }

    @Override
    public void update(Player player) {
        if (!active) return;
        if (!hasHitPlayer && animIndex > 11 && animIndex < 16) {
            if (hitBox.intersects(player.getHitBox())) {
                player.changeHealth(-10, this);
                hasHitPlayer = true;
            }
        }
        updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (active) {
            int x = (int) getHitBox().x - xLevelOffset - FLASH_OFFSET_X;
            int y = (int) getHitBox().y - yLevelOffset + 1;
            g.drawImage(getAnimations()[getAnimIndex()], x, y, getWidth(), getHeight(), null);
        }
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return SpriteManager.getInstance().getFlashAnimations();
    }

    @Override
    public void reset() {
        animTick = animIndex = 0;
        hasHitPlayer = false;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
