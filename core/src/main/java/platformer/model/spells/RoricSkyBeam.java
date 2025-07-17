package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;

import static platformer.constants.Constants.*;

public class RoricSkyBeam extends Spell {

    private final boolean isTargeted;

    public RoricSkyBeam(SpellType spellType, int xPos, int yPos, boolean isTargeted) {
        super(spellType, xPos, yPos, RORIC_BEAM_HEI, RORIC_BEAM_WID);
        this.isTargeted = isTargeted;
        super.animSpeed = 25;
        int offset = isTargeted ? (int) (25 * SCALE) : 0;
        initHitBox(width / 10.0 + offset, height);
    }

    public void update(Player player) {
        if (!active) return;
        updateAnimation();
        if (animIndex >= 3 && animIndex <= 5) {
            if (hitBox.intersects(player.getHitBox())) {
                // player.changeHealth(-15, this);
            }
        }
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public boolean isTargeted() {
        return isTargeted;
    }
}
