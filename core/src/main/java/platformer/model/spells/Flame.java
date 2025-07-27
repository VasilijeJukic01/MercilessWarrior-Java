package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;

public class Flame extends Spell {

    public Flame(SpellType spellType, int xPos, int yPos, int width, int height) {
        super(spellType, xPos, yPos, width, height);
        initHitBox(width, height);
    }

    @Override
    public void update(Player player) {

    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
