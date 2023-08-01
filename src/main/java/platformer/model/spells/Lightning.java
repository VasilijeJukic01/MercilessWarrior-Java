package platformer.model.spells;

import java.awt.*;

import static platformer.constants.Constants.LIGHTNING_HEIGHT;
import static platformer.constants.Constants.LIGHTNING_WIDTH;

public class Lightning extends Spell {

    public Lightning(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, LIGHTNING_WIDTH, LIGHTNING_HEIGHT);
        super.setActive(false);
        initHitBox(width/1.5, height);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
