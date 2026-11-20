package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.enemies.renderer.AfterimageTrail;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.*;

/**
 * Sir Dejanovic's void-corrupted Oath-Brothers.
 * Features a distinct moveset from regular Knights:
 * 1. Void Shadow-Step: Teleports behind the player when closing distance or retaliating from a hit.
 * 2. Void Crescent Wave (ATTACK_2): Conjures purple void fire and hurls a ranged crescent slash projectile.
 * 3. Relentless Void Cleave (ATTACK_1): Fast lunging greatsword strike with void afterimages.
 */
public class CorruptedKnight extends Enemy {

    private static final double SHADOW_STEP_SPEED = 1.45 * SCALE;
    private static final int SHADOW_STEP_WINDUP_TICKS = 18;

    private int shadowStepCooldown = 0;
    private int shadowStepWindup = 0;
    private boolean isShadowStepping = false;

    private final AfterimageTrail afterimageTrail = new AfterimageTrail(3, 3);

    public CorruptedKnight(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.CORRUPTED_KNIGHT);
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

        isShadowStepping = false;
        shadowStepWindup = 0;
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
        isShadowStepping = false;
        shadowStepWindup = 0;
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        if (shadowStepWindup > 0) {
            shadowStepWindup--;
            faceEntity(target);
            if (shadowStepWindup == 0) {
                if (performShadowStepBehind(levelData, target)) {
                    setEnemyAction(Anim.ATTACK_1);
                }
                else {
                    isShadowStepping = false;
                    releaseCombatToken();
                }
            }
            return;
        }

        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> voidCleaveAction(levelData, target);
            case ATTACK_2 -> voidCrescentWaveAction(target);
            case HIT -> hitAction(levelData);
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
                    isShadowStepping = true;
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }

                if (shadowStepCooldown == 0 && dist > attackRange * 1.35 && dist < TILES_SIZE * 4.5 && rand.nextDouble() < 0.015 && requestMeleeToken()) {
                    faceEntity(target);
                    setLocomotionState(Anim.IDLE);
                    isShadowStepping = true;
                    shadowStepWindup = SHADOW_STEP_WINDUP_TICKS;
                    return;
                }

                if (dist >= attackRange * 1.35 && dist <= definition.getSightRange() * 0.90 && rand.nextDouble() < 0.018 && requestRangedToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }

        patrolStep(levelData);
    }

    private void voidCleaveAction(int[][] levelData, Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 1 || animIndex == 3) && checkPlayerParry(target)) {
            isShadowStepping = false;
            attackCheck = true;
            return;
        }
        else if (animIndex == 3 || animIndex == 4) {
            if (animTick % 2 == 0) stepForwardClamped(levelData, target, SHADOW_STEP_SPEED);
            if (animIndex == 4 && !attackCheck) {
                checkEntityHit(attackBox, target);
            }
        }
        else if (animIndex == 7) {
            isShadowStepping = false;
        }
    }

    /**
     * Conjures purple void fire in hand (frames 1..3) and hurls a Void Crescent projectile on frame 4.
     */
    private void voidCrescentWaveAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            faceEntity(target);
        }
        else if ((animIndex == 1 || animIndex == 3) && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        else if (animIndex == 4 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createVoidSlash(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            if (attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
            }
            attackCheck = true;
        }
    }

    private void hitAction(int[][] levelData) {
        pushBack(pushDirection, levelData, 1.15, enemySpeed);
    }

    private boolean performShadowStepBehind(int[][] levelData, Entity target) {
        double targetCenterX = target.getHitBox().getCenterX();
        double side = (hitBox.getCenterX() < targetCenterX) ? 1.0 : -1.0;
        double destX = targetCenterX + (side * 1.45 * TILES_SIZE) - (hitBox.width / 2.0);
        Direction moveDir = (destX > hitBox.x) ? Direction.RIGHT : Direction.LEFT;

        if (canMoveHere(destX, hitBox.y, hitBox.width, hitBox.height, levelData)
                && isFloor(new Rectangle2D.Double(destX, hitBox.y, hitBox.width, hitBox.height), 0, levelData, moveDir)) {
            hitBox.x = destX;
            faceEntity(target);
            isShadowStepping = true;
            shadowStepCooldown = 640;
            Audio.getInstance().getAudioPlayer().playSound(Sound.DASH);
            return true;
        }
        return false;
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (shadowStepCooldown > 0) shadowStepCooldown--;
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateForwardAttackBox(definition.getAttackBoxWidth());
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity entity) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, entity);
    }

    public boolean isShadowStepping() {
        return isShadowStepping || shadowStepWindup > 0 || entityState == Anim.ATTACK_1;
    }

    public AfterimageTrail getAfterimageTrail() {
        return afterimageTrail;
    }

    @Override
    protected void reset() {
        super.reset();
        shadowStepCooldown = 0;
        shadowStepWindup = 0;
        isShadowStepping = false;
        afterimageTrail.clear();
    }
}