package platformer.model.entities.effects;

import platformer.animation.Animation;
import platformer.model.entities.Entity;
import platformer.model.entities.effects.particles.DustParticle;
import platformer.model.entities.effects.particles.DustType;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static platformer.constants.AnimConstants.DUST1_H;
import static platformer.constants.AnimConstants.DUST1_W;
import static platformer.constants.Constants.*;
import static platformer.constants.FilePaths.DUST_SHEET_1;

/**
 * This class is controlling the effects related to the player in the game.
 * It handles the loading, updating, and rendering of these effects.
 */
public class PlayerEffectController {

    private final Entity entity;

    private BufferedImage[][] effects;
    private final int animSpeed = 20;
    private int animTick = 0, effectIndex = 0;

    private final List<DustParticle> dust = new ArrayList<>();
    private final Random rand = new Random();

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

        dust.removeIf(p -> !p.isActive());
        dust.forEach(DustParticle::update);
    }

    public void render(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderWallSlide(g, xLevelOffset, yLevelOffset);

        try {
            dust.forEach(d -> d.render(g, xLevelOffset, yLevelOffset));
        } catch (Exception ignored) { }
    }

    private void renderWallSlide(Graphics g, int xLevelOffset, int yLevelOffset) {
        if (!(entity instanceof Player)) return;
        if (entity.getEntityEffect() != EffectType.WALL_SLIDE) return;
        Player p = (Player) entity;
        boolean onWall = p.checkAction(PlayerAction.ON_WALL);
        if (onWall) {
            int flip = (p.getFlipSign() == 1) ? (-1) : (1);
            int wid = (flip == 1) ? (0) : (int)((p.getHitBox().width + (28 * SCALE)));
            int xPos = (int)(p.getHitBox().x - DUST1_OFFSET_X) + wid - xLevelOffset;
            int yPos = (int)(p.getHitBox().y - DUST1_OFFSET_Y) - yLevelOffset;
            g.drawImage(effects[EffectType.WALL_SLIDE.ordinal()][effectIndex], xPos, yPos, (int)(flip*DUST1_WID), (int)DUST1_HEI, null);
        }
    }

    /**
     * Spawns a number of dust particles of a specific type at a given location.
     * @param x The x-coordinate to spawn at.
     * @param y The y-coordinate to spawn at.
     * @param count The number of particles to spawn.
     * @param type The type of dust to create (IMPACT or RUNNING).
     */
    public void spawnDustParticles(double x, double y, int count, DustType type, int playerFlipSign) {
        for (int i = 0; i < count; i++) {
            int size;
            double yOffset = 0;
            if (type == DustType.IMPACT) size = (int) ((rand.nextInt(6) + 3) * SCALE);
            else if (type == DustType.RUNNING) size = (int) ((rand.nextInt(5) + 4) * SCALE);
            else {
                yOffset = (rand.nextDouble() - 0.5) * (entity.getHitBox().height * 0.8);
                size = (int) ((rand.nextInt(4) + 4) * SCALE);
            }

            dust.add(new DustParticle((int) x, (int) (y + yOffset), size, type, playerFlipSign));
        }
    }

    public void setPlayerEffect(EffectType effect) {
        if (effect != entity.getEntityEffect()) effectIndex = 0;
        entity.setEntityEffect(effect);
    }

}
