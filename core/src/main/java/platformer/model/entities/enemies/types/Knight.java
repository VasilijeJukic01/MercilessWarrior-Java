package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Elite armored enemy featuring hyper-armor poise during attack windups, a fast greatsword cleave,
 * and an aggressive mid-range arcane surge gap-closer that frequently chains as a 2-hit combo.
 */
public class Knight extends Enemy {

    private static final double CLEAVE_LUNGE_SPEED = 1.25 * SCALE;
    private static final double ARCANE_SURGE_SPEED = 1.70 * SCALE;

    private boolean hasHyperArmor = true;
    private boolean chainIntoArcaneSlash = false;

    public Knight(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.KNIGHT);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
            return true;
        }
        if (isAttacking() && hasHyperArmor && freezeTick == 0) {
            hasHyperArmor = false;
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
            return true;
        }
        chainIntoArcaneSlash = false;
        releaseCombatToken();
        setEnemyAction(Anim.HIT);
        if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        enemySpeed = definition.getScaledWalkSpeed();
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive) return;
        currentHealth -= damage;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else if (!isAttacking()) {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
        enemySpeed = 0;
    }

    private void checkDeath() {
        chainIntoArcaneSlash = false;
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> cleaveAttackAction(levelData, target);
            case ATTACK_2 -> arcaneSlashAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.0, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double attackRange = definition.getAttackRange();

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= attackRange && requestMeleeToken()) {
                    hasHyperArmor = true;
                    faceEntity(target);
                    chainIntoArcaneSlash = rand.nextDouble() < 0.48;
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist > attackRange && dist <= attackRange * 1.75 && rand.nextDouble() < 0.08 && requestMeleeToken()) {
                    hasHyperArmor = true;
                    faceEntity(target);
                    chainIntoArcaneSlash = false;
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
            }
            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void cleaveAttackAction(int[][] levelData, Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            chainIntoArcaneSlash = false;
            attackCheck = true;
            return;
        }
        if (animIndex == 3 || animIndex == 4) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, CLEAVE_LUNGE_SPEED);
            if (animIndex == 4 && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
        else if (animIndex == 7 && chainIntoArcaneSlash) {
            chainIntoArcaneSlash = false;
            hasHyperArmor = true;
            faceEntity(target);
            setEnemyAction(Anim.ATTACK_2);
        }
    }

    private void arcaneSlashAction(int[][] levelData, Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex >= 3 && animIndex <= 5) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, ARCANE_SURGE_SPEED);
            if ((animIndex == 4 || animIndex == 5) && !attackCheck) {
                double damage = getEnemyType().getDamage() * 1.15;
                if (attackBox.intersects(target.getHitBox())) {
                    if (target instanceof Player player) {
                        if (checkPlayerParry(player)) {
                            hasHyperArmor = false;
                            attackCheck = true;
                            return;
                        }
                        if (!player.checkAction(PlayerAction.CAN_BLOCK)) {
                            player.changeHealth(-damage, this);
                        }
                    }
                }
                attackCheck = true;
            }
        }
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) updatePushOffset();
    }

    public void updateMove(int[][] levelData, Entity entity) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, entity);
    }

    private void updateAttackBox() {
        double width = (entityState == Anim.ATTACK_2) ? definition.getAttackBoxWidth() * 1.25 : definition.getAttackBoxWidth();
        updateForwardAttackBox(width);
    }

    @Override
    protected void reset() {
        super.reset();
        hasHyperArmor = true;
        chainIntoArcaneSlash = false;
    }
}