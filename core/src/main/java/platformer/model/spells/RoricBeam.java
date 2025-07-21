package platformer.model.spells;

import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;

import java.awt.*;
import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.*;

public class RoricBeam extends Spell {

    private final Direction direction;
    private boolean hasHitPlayer = false;

    public RoricBeam(SpellType spellType, int xPos, int yPos, Direction direction) {
        super(spellType, xPos, yPos, RORIC_BEAM_WID, RORIC_BEAM_HEI);
        super.animSpeed = 16;
        this.direction = direction;
        initHitBox();
    }

    private void initHitBox() {
        double beamX;
        if (direction == Direction.LEFT) beamX = xPos + RORIC_BEAM_OFFSET_X_LEFT - RORIC_BEAM_WID;
        else beamX = xPos + RORIC_BEAM_OFFSET_X_RIGHT;
        super.hitBox = new Rectangle2D.Double(beamX, yPos + RORIC_BEAM_OFFSET_Y, RORIC_BEAM_WID, RORIC_BEAM_HB_HEI);
    }

    @Override
    public void update(Player player) {
        if (!active) return;
        if (!hasHitPlayer && animIndex >= 0 && animIndex < 2) {
            if (hitBox.intersects(player.getHitBox())) {
                player.changeHealth(-15, this);
                hasHitPlayer = true;
            }
        }
        updateAnimation();
    }

    @Override
    public Direction getKnockbackDirection() {
        return this.direction;
    }

    @Override
    public void reset() {
        super.reset();
        hasHitPlayer = false;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {

    }

    public Direction getDirection() {
        return direction;
    }
}
