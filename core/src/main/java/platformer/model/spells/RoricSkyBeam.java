package platformer.model.spells;

import platformer.model.entities.player.Player;

import java.awt.*;

import static platformer.constants.Constants.RORIC_BEAM_HEI;
import static platformer.constants.Constants.RORIC_BEAM_WID;

public class RoricSkyBeam extends Spell {

    public RoricSkyBeam(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, RORIC_BEAM_HEI, RORIC_BEAM_WID);
        super.animSpeed = 25;
        initHitBox(width / 10.0, height);
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
}
