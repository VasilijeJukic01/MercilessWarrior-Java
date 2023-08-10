package platformer.model.spells;

import java.awt.*;

import static platformer.constants.Constants.*;

public class Flash extends Spell {

    public Flash(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos+(TILES_SIZE/3), yPos, FLASH_WIDTH, FLASH_HEIGHT);
        super.setActive(false);
        initHitBox(width/5.0, height);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
