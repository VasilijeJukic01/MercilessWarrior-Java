package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Elite horned Crimson Skeleton warrior.
 * Features a Spiked Shield-Charge Lunge (Row 8 -> ATTACK_1), an instant Counter-Riposte on block
 * that unleashes a dark-violet slash wave, and a one-time Undying Reassembly when felled by physical strikes.
 */
public class CrimsonSkeleton extends Enemy {

    private static final double SHIELD_CHARGE_SPEED = 1.65 * SCALE;

    private boolean counterRiposteQueued = false;
    private boolean shieldCharging = false;
    private int chargeTicks = 0;
    private boolean hasReassembled = false;

    public CrimsonSkeleton(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.CRIMSON_SKELETON);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableBlock && !shieldCharging && rand.nextDouble() < 0.35) {
            setEnemyAction(Anim.BLOCK);
            counterRiposteQueued = true;
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
            return true;
        }

        currentHealth -= damage;
        shieldCharging = false;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();

        if (currentHealth <= 0) {
            if (!hasReassembled && enableBlock) {
                hasReassembled = true;
                currentHealth = maxHealth * 0.40;
                setEnemyAction(Anim.BLOCK);
                counterRiposteQueued = true;
                Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
                return true;
            }
            checkDeath();
        }
        else {
            setEnemyAction(Anim.HIT);
            if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        }
        enemySpeed = definition.getScaledWalkSpeed();
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive) return;
        currentHealth -= damage;
        shieldCharging = false;
        counterRiposteQueued = false;
        enemySpeed = 0;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void checkDeath() {
        shieldCharging = false;
        counterRiposteQueued = false;
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case BLOCK -> blockRiposteAction(target);
            case JUMP -> shieldChargeAction(levelData, target);
            case ATTACK_1 -> cleaveAttackAction(levelData, target);
            case HIT -> pushBack(pushDirection, levelData, 1.1, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double attackRange = definition.getAttackRange();

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= attackRange && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist > attackRange * 1.2 && dist <= TILES_SIZE * 3.2 && rand.nextDouble() < 0.045 && requestMeleeToken()) {
                    faceEntity(target);
                    shieldCharging = true;
                    chargeTicks = 24;
                    setEnemyAction(Anim.JUMP);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
                    return;
                }
            }
            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void blockRiposteAction(Entity target) {
        faceEntity(target);
        if (counterRiposteQueued && animIndex >= 3) {
            counterRiposteQueued = false;
            setEnemyAction(Anim.ATTACK_1);
            this.animSpeed = 13;
        }
    }

    private void shieldChargeAction(int[][] levelData, Entity target) {
        if (chargeTicks > 0) {
            chargeTicks--;
            stepForwardClamped(levelData, target, SHIELD_CHARGE_SPEED);
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            if (dist <= definition.getAttackRange() * 0.95 || chargeTicks == 0) {
                shieldCharging = false;
                faceEntity(target);
                setEnemyAction(Anim.ATTACK_1);
            }
        }
        else {
            shieldCharging = false;
            setEnemyAction(Anim.ATTACK_1);
        }
    }

    private void cleaveAttackAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if (animIndex == 2 || animIndex == 3) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, 0.95 * SCALE);
            if (animIndex == 3 && !attackCheck) {
                checkEntityHit(attackBox, target);
                if (hasReassembled || rand.nextDouble() < 0.40) {
                    ProjectileFactory.createVoidSlash(this);
                    Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
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
        updateForwardAttackBox(definition.getAttackBoxWidth());
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    @Override
    protected void reset() {
        super.reset();
        counterRiposteQueued = false;
        shieldCharging = false;
        chargeTicks = 0;
        hasReassembled = false;
    }
}