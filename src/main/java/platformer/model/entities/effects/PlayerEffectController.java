package platformer.model.entities.effects;

import platformer.animation.AnimUtils;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;

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
        anim[EffectType.WALL_SLIDE.ordinal()] = AnimUtils.getInstance().loadFromSprite(DUST_SHEET_1, 8, 0, DUST1_W, DUST1_H, 0, DUST1_W, DUST1_H);
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
        if (p.isOnWall()) {
            int newFlip = (p.getFlipCoefficient() != 0) ? (0) : (int)(PLAYER_WIDTH-p.getHitBox().width-10*SCALE), newSign = (p.getFlipSign() == 1) ? (-1) : (1);
            int effectXPos = (int)(p.getHitBox().x-PLAYER_HB_OFFSET_X-xLevelOffset)+(int)(newSign*27*SCALE)+newFlip;
            int effectYPos = (int)(p.getHitBox().y-PLAYER_HB_OFFSET_Y-yLevelOffset)-(int)(SCALE);
            int effectWid = newSign*(effects[EffectType.WALL_SLIDE.ordinal()][effectIndex].getWidth()+(int)(10*SCALE));
            int effectHei = effects[EffectType.WALL_SLIDE.ordinal()][effectIndex].getHeight()+(int)(50*SCALE);
            g.drawImage(effects[EffectType.WALL_SLIDE.ordinal()][effectIndex], effectXPos, effectYPos, effectWid, effectHei, null);
        }
    }

    public void setPlayerEffect(EffectType effect) {
        if (effect != entity.getEntityEffect()) effectIndex = 0;
        entity.setEntityEffect(effect);
    }

}
