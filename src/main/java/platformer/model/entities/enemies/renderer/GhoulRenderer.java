package platformer.model.entities.enemies.renderer;

import platformer.animation.Anim;
import platformer.model.entities.enemies.Ghoul;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;
import static platformer.constants.Constants.GHOUL_HEIGHT;

public class GhoulRenderer implements EnemyRenderer<Ghoul> {

    private final BufferedImage[][] animations;

    public GhoulRenderer(BufferedImage[][] animations) {
        this.animations = animations;
    }

    @Override
    public void render(Graphics g, Ghoul gh, int xLevelOffset, int yLevelOffset) {
        int fC = gh.getFlipCoefficient(), fS = gh.getFlipSign();
        int x = (int) gh.getHitBox().x - GHOUL_X_OFFSET - xLevelOffset + fC;
        int y = (int) gh.getHitBox().y - GHOUL_Y_OFFSET - yLevelOffset + (int)gh.getPushOffset() + 1;
        g.drawImage(animations[gh.getEnemyAction().ordinal()][gh.getAnimIndex()], x, y, GHOUL_WIDTH * fS, GHOUL_HEIGHT, null);
        if (gh.getEnemyAction() == Anim.HIDE || gh.getEnemyAction() == Anim.REVEAL) {
            int r = (gh.getAnimIndex() > 15 && gh.getEnemyAction() == Anim.REVEAL) ? (255) : (0);
            g.setColor(new Color(r, 0, 0, gh.getFadeCoefficient()));
            g.fillRect(0, 0, GAME_WIDTH, GAME_HEIGHT);
        }
        gh.hitBoxRenderer(g, xLevelOffset, yLevelOffset, Color.BLUE);
        gh.attackBoxRenderer(g, xLevelOffset, yLevelOffset);
    }

}
