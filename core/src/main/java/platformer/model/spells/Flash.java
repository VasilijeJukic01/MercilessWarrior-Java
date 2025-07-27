package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;

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
