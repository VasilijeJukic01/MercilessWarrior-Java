package platformer.model.spells;

import java.awt.*;

import static platformer.constants.Constants.LIGHTNING_HEIGHT;
import static platformer.constants.Constants.LIGHTNING_WIDTH;

public class Lightning extends Spell {

    public Lightning(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, LIGHTNING_WIDTH, LIGHTNING_HEIGHT);
        super.setAlive(false);
        initHitBox(width/1.5, height);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

}
