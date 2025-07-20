package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;

import static platformer.constants.Constants.LIGHTNING_HEIGHT;
import static platformer.constants.Constants.LIGHTNING_WIDTH;

public class Lightning extends Spell {

    private boolean hasHitPlayer = false;

    public Lightning(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, LIGHTNING_WIDTH, LIGHTNING_HEIGHT);
        super.setActive(false);
        initHitBox(width/1.5, height);
    }

    @Override
    public void update(Player player) {
        if (!active) return;
        if (!hasHitPlayer && animIndex > 0 && animIndex < 5) {
            if (hitBox.intersects(player.getHitBox())) {
                player.changeHealth(-20, this);
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
