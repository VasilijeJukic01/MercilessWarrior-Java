package platformer.model.entities.effects;

import platformer.animation.Animation;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.*;
import java.awt.image.BufferedImage;

import static platformer.constants.AnimConstants.DUST1_H;
import static platformer.constants.AnimConstants.DUST1_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.DUST_SHEET_1;

public class PlayerEffectController {

    private final Entity entity;

    private BufferedImage[][] effects;
    private final int animSpeed = 20;
    private int animTick = 0, effectIndex = 0;

    public PlayerEffectController(Entity entity) {
        this.entity = entity;
        loadEffects();
    }

    // Init
    private void loadEffects() {
        BufferedImage[][] anim = new BufferedImage[11][11];
        anim[EffectType.WALL_SLIDE.ordinal()] = Animation.getInstance().loadFromSprite(DUST_SHEET_1, 8, 0, DUST1_W, DUST1_H, 0, DUST1_W, DUST1_H);
        this.effects = anim;
    }

    private void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            effectIndex++;
        }
    }

    private void updateEffectAnimation() {
        if (entity.getEntityEffect() == EffectType.WALL_SLIDE) {
            if (effectIndex >= effects[EffectType.WALL_SLIDE.ordinal()].length) {
                effectIndex = 2;
            }
        }
    }

    // Core
    public void update() {
        updateAnimation();
        updateEffectAnimation();
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderWallSlide(g, xLevelOffset, yLevelOffset);
    }

    private void renderWallSlide(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!(entity instanceof Player)) return;
        if (entity.getEntityEffect() != EffectType.WALL_SLIDE) return;
        Player p = (Player) entity;
        boolean onWall = p.checkAction(PlayerAction.ON_WALL);
        if (onWall) {
            int wid = (p.getFlipCoefficient() != 0) ? (0) : (int)(p.getHitBox().width + DUST1_W);
            int flip = (p.getFlipSign() == 1) ? (-1) : (1);
            int xPos = (int)(p.getHitBox().x - DUST1_OFFSET_X) + wid;
            int yPos = (int)(p.getHitBox().y - DUST1_OFFSET_Y);
            g.drawImage(effects[EffectType.WALL_SLIDE.ordinal()][effectIndex], xPos-xLevelOffset, yPos-yLevelOffset, flip*DUST1_WID, DUST1_HEI, null);
        }
    }

    public void setPlayerEffect(EffectType effect) {
        if (effect != entity.getEntityEffect()) effectIndex = 0;
        entity.setEntityEffect(effect);
    }

}
