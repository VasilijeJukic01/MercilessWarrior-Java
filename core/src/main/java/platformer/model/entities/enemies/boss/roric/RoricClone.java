package platformer.model.entities.enemies.boss.roric;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.event.EventBus;
import platformer.event.events.roric.RoricCloneEvent;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.enemies.boss.Roric;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.*;


/**
 * A temporary clone of Roric used for a special attack. It has limited AI and is designed to perform one action before disappearing.
 */
public class RoricClone extends Roric {

    private boolean attackStarted = false;
    private int shotCounter = 0;
    private static final int SHOTS_TO_FIRE = 2;

    public RoricClone(int xPos, int yPos) {
        super(xPos, yPos);
    }

    @Override
    public void update(int[][] levelData, Player player, SpellManager spellManager, EnemyManager enemyManager, ProjectileManager projectileManager, BossInterface bossInterface) {
        if (!attackStarted) {
            setEnemyAction(Anim.ATTACK_2);
            attackStarted = true;
        }

        updateAnimation();
        updateAttackBox();

        if (getEnemyAction() == Anim.ATTACK_2) {
            if (getAnimIndex() == 9 && !isAttackCheck()) {
                projectileManager.activateRoricArrow(this, 1.0);
                setAttackCheck(true);
            }
        }
    }

    /**
     * A simple method to aim the clone at the player.
     * This should be called right after the clone is spawned.
     * @param player The player to aim at.
     */
    public void aimAtPlayer(Player player) {
        if (player.getHitBox().getCenterX() < this.getHitBox().getCenterX()) {
            setDirection(Direction.LEFT);
        } else {
            setDirection(Direction.RIGHT);
        }
    }

    /**
     * Ensures the clone deactivates itself after its attack.
     */
    protected void finishAnimation() {
        if (getEnemyAction() == Anim.ATTACK_2) {
            shotCounter++;
            setAttackCheck(false);
            if (shotCounter < SHOTS_TO_FIRE) setAnimIndex(0);
            else {
                this.alive = false;
                EventBus.getInstance().publish(new RoricCloneEvent(new Point((int)getHitBox().getCenterX(), (int)getHitBox().getCenterY())));
            }
        }
        else this.alive = false;
    }

    @Override
    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= SpriteManager.getInstance().getAnimFrames(getEnemyType(), entityState)) {
                this.finishAnimation();
            }
        }
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}