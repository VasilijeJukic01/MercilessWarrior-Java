package platformer.model.entities.enemies.renderer;

import platformer.model.entities.enemies.boss.SpearWoman;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class SpearWomanRenderer implements EnemyRenderer<SpearWoman> {

    private final BufferedImage[][] animations;

    public SpearWomanRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, SpearWoman spearWoman, int xLevelOffset, int yLevelOffset) {
        int fC = spearWoman.getFlipCoefficient(), fS = spearWoman.getFlipSign();
        int x = (int) spearWoman.getHitBox().x - SW_X_OFFSET - xLevelOffset + fC;
        int y = (int) spearWoman.getHitBox().y - SW_Y_OFFSET - yLevelOffset + (int)spearWoman.getPushOffset() + 1;
        if (fS == -1) x -= 21*SCALE;
        g.drawImage(animations[spearWoman.getEnemyAction().ordinal()][spearWoman.getAnimIndex()], x, y, SW_WIDTH * fS, SW_HEIGHT, null);
        spearWoman.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        spearWoman.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
        spearWoman.overlayRender(g);
    }
}
