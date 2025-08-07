package platformer.model.spells.types;

import platformer.animation.SpriteManager;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

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
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (isActive()) {
            int flipSign = (direction == Direction.LEFT) ? -1 : 1;
            int x = (int) hitBox.x - xLevelOffset;
            int y = (int) hitBox.y - yLevelOffset - (int) (64 * SCALE);

            if (flipSign == -1) x += RORIC_BEAM_WID;

            g.drawImage(getAnimations()[getAnimIndex()], x, y, flipSign * RORIC_BEAM_WID, RORIC_BEAM_HEI, null);
            hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
        }
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return SpriteManager.getInstance().getRoricBeamAnimations();
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
