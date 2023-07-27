package platformer.model.spells;

import java.awt.*;

import static platformer.constants.Constants.SCALE;

public class Flames extends Spell {

    private final int xOffset;
    private final int yOffset;

    public Flames(SpellType spellType, int xPos, int yPos, int width, int height) {
        super(spellType, xPos, yPos, width, height);
        initHitBox(width, height);
        this.xOffset = (int)(55*SCALE);
        this.yOffset = (int)(1*SCALE);
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

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }
}
