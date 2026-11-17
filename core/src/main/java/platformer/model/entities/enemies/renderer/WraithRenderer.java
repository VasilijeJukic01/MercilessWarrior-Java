package platformer.model.entities.enemies.renderer;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Wraith;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Renders the Wraith with levitation bobbing, translucent spectral afterimages, and orbiting soul wisps.
 */
public class WraithRenderer implements EnemyRenderer<Wraith> {

    private static final int GHOST_COUNT = 3;

    private final BufferedImage[][] animations;
    private final int[] ghostX = new int[GHOST_COUNT];
    private final int[] ghostY = new int[GHOST_COUNT];
    private final int[] ghostFlipSign = new int[GHOST_COUNT];
    private final int[] ghostState = new int[GHOST_COUNT];
    private final int[] ghostFrame = new int[GHOST_COUNT];
    private int ghostHead = 0;
    private int ghostSize = 0;
    private int recordTick = 0;

    public WraithRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.WRAITH);
    }

    @Override
    public void render(Graphics g, Wraith w, int xLevelOffset, int yLevelOffset) {
        Anim state = w.getEnemyAction();
        int fC = w.getFlipCoefficient(), fS = w.getFlipSign();
        int hoverY = w.getHoverOffsetY();

        int worldDrawX = (int) w.getHitBox().x - WRAITH_X_OFFSET + fC;
        int worldDrawY = (int) w.getHitBox().y - WRAITH_Y_OFFSET + (int) w.getPushOffset() + hoverY + 1;

        boolean showTrail = (state == Anim.RUN || state == Anim.ATTACK_1 || state == Anim.ATTACK_2 || state == Anim.SPELL_1 || w.getPhaseAlpha() < 0.95f);
        if (showTrail && state != Anim.DEATH) {
            recordTick++;
            if (recordTick >= 4) {
                recordTick = 0;
                ghostX[ghostHead] = worldDrawX;
                ghostY[ghostHead] = worldDrawY;
                ghostFlipSign[ghostHead] = fS;
                ghostState[ghostHead] = state.ordinal();
                ghostFrame[ghostHead] = w.getAnimIndex();
                ghostHead = (ghostHead + 1) % GHOST_COUNT;
                if (ghostSize < GHOST_COUNT) ghostSize++;
            }
            renderAfterimages(g, xLevelOffset, yLevelOffset);
        }
        else ghostSize = 0;

        int cx = (int) (w.getHitBox().getCenterX() - xLevelOffset);
        int cy = (int) (w.getHitBox().getCenterY() - yLevelOffset + hoverY);
        if (state != Anim.DEATH) {
            renderOrbitingWisps(g, cx, cy, state == Anim.SPELL_1 || state == Anim.ATTACK_2, false);
        }

        int screenX = worldDrawX - xLevelOffset;
        int screenY = worldDrawY - yLevelOffset;

        Graphics2D g2d = (Graphics2D) g.create();
        float alpha = w.getPhaseAlpha();
        if (alpha < 0.99f) {
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.15f, alpha)));
        }
        drawEnemySprite(g2d, w, animations[state.ordinal()][w.getAnimIndex()], screenX, screenY, WRAITH_WIDTH * fS, WRAITH_HEIGHT);
        g2d.dispose();

        if (state != Anim.DEATH) {
            renderOrbitingWisps(g, cx, cy, state == Anim.SPELL_1 || state == Anim.ATTACK_2, true);
        }

        w.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        w.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderAfterimages(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        for (int i = ghostSize - 1; i >= 0; i--) {
            int idx = (ghostHead - 1 - i + GHOST_COUNT) % GHOST_COUNT;
            float alpha = (1.0f - (float) i / GHOST_COUNT) * 0.30f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            BufferedImage[] stateFrames = animations[ghostState[idx]];
            if (stateFrames != null && ghostFrame[idx] < stateFrames.length) {
                BufferedImage frame = stateFrames[ghostFrame[idx]];
                g2d.drawImage(frame, ghostX[idx] - xLevelOffset, ghostY[idx] - yLevelOffset, WRAITH_WIDTH * ghostFlipSign[idx], WRAITH_HEIGHT, null);
            }
        }
        g2d.dispose();
    }

    private void renderOrbitingWisps(Graphics g, int cx, int cy, boolean casting, boolean frontPass) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double time = System.currentTimeMillis() * (casting ? 0.008 : 0.0035);
        double rx = (casting ? 22.0 : 16.0) * SCALE;
        double ry = 6.0 * SCALE;

        for (int i = 0; i < 2; i++) {
            double angle = time + i * Math.PI;
            double z = Math.sin(angle);
            boolean isFront = z >= 0;
            if (isFront != frontPass) continue;

            int wx = cx + (int) (Math.cos(angle) * rx);
            int wy = cy + (int) (z * ry + Math.sin(time * 1.5 + i) * (3 * SCALE));
            int r = (int) ((casting ? 7.5 : 5.5) * SCALE);

            RadialGradientPaint wispGlow = new RadialGradientPaint(
                    new Point2D.Float(wx, wy),
                    r,
                    new float[]{0.0f, 0.5f, 1.0f},
                    new Color[]{new Color(220, 255, 250, 220), new Color(0, 225, 200, 140), new Color(0, 120, 130, 0)}
            );
            g2d.setPaint(wispGlow);
            g2d.fillOval(wx - r, wy - r, r * 2, r * 2);
        }

        g2d.dispose();
    }
}