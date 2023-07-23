package platformer.model.spells;

import java.awt.*;

public class Lightning extends Spell {

    public Lightning(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, SpellSize.LIGHTNING_WIDTH.getValue(), SpellSize.LIGHTNING_HEIGHT.getValue());
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
