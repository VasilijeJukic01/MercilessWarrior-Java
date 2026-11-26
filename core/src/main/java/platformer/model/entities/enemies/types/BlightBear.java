package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.event.EventBus;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Heavy undead Blight Bear featuring hyper-armor poise, a paw maul (ATTACK_1),
 * a rear-up AoE ground slam with screen shake (ATTACK_2), a savage bite (ATTACK_3), and a feral pounce (SPELL_1).
 */
public class BlightBear extends Enemy {

    private static final double POUNCE_SPEED = 1.65 * SCALE;
    private static final double MAUL_LUNGE_SPEED = 0.95 * SCALE;

    private boolean hasHyperArmor = true;

    public BlightBear(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.BLIGHT_BEAR);
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
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> pawMaulAction(levelData, target);
            case ATTACK_2 -> groundSlamAction(target);
            case ATTACK_3 -> savageBiteAction(levelData, target);
            case SPELL_1 -> feralPounceAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 0.75, enemySpeed);
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
                    int roll = rand.nextInt(3);
                    setEnemyAction(roll == 0 ? Anim.ATTACK_1 : (roll == 1 ? Anim.ATTACK_2 : Anim.ATTACK_3));
                    return;
                }
                else if (dist > attackRange && dist <= TILES_SIZE * 3.0 && rand.nextDouble() < 0.045 && requestMeleeToken()) {
                    hasHyperArmor = true;
                    faceEntity(target);
                    setEnemyAction(Anim.SPELL_1);
                    return;
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void pawMaulAction(int[][] levelData, Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex == 4 || animIndex == 5) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, MAUL_LUNGE_SPEED);
            if (animIndex == 5 && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
    }

    private void groundSlamAction(Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex == 4 && !attackCheck) {
            EventBus.getInstance().publish(new ScreenShakeEvent(14, 7.5));
            checkEntityHit(attackBox, target);
        }
    }

    private void savageBiteAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex >= 3 && animIndex <= 5) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, MAUL_LUNGE_SPEED);
            if (animIndex == 4 && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
    }

    private void feralPounceAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex >= 2 && animIndex <= 4) {
            stepForwardClamped(levelData, target, POUNCE_SPEED);
            if ((animIndex == 3 || animIndex == 4) && !attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
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
        updateDynamicAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    private void updateDynamicAttackBox() {
        double width = (entityState == Anim.ATTACK_2) ? definition.getAttackBoxWidth() * 1.35 : definition.getAttackBoxWidth();
        updateForwardAttackBox(width);
    }

    @Override
    protected void reset() {
        super.reset();
        hasHyperArmor = true;
    }
}