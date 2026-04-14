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

    // Telegraph
    private boolean isTelegraphing = true;
    private int telegraphTimer = 0;
    private static final int TELEGRAPH_DURATION = 60;

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
        if (isTelegraphing) {
            telegraphTimer++;
            if (telegraphTimer >= TELEGRAPH_DURATION) {
                isTelegraphing = false;
            }
            return;
        }
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
        if (!isActive()) return;
        int x = (int) getHitBox().x - xLevelOffset;
        int y = (int) getHitBox().y - yLevelOffset;
        int offset = isTargeted() ? (int)(150 * SCALE) : (int)(67 * SCALE);
        int renderWidth = isTargeted() ? RORIC_BEAM_HEI + (int) (190 * SCALE) : RORIC_BEAM_HEI;

        if (isTelegraphing) {
            Graphics2D g2d = (Graphics2D) g.create();

            float pulse = (float) (0.4 + 0.3 * Math.sin(telegraphTimer * 0.4));
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));

            int telegraphX = (int) getHitBox().x - xLevelOffset;
            int telegraphWidth = (int) getHitBox().width;

            g2d.setColor(new Color(255, 40, 40, 150));
            g2d.fillRect(telegraphX, 0, telegraphWidth, GAME_HEIGHT);
            g2d.setColor(new Color(255, 200, 200, 220));
            g2d.fillRect(telegraphX + (telegraphWidth / 2) - 2, 0, 4, GAME_HEIGHT);
            g2d.dispose();
        }
        else {
            BufferedImage frame = getAnimations()[getAnimIndex()];
            BufferedImage rotatedFrame = ImageUtils.rotateImage(frame, 90);
            int drawX = x + (getWidth() - rotatedFrame.getWidth()) / 2 - offset;
            int drawY = y + (getHeight() - rotatedFrame.getHeight()) / 2 - (int)(180 * SCALE);

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
        isTelegraphing = true;
        telegraphTimer = 0;
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
