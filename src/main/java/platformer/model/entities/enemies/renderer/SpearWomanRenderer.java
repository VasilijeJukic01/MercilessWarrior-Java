package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.boss.SpearWoman;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class SpearWomanRenderer implements EnemyRenderer<SpearWoman> {

    private final BufferedImage[][] spearWomanAnimations;

    public SpearWomanRenderer(BufferedImage[][] spearWomanAnimations) {
        this.spearWomanAnimations = spearWomanAnimations;
    }

    @Override
    public void render(Graphics g, SpearWoman spearWoman, int xLevelOffset, int yLevelOffset) {
        int fC = spearWoman.getFlipCoefficient(), fS = spearWoman.getFlipSign();
        int x = (int) spearWoman.getHitBox().x - SW_X_OFFSET - xLevelOffset + fC;
        int y = (int) spearWoman.getHitBox().y - SW_Y_OFFSET - yLevelOffset+1 + (int)spearWoman.getPushOffset();
        int w = SW_WIDTH * fS;
        int h = SW_HEIGHT;
        if (fS == -1) x -= 21*SCALE;
        g.drawImage(spearWomanAnimations[spearWoman.getEnemyAction().ordinal()][spearWoman.getAnimIndex()], x, y, w, h, null);
        spearWoman.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        spearWoman.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
        spearWoman.overlayRender(g);
    }
}
