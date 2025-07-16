package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

public class ArrowRain extends Spell {

    // TODO: Use this later
    private final int damageCooldown = 20;
    private int currentDamageCooldown = 0;

    public ArrowRain(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, RORIC_RAIN_WID, RORIC_RAIN_HEI);
        initHitBox();
    }

    private void initHitBox() {
        super.hitBox = new Rectangle2D.Double(xPos + RORIC_BEAM_OFFSET_X, yPos + RORIC_BEAM_OFFSET_Y, RORIC_RAIN_HB_WID, RORIC_RAIN_HEI);
    }

    public void update(Player player) {
        if (!active) return;

        if (currentDamageCooldown > 0) {
            currentDamageCooldown--;
        }

        if (currentDamageCooldown == 0 && hitBox.intersects(player.getHitBox())) {
            // player.changeHealth(-10, this);
            currentDamageCooldown = damageCooldown;
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }
}
