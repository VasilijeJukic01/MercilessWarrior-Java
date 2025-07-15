package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Song;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

// TODO:
public class Roric extends Enemy {

    private int attackOffset;

    private boolean start;

    public Roric(int xPos, int yPos) {
        super(xPos, yPos, RORIC_WIDTH, RORIC_HEIGHT, EnemyType.RORIC, 16);
        super.setDirection(Direction.LEFT);
        initHitBox(RORIC_HB_WID, RORIC_HB_HEI);
        hitBox.x += RORIC_HB_OFFSET_X;
        hitBox.y += RORIC_HB_OFFSET_Y;
        initAttackBox();
        super.cooldown = new double[1];
    }

    // Init
    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos - (15 * SCALE), yPos + (15 * SCALE), RORIC_AB_WID, RORIC_AB_HEI);
        this.attackOffset = (int)(33 * SCALE);
    }

    private void startFight(ObjectManager objectManager) {
        if (!start) {
            setStart(true);
        }
    }

    // Core
    private void updateBehavior(int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager) {
        if (cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        switch (entityState) {
            case IDLE:
                idleAction(levelData, player, objectManager); break;
            default: break;
        }
    }

    @Override
    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {

    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, ObjectManager objectManager, BossInterface bossInterface) {
        updateAnimation(animations);
        if (!bossInterface.isActive() && start) bossInterface.setActive(true);
        else if (bossInterface.isActive() && !start) bossInterface.setActive(false);
    }

    @Override
    public boolean hit(double damage, boolean special, boolean hitSound) {
        return false;
    }

    @Override
    public void spellHit(double damage) {

    }

    // Behavior
    private void idleAction(int[][] levelData, Player player, ObjectManager objectManager) {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            // TODO: Later
        }
    }

    // Update animation
    @Override
    protected void updateAnimation(BufferedImage[][] animations) {
        if (cooldown != null) {             // Pre-Attack cooldown check
            coolDownTickUpdate();
            if ((entityState != Anim.IDLE) && cooldown[Cooldown.ATTACK.ordinal()] != 0) return;
        }
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) finishAnimation();
        }
    }

    private void finishAnimation() {
        animIndex = 0;
    }

    public void reset() {
        super.reset();
        setDirection(Direction.LEFT);
        setEnemyAction(Anim.IDLE);
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    // Setters
    public void setStart(boolean start) {
        this.start = start;
        if (start) Audio.getInstance().getAudioPlayer().playSong(Song.BOSS_1);
    }

    public void setAttackCooldown(double value) {
        cooldown[Cooldown.ATTACK.ordinal()] = value;
    }
}
