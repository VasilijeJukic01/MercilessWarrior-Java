package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.debug.logger.Logger;
import platformer.debug.logger.Message;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Handles all visual rendering for the Player, including sprite animations, tinted dash afterimage ghosts,
 * parry clash visuals, and the fiery lava-burn silhouette overlay.
 */
public class PlayerRenderer {

    private static final int DASH_GHOST_COUNT = 5;
    private static final int PARRY_FLASH_MAX_TICKS = 22;

    private final Player player;
    private final BufferedImage[][] animations;
    private final BufferedImage[][] transformAnimations;

    private final int[] ghostX = new int[DASH_GHOST_COUNT];
    private final int[] ghostY = new int[DASH_GHOST_COUNT];
    private final int[] ghostFlipSign = new int[DASH_GHOST_COUNT];
    private final int[] ghostState = new int[DASH_GHOST_COUNT];
    private final int[] ghostFrame = new int[DASH_GHOST_COUNT];
    private int ghostHead = 0;
    private int ghostSize = 0;
    private int ghostRecordTick = 0;

    private BufferedImage dashGhostBuffer;
    private BufferedImage burnSilhouetteBuffer;

    // Debug
    private long lastRenderErrorLogMs = 0;

    public PlayerRenderer(Player player) {
        this.player = player;
        this.animations = SpriteManager.getInstance().getPlayerAnimations(false);
        this.transformAnimations = SpriteManager.getInstance().getPlayerAnimations(true);
    }

    /**
     * Records or decays dash afterimage snapshots each tick.
     */
    public void updateAfterimages(boolean isDashing, double pushOffset) {
        if (isDashing) {
            ghostRecordTick++;
            if (ghostRecordTick >= 3) {
                ghostRecordTick = 0;
                ghostX[ghostHead] = (int) (player.getHitBox().x - PLAYER_HB_OFFSET_X + player.getFlipCoefficient());
                ghostY[ghostHead] = (int) (player.getHitBox().y - PLAYER_HB_OFFSET_Y + pushOffset);
                ghostFlipSign[ghostHead] = player.getFlipSign();
                ghostState[ghostHead] = player.getEntityState().ordinal();
                ghostFrame[ghostHead] = player.getAnimIndex();
                ghostHead = (ghostHead + 1) % DASH_GHOST_COUNT;
                if (ghostSize < DASH_GHOST_COUNT) ghostSize++;
            }
        }
        else if (ghostSize > 0) {
            ghostRecordTick++;
            if (ghostRecordTick >= 3) {
                ghostRecordTick = 0;
                ghostSize--;
            }
        }
    }

    /**
     * Renders the player sprite, active dash afterimages, parry clash effects, and burn overlay.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, double pushOffset, int burnTick, int hitFlashTick, int invulnerabilityTick, int parryFlashTick) {
        int width = PLAYER_WIDTH;
        int height = PLAYER_HEIGHT;

        try {
            if (ghostSize > 0) renderDashAfterimages(g, xLevelOffset, yLevelOffset, width, height);
            int playerXPos = (int) (player.getHitBox().x - PLAYER_HB_OFFSET_X - xLevelOffset + player.getFlipCoefficient());
            int playerYPos = (int) (player.getHitBox().y - PLAYER_HB_OFFSET_Y - yLevelOffset) + (int) pushOffset;
            boolean transform = player.checkAction(PlayerAction.TRANSFORM);
            int stateOrd = player.getEntityState().ordinal();
            int animIndex = player.getAnimIndex();
            BufferedImage frame = (!transform) ? animations[stateOrd][animIndex] : transformAnimations[stateOrd][animIndex];

            if (invulnerabilityTick > 0 && hitFlashTick == 0) {
                Graphics2D g2d = (Graphics2D) g.create();
                float blinkAlpha = ((invulnerabilityTick / 6) % 2 == 0) ? 0.42f : 0.92f;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blinkAlpha));
                g2d.drawImage(frame, playerXPos, playerYPos, player.getFlipSign() * width, height, null);
                g2d.dispose();
            }
            else g.drawImage(frame, playerXPos, playerYPos, player.getFlipSign() * width, height, null);

            if (hitFlashTick > 0 && frame != null) {
                renderHitFlashOverlay(g, frame, playerXPos, playerYPos, width, height, hitFlashTick);
            }
            else if (parryFlashTick > 10 && frame != null) {
                renderParrySilhouetteGlint(g, frame, playerXPos, playerYPos, width, height, parryFlashTick);
            }
            else if (burnTick > 0 && frame != null) {
                renderBurnOverlay(g, frame, playerXPos, playerYPos, width, height, burnTick);
            }

            if (parryFlashTick > 0) {
                renderParryClashEffect(g, xLevelOffset, yLevelOffset, parryFlashTick);
            }
            renderAttackRecoveryIndicator(g, xLevelOffset, yLevelOffset, pushOffset);
        } catch (Exception e) {
            long now = System.currentTimeMillis();
            if (now - lastRenderErrorLogMs >= 1000) {
                lastRenderErrorLogMs = now;
                Logger.getInstance().notify("Render error in " + getClass().getSimpleName() + ": " + e.getMessage(), Message.ERROR);
            }
        }
    }

    private void renderParrySilhouetteGlint(Graphics g, BufferedImage frame, int drawX, int drawY, int width, int height, int parryFlashTick) {
        if (burnSilhouetteBuffer == null || burnSilhouetteBuffer.getWidth() != width || burnSilhouetteBuffer.getHeight() != height) {
            burnSilhouetteBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        float ratio = (parryFlashTick - 10) / (float) (PARRY_FLASH_MAX_TICKS - 10);
        int alpha = Math.max(0, Math.min(220, Math.round(ratio * 210)));
        Graphics2D bG2d = burnSilhouetteBuffer.createGraphics();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fillRect(0, 0, width, height);
        bG2d.setComposite(AlphaComposite.SrcOver);
        bG2d.drawImage(frame, 0, 0, width, height, null);

        bG2d.setComposite(AlphaComposite.SrcAtop);
        bG2d.setColor(new Color(255, 235, 165, alpha));
        bG2d.fillRect(0, 0, width, height);
        bG2d.dispose();
        g.drawImage(burnSilhouetteBuffer, drawX, drawY, player.getFlipSign() * width, height, null);
    }

    /**
     * Renders a crisp golden-white steel-clash star flare and expanding guard arc at the player's parry impact point.
     */
    private void renderParryClashEffect(Graphics g, int xLevelOffset, int yLevelOffset, int parryFlashTick) {
        float life = (float) parryFlashTick / PARRY_FLASH_MAX_TICKS;
        float progress = 1.0f - life;
        int flip = player.getFlipSign();

        double cx = player.getHitBox().getCenterX() + flip * (14.0 * SCALE) - xLevelOffset;
        double cy = player.getHitBox().getCenterY() - (4.0 * SCALE) - yLevelOffset;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Expanding curved guard wave in front of the blade
        double arcRadius = (16.0 + progress * 24.0) * SCALE;
        int arcAlpha = Math.max(0, Math.min(255, (int) (life * 225)));
        double startAngle = (flip == 1) ? -58.0 : 122.0;
        double extent = 116.0;

        Arc2D.Double guardArc = new Arc2D.Double(cx - arcRadius, cy - arcRadius, arcRadius * 2.0, arcRadius * 2.0, startAngle, extent, Arc2D.OPEN);
        g2d.setColor(new Color(255, 200, 75, arcAlpha / 2));
        g2d.setStroke(new BasicStroke((4.2f * life + 1.0f) * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(guardArc);

        g2d.setColor(new Color(255, 250, 225, arcAlpha));
        g2d.setStroke(new BasicStroke((1.8f * life + 0.6f) * SCALE, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.draw(guardArc);

        double outerArm = (30.0 * Math.sin(life * Math.PI * 0.72) + 8.0 * life) * SCALE;
        double innerCore = Math.max(1.5, 4.2 * life * SCALE);
        double rot = Math.toRadians(15.0 + progress * 28.0) * flip;

        AffineTransform oldTx = g2d.getTransform();
        g2d.translate(cx, cy);
        g2d.rotate(rot);

        Path2D.Double outerStar = buildFourPointFlare(outerArm * 1.15, innerCore * 1.45);
        g2d.setColor(new Color(255, 195, 55, Math.max(0, Math.min(255, (int) (life * 195)))));
        g2d.fill(outerStar);

        Path2D.Double innerStar = buildFourPointFlare(outerArm * 0.85, innerCore * 0.85);
        g2d.setColor(new Color(255, 255, 245, Math.max(0, Math.min(255, (int) (life * 255)))));
        g2d.fill(innerStar);

        // Horizontal glinting slash streak across the impact point
        double streakLen = (26.0 + progress * 18.0) * SCALE;
        double streakHalfH = Math.max(1.0, 2.2 * life * SCALE);
        Path2D.Double streak = new Path2D.Double();
        streak.moveTo(-streakLen, 0);
        streak.quadTo(0, -streakHalfH, streakLen, 0);
        streak.quadTo(0, streakHalfH, -streakLen, 0);
        streak.closePath();
        g2d.setColor(new Color(200, 252, 255, Math.max(0, Math.min(255, (int) (life * 210)))));
        g2d.fill(streak);

        g2d.setTransform(oldTx);
        g2d.dispose();
    }

    private Path2D.Double buildFourPointFlare(double outerR, double innerR) {
        Path2D.Double star = new Path2D.Double();
        for (int i = 0; i < 8; i++) {
            double angle = i * (Math.PI / 4.0);
            double r = (i % 2 == 0) ? outerR : innerR;
            double px = Math.cos(angle) * r;
            double py = Math.sin(angle) * r;
            if (i == 0) star.moveTo(px, py);
            else star.lineTo(px, py);
        }
        star.closePath();
        return star;
    }

    private void renderHitFlashOverlay(Graphics g, BufferedImage frame, int drawX, int drawY, int width, int height, int hitFlashTick) {
        if (burnSilhouetteBuffer == null || burnSilhouetteBuffer.getWidth() != width || burnSilhouetteBuffer.getHeight() != height) {
            burnSilhouetteBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D bG2d = burnSilhouetteBuffer.createGraphics();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fillRect(0, 0, width, height);
        bG2d.setComposite(AlphaComposite.SrcOver);
        bG2d.drawImage(frame, 0, 0, width, height, null);

        bG2d.setComposite(AlphaComposite.SrcAtop);
        Color flashTint = (hitFlashTick > 11) ? new Color(255, 255, 255, 210) : new Color(255, 35, 45, (int) ((hitFlashTick / 11.0f) * 185));
        bG2d.setColor(flashTint);
        bG2d.fillRect(0, 0, width, height);
        bG2d.dispose();

        g.drawImage(burnSilhouetteBuffer, drawX, drawY, player.getFlipSign() * width, height, null);
    }

    private void renderDashAfterimages(Graphics g, int xLevelOffset, int yLevelOffset, int width, int height) {
        if (dashGhostBuffer == null || dashGhostBuffer.getWidth() != width || dashGhostBuffer.getHeight() != height) {
            dashGhostBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        boolean transformed = player.checkAction(PlayerAction.TRANSFORM);
        BufferedImage[][] sheet = transformed ? transformAnimations : animations;
        Color tintColor = transformed ? new Color(255, 130, 25, 190) : new Color(55, 235, 245, 185);

        Graphics2D g2d = (Graphics2D) g.create();
        int count = ghostSize;
        int head = ghostHead;

        for (int i = count - 1; i >= 0; i--) {
            int idx = (head - 1 - i + DASH_GHOST_COUNT) % DASH_GHOST_COUNT;
            BufferedImage[] stateFrames = sheet[ghostState[idx]];
            if (stateFrames == null || ghostFrame[idx] >= stateFrames.length) continue;
            BufferedImage frame = stateFrames[ghostFrame[idx]];
            if (frame == null) continue;

            Graphics2D bG2d = dashGhostBuffer.createGraphics();
            bG2d.setComposite(AlphaComposite.Clear);
            bG2d.fillRect(0, 0, width, height);
            bG2d.setComposite(AlphaComposite.SrcOver);
            bG2d.drawImage(frame, 0, 0, width, height, null);
            bG2d.setComposite(AlphaComposite.SrcAtop);
            bG2d.setColor(tintColor);
            bG2d.fillRect(0, 0, width, height);
            bG2d.dispose();

            float alpha = (1.0f - (float) i / DASH_GHOST_COUNT) * 0.48f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            int gx = ghostX[idx] - xLevelOffset;
            int gy = ghostY[idx] - yLevelOffset;
            g2d.drawImage(dashGhostBuffer, gx, gy, width * ghostFlipSign[idx], height, null);
        }
        g2d.dispose();
    }

    private void renderBurnOverlay(Graphics g, BufferedImage frame, int drawX, int drawY, int width, int height, int burnTick) {
        if (burnSilhouetteBuffer == null || burnSilhouetteBuffer.getWidth() != width || burnSilhouetteBuffer.getHeight() != height) {
            burnSilhouetteBuffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        Graphics2D bG2d = burnSilhouetteBuffer.createGraphics();
        bG2d.setComposite(AlphaComposite.Clear);
        bG2d.fillRect(0, 0, width, height);
        bG2d.setComposite(AlphaComposite.SrcOver);
        bG2d.drawImage(frame, 0, 0, width, height, null);

        bG2d.setComposite(AlphaComposite.SrcAtop);
        boolean brightFlicker = (burnTick / 5) % 2 == 0;
        float alpha = Math.min(0.62f, 0.25f + (burnTick / 90.0f) * 0.35f);
        Color fireTint = brightFlicker ? new Color(255, 130, 15, (int) (alpha * 255)) : new Color(235, 45, 10, (int) (alpha * 255));
        bG2d.setColor(fireTint);
        bG2d.fillRect(0, 0, width, height);
        bG2d.dispose();

        g.drawImage(burnSilhouetteBuffer, drawX, drawY, player.getFlipSign() * width, height, null);
    }

    /**
     * Renders the diegetic overhead combo-warning pips and finisher recovery bar above the player's head.
     */
    private void renderAttackRecoveryIndicator(Graphics g, int xLevelOffset, int yLevelOffset, double pushOffset) {
        if (player.getCurrentHealth() <= 0 || player.checkAction(PlayerAction.DYING)) return;
        if (player.getUltimateHandler() != null && player.getUltimateHandler().isActive()) return;

        PlayerCombatHandler combat = player.getCombatHandler();
        if (combat == null) return;

        boolean showBar = combat.isShowingCooldownBar();
        int comboPips = combat.getActiveComboPips();
        float readyRatio = combat.getReadyFlashRatio();
        if (!showBar && comboPips <= 0 && readyRatio <= 0.0f) return;

        int barW = scale(26);
        int barH = Math.max(4, scale(3.5));
        int centerX = (int) (player.getHitBox().getCenterX() - xLevelOffset);
        int barY = (int) (player.getHitBox().y - yLevelOffset - scale(10) + pushOffset);

        float deniedRatio = combat.getDeniedFlashRatio();
        int shakeX = (deniedRatio > 0.0f) ? (int) Math.round(Math.sin(deniedRatio * Math.PI * 5.0) * (1.8 * SCALE * deniedRatio)) : 0;
        int barX = centerX - barW / 2 + shakeX;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (showBar) renderCooldownBar(g2d, barX, barY, barW, barH, combat.getAttackCooldownRatio(), deniedRatio);
        else if (readyRatio > 0.0f) renderReadyFlash(g2d, barX, barY, barW, barH, readyRatio);
        else renderComboPips(g2d, barX, barY, barW, barH, comboPips);

        g2d.dispose();
    }

    private void renderComboPips(Graphics2D g2d, int barX, int barY, int barW, int barH, int litPips) {
        int gap = Math.max(2, scale(1.5));
        int pipW = (barW - gap * 2) / 3;
        int arc = Math.max(2, scale(1.5));
        boolean warningFinisher = (litPips >= 2);
        double pulse = 0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 0.018);

        for (int i = 0; i < 3; i++) {
            int px = barX + i * (pipW + gap);
            g2d.setColor(new Color(10, 18, 22, 185));
            g2d.fillRoundRect(px, barY, pipW, barH, arc, arc);

            if (i < litPips) {
                Color fill = warningFinisher ? new Color(255, 175, 45, 240) : new Color(55, 235, 225, 230);
                g2d.setColor(fill);
                g2d.fillRoundRect(px + 1, barY + 1, Math.max(1, pipW - 2), Math.max(1, barH - 2), arc, arc);
            }
            else if (warningFinisher && i == 2) {
                int warnAlpha = (int) (70 + 130 * pulse);
                g2d.setColor(new Color(255, 85, 55, warnAlpha));
                g2d.drawRoundRect(px, barY, pipW, barH, arc, arc);
                continue;
            }

            g2d.setColor(new Color(85, 115, 122, 190));
            g2d.drawRoundRect(px, barY, pipW, barH, arc, arc);
        }
    }

    private void renderCooldownBar(Graphics2D g2d, int barX, int barY, int barW, int barH, float ratio, float deniedRatio) {
        int arc = Math.max(2, scale(1.8));
        g2d.setColor(new Color(10, 16, 20, 210));
        g2d.fillRoundRect(barX, barY, barW, barH, arc, arc);

        int innerW = Math.max(1, barW - 2);
        int innerH = Math.max(1, barH - 2);
        int fillW = Math.max(1, Math.round(innerW * ratio));

        Color fillColor;
        if (deniedRatio > 0.0f) {
            fillColor = new Color(255, 55, 65, 245);
        }
        else {
            int gComp = (int) (75 + (1.0f - ratio) * 135);
            int bComp = (int) (45 + (1.0f - ratio) * 35);
            fillColor = new Color(255, gComp, bComp, 235);
        }

        g2d.setColor(fillColor);
        g2d.fillRoundRect(barX + 1, barY + 1, fillW, innerH, arc, arc);

        g2d.setColor(new Color(255, 245, 220, 120));
        g2d.fillRect(barX + 1, barY + 1, fillW, 1);

        Color borderColor = (deniedRatio > 0.0f) ? new Color(255, 120, 120, 240) : new Color(110, 130, 135, 210);
        g2d.setColor(borderColor);
        g2d.drawRoundRect(barX, barY, barW, barH, arc, arc);
    }

    private void renderReadyFlash(Graphics2D g2d, int barX, int barY, int barW, int barH, float readyRatio) {
        int arc = Math.max(2, scale(1.8));
        int alpha = Math.max(0, Math.min(255, Math.round(readyRatio * 235)));
        g2d.setColor(new Color(90, 250, 245, alpha));
        g2d.fillRoundRect(barX, barY, barW, barH, arc, arc);
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.drawRoundRect(barX, barY, barW, barH, arc, arc);
    }

    /**
     * Immediately records an afterimage ghost snapshot for the Ultimate Blitz dash.
     */
    public void recordInstantSnapshot(double pushOffset) {
        ghostX[ghostHead] = (int) (player.getHitBox().x - PLAYER_HB_OFFSET_X + player.getFlipCoefficient());
        ghostY[ghostHead] = (int) (player.getHitBox().y - PLAYER_HB_OFFSET_Y + pushOffset);
        ghostFlipSign[ghostHead] = player.getFlipSign();
        ghostState[ghostHead] = player.getEntityState().ordinal();
        ghostFrame[ghostHead] = player.getAnimIndex();
        ghostHead = (ghostHead + 1) % DASH_GHOST_COUNT;
        if (ghostSize < DASH_GHOST_COUNT) ghostSize++;
    }

    public int getFrameCount(Anim state) {
        return animations[state.ordinal()].length;
    }

    public void reset() {
        ghostSize = 0;
        ghostHead = 0;
        ghostRecordTick = 0;
    }
}