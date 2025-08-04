package platformer.model.spells.types;

import platformer.model.entities.player.Player;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Flame extends Spell {

    public Flame(SpellType spellType, int xPos, int yPos, int width, int height) {
        super(spellType, xPos, yPos, width, height);
        initHitBox(width, height);
    }

    @Override
    public void update(Player player) {
        if (player.getFlipSign() != 1) {
            getHitBox().x = player.getHitBox().x - FLAME_OFFSET_X;
        }
        else getHitBox().x = player.getHitBox().x + player.getHitBox().width + FLAME_OFFSET_X - FLAME_WID;
        getHitBox().y = player.getHitBox().y - FLAME_OFFSET_Y;
        setActive(player.getSpellState() != 0);
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return null;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
