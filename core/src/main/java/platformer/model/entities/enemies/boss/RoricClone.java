package platformer.model.entities.enemies.boss;

import platformer.animation.Anim;
import platformer.model.entities.Direction;
import platformer.model.entities.enemies.EnemyManager;
import platformer.model.entities.player.Player;
import platformer.model.gameObjects.ObjectManager;
import platformer.model.gameObjects.projectiles.ProjectileManager;
import platformer.model.spells.SpellManager;
import platformer.ui.overlays.hud.BossInterface;

import java.awt.image.BufferedImage;


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
    public void update(BufferedImage[][] animations, int[][] levelData, Player player, SpellManager spellManager, EnemyManager enemyManager, ObjectManager objectManager, ProjectileManager projectileManager, BossInterface bossInterface) {
        if (!attackStarted) {
            setEnemyAction(Anim.ATTACK_2);
            attackStarted = true;
        }

        updateAnimation(animations);
        updateAttackBox();

        if (getEnemyAction() == Anim.ATTACK_2) {
            if (getAnimIndex() == 9 && !isAttackCheck()) {
                projectileManager.activateRoricArrow(this);
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
            else this.alive = false;
        }
        else this.alive = false;
    }

    /**
     * FIX: Override the parent's animation logic to ensure the clone's own
     * finishAnimation() method is called.
     */
    @Override
    protected void updateAnimation(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                this.finishAnimation();
            }
        }
    }

    @Override
    public boolean isVisible() {
        return true;
    }
}