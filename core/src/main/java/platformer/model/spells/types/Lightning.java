package platformer.model.spells.types;

import platformer.animation.SpriteManager;
import platformer.core.config.BalanceManager;
import platformer.event.EventBus;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.model.entities.player.Player;
import platformer.model.spells.Spell;
import platformer.model.spells.SpellType;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class Lightning extends Spell {

    private static final int TELEGRAPH_DURATION = 85;

    private boolean hasHitPlayer = false;
    private boolean isTelegraphing = true;
    private int telegraphTimer = 0;

    public Lightning(SpellType spellType, int xPos, int yPos) {
        super(spellType, xPos, yPos, LIGHTNING_WIDTH, LIGHTNING_HEIGHT);
        super.setActive(false);
        initHitBox(width / 1.5, height);
    }

    @Override
    public void update(Player player) {
        if (!active) return;
        if (isTelegraphing) {
            telegraphTimer++;
            if (telegraphTimer >= TELEGRAPH_DURATION) {
                isTelegraphing = false;
                EventBus.getInstance().publish(new ScreenShakeEvent(22, 12.0));
            }
            return;
        }
        if (!hasHitPlayer && animIndex > 0 && animIndex < 5) {
            if (hitBox.intersects(player.getHitBox())) {
                player.changeHealth(-BalanceManager.getInstance().getCombat().getLightningSpellDamage(), this);
                hasHitPlayer = true;
            }
        }
        updateAnimation();
    }

    @Override
    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!active) return;
        if (isTelegraphing) {
            renderTelegraph(g, xLevelOffset, yLevelOffset);
            return;
        }

        int x = (int) getHitBox().x - xLevelOffset - LIGHTNING_OFFSET_X;
        int y = (int) getHitBox().y - yLevelOffset + 1;
        g.drawImage(getAnimations()[getAnimIndex()], x, y, getWidth(), getHeight(), null);
        hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.CYAN);
    }

    private void renderTelegraph(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boxX = (int) getHitBox().x - xLevelOffset, boxY = (int) getHitBox().y - yLevelOffset;
        int boxW = (int) getHitBox().width, boxH = (int) getHitBox().height;
        int centerX = boxX + boxW / 2;
        int groundY = boxY + boxH + (int) (5 * SCALE);

        float pulse = (float) (0.7 + 0.3 * Math.sin(telegraphTimer * 0.3));
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pulse));

        int padW = (int) (boxW * 1.45);
        int padH = (int) (10 * SCALE);
        int padX = centerX - padW / 2;
        int padY = groundY - padH / 2;

        Color centerCol = new Color(140, 250, 255, 210);
        Color edgeCol = new Color(0, 140, 210, 70);
        GradientPaint groundPad = new GradientPaint(padX, padY, edgeCol, centerX, padY, centerCol, true);
        g2d.setPaint(groundPad);
        g2d.fillRoundRect(padX, padY, padW, padH, 10, 10);
        g2d.dispose();
    }

    @Override
    protected BufferedImage[] getAnimations() {
        return SpriteManager.getInstance().getLightningAnimations();
    }

    @Override
    public void reset() {
        animTick = animIndex = 0;
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
}
