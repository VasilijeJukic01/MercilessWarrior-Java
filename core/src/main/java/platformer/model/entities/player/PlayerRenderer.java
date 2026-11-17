package platformer.model.entities.player;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Handles all visual rendering for the Player, including sprite animations, tinted dash afterimage ghosts, and the fiery lava-burn silhouette overlay.
 */
public class PlayerRenderer {

    private static final int DASH_GHOST_COUNT = 5;

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
     * Renders the player sprite, active dash afterimages, and burn overlay.
     */
    public void render(Graphics g, int xLevelOffset, int yLevelOffset, double pushOffset, int burnTick, int hitFlashTick) {
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
            g.drawImage(frame, playerXPos, playerYPos, player.getFlipSign() * width, height, null);
            if (hitFlashTick > 0 && frame != null) {
                renderHitFlashOverlay(g, frame, playerXPos, playerYPos, width, height, hitFlashTick);
            }
            else if (burnTick > 0 && frame != null) {
                renderBurnOverlay(g, frame, playerXPos, playerYPos, width, height, burnTick);
            }
        } catch (Exception ignored) {}
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

    public int getFrameCount(Anim state) {
        return animations[state.ordinal()].length;
    }

    public void reset() {
        ghostSize = 0;
        ghostHead = 0;
        ghostRecordTick = 0;
    }
}