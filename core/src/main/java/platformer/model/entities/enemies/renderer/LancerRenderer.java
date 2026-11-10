package platformer.model.entities.enemies.renderer;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.boss.Lancer;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class LancerRenderer implements EnemyRenderer<Lancer> {

    private static final int GHOST_COUNT = 4;

    private final BufferedImage[][] animations;
    private final int[] ghostX = new int[GHOST_COUNT];
    private final int[] ghostY = new int[GHOST_COUNT];
    private final int[] ghostFlipSign = new int[GHOST_COUNT];
    private final int[] ghostState = new int[GHOST_COUNT];
    private final int[] ghostFrame = new int[GHOST_COUNT];
    private int ghostHead = 0;
    private int ghostSize = 0;
    private int recordTick = 0;

    public LancerRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.LANCER);
    }

    @Override
    public void render(Graphics g, Lancer lancer, int xLevelOffset, int yLevelOffset) {
        Anim state = lancer.getEnemyAction();
        int fC = lancer.getFlipCoefficient(), fS = lancer.getFlipSign();
        int worldDrawX = (int) lancer.getHitBox().x - LANCER_X_OFFSET + fC - (fS == -1 ? (int) (21 * SCALE) : 0);
        int worldDrawY = (int) lancer.getHitBox().y - LANCER_Y_OFFSET + (int) lancer.getPushOffset() + 1;

        boolean showAfterimages = (state == Anim.SPELL_1 && lancer.getAnimIndex() >= 2 && lancer.getAnimIndex() <= 11) || (state == Anim.ATTACK_3 && lancer.getAnimIndex() >= 1 && lancer.getAnimIndex() <= 4);
        if (showAfterimages) {
            recordTick++;
            if (recordTick >= 3) {
                recordTick = 0;
                ghostX[ghostHead] = worldDrawX;
                ghostY[ghostHead] = worldDrawY;
                ghostFlipSign[ghostHead] = fS;
                ghostState[ghostHead] = state.ordinal();
                ghostFrame[ghostHead] = lancer.getAnimIndex();
                ghostHead = (ghostHead + 1) % GHOST_COUNT;
                if (ghostSize < GHOST_COUNT) ghostSize++;
            }
            renderAfterimages(g, xLevelOffset, yLevelOffset);
        }
        else ghostSize = 0;

        int screenX = worldDrawX - xLevelOffset, screenY = worldDrawY - yLevelOffset;
        g.drawImage(animations[state.ordinal()][lancer.getAnimIndex()], screenX, screenY, LANCER_WIDTH * fS, LANCER_HEIGHT, null);

        lancer.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        lancer.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

    private void renderAfterimages(Graphics g, int xLevelOffset, int yLevelOffset) {
        Graphics2D g2d = (Graphics2D) g.create();
        int count = ghostSize;
        int head = ghostHead;
        for (int i = count - 1; i >= 0; i--) {
            int idx = (head - 1 - i + GHOST_COUNT) % GHOST_COUNT;
            float alpha = (1.0f - (float) i / GHOST_COUNT) * 0.42f;
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            BufferedImage frame = animations[ghostState[idx]][ghostFrame[idx]];
            int gx = ghostX[idx] - xLevelOffset;
            int gy = ghostY[idx] - yLevelOffset;
            g2d.drawImage(frame, gx, gy, LANCER_WIDTH * ghostFlipSign[idx], LANCER_HEIGHT, null);
        }
        g2d.dispose();
    }
}