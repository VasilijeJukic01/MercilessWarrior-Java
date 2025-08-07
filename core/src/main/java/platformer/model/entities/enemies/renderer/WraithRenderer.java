package platformer.model.entities.enemies.renderer;

import platformer.animation.SpriteManager;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.Wraith;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

public class WraithRenderer implements EnemyRenderer<Wraith> {

    private final BufferedImage[][] animations;

    public WraithRenderer() {
        this.animations = SpriteManager.getInstance().getEnemyAnimations(EnemyType.WRAITH);
    }

    @Override
    public void render(Graphics g, Wraith w, int xLevelOffset, int yLevelOffset) {
        int fC = w.getFlipCoefficient(), fS = w.getFlipSign();
        int x = (int) w.getHitBox().x - WRAITH_X_OFFSET - xLevelOffset + fC;
        int y = (int) w.getHitBox().y - WRAITH_Y_OFFSET - yLevelOffset + (int)w.getPushOffset() + 1;
        g.drawImage(animations[w.getEnemyAction().ordinal()][w.getAnimIndex()], x, y, WRAITH_WIDTH * fS, WRAITH_HEIGHT, null);
        w.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        w.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

}
