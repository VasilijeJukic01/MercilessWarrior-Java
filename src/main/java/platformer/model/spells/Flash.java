package platformer.model.spells;


import platformer.model.Tiles;

import java.awt.*;

public class Flash extends Spell {

    public Flash(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos+(int)(Tiles.TILES_SIZE.getValue()/3), yPos, SpellSize.FLASH_WIDTH.getValue(), SpellSize.FLASH_HEIGHT.getValue());
        super.setAlive(false);
        initHitBox(width/5.0, height);
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