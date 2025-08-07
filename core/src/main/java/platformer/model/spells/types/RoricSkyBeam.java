package platformer.model.spells.types;

import platformer.animation.SpriteManager;
import platformer.model.entities.player.Player;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;
import platformer.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class RoricSkyBeam extends Spell {

    private final boolean isTargeted;
    private boolean hasHitPlayer = false;

    public RoricSkyBeam(SpellType spellType, int xPos, int yPos, boolean isTargeted) {
        super(spellType, xPos, yPos, RORIC_BEAM_HEI, RORIC_BEAM_WID);
        this.isTargeted = isTargeted;
        super.animSpeed = 25;
        int offset = isTargeted ? (int) (25 * SCALE) : 0;
        initHitBox(width / 10.0 + offset, height);
    }

    @Override
    public void update(Player player) {
        if (!active) return;
        if (!hasHitPlayer && animIndex >= 3 && animIndex <= 5) {
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
            int x = (int) getHitBox().x - xLevelOffset;
            int y = (int) getHitBox().y - yLevelOffset;
            BufferedImage frame = getAnimations()[getAnimIndex()];
            BufferedImage rotatedFrame = ImageUtils.rotateImage(frame, 90);

            int offset = isTargeted() ? (int)(150 * SCALE) : (int)(67 * SCALE);
            int drawX = x + (getWidth() - rotatedFrame.getWidth()) / 2 - offset;
            int drawY = y + (getHeight() - rotatedFrame.getHeight()) / 2 - (int)(180 * SCALE);

            int renderWidth = isTargeted() ? RORIC_BEAM_HEI + (int) (190 * SCALE) : RORIC_BEAM_HEI;

            g.drawImage(rotatedFrame, drawX, drawY, renderWidth, RORIC_BEAM_WID, null);
            hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
        }
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return SpriteManager.getInstance().getSkyBeamAnimations();
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

    public boolean isTargeted() {
        return isTargeted;
    }
}
