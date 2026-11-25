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
 * Spectral apparition featuring three distinct attacks and a telegraphed two-stage Phase-Blink:
 * - ATTACK_1: Forward spectral chain-whip lash.
 * - ATTACK_2: 360-degree AoE Soul Vortex when the player crowds too close.
 * - SPELL_1 : Ranged homing Spectral Soul Bolt at long distance.
 * - Phase-Blink: Fades out over a 26-tick windup, repositions, and materializes over a 24-tick recovery window before attacking.
 */
public class Wraith extends Enemy {

    private static final int BLINK_FADEOUT_TICKS = 26;
    private static final int BLINK_RECOVERY_TICKS = 24;
    private static final int BLINK_COOLDOWN_TICKS = 680;

    private int blinkCooldown = 0;
    private int blinkWindupTick = 0;
    private int blinkRecoveryTick = 0;
    private double pendingBlinkX = 0;
    private float phaseAlpha = 1.0f;
    private double hoverPhase = Math.random() * Math.PI * 2.0;

    private final AfterimageTrail afterimageTrail = new AfterimageTrail(3, 4);

    public Wraith(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.WRAITH);
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
        }
        else {
            cancelPendingBlink();
            releaseCombatToken();
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
        releaseCombatToken();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            cancelPendingBlink();
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
        enemySpeed = 0;
    }

    private void cancelPendingBlink() {
        blinkWindupTick = 0;
        blinkRecoveryTick = 0;
        phaseAlpha = 1.0f;
    }

    private void checkDeath() {
        cancelPendingBlink();
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        if (updatePhaseBlinkSequence(target)) return;

        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1, ATTACK_2 -> meleeAttackAction(target);
            case SPELL_1 -> spellBoltAction(target);
            case HIT -> hitAction(levelData, target);
            default -> {}
        }
    }

    /**
     * Executes the two-stage Phase-Blink (fade-out windup followed by arrival recovery) so the player has time to read and react.
     */
    private boolean updatePhaseBlinkSequence(Entity target) {
        if (blinkWindupTick > 0) {
            blinkWindupTick--;
            phaseAlpha = Math.max(0.15f, (float) blinkWindupTick / BLINK_FADEOUT_TICKS);
            if (blinkWindupTick == 0) {
                hitBox.x = pendingBlinkX;
                faceEntity(target);
                Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_REVEAL);
                blinkRecoveryTick = BLINK_RECOVERY_TICKS;
                setLocomotionState(Anim.IDLE);
            }
            return true;
        }

        if (blinkRecoveryTick > 0) {
            blinkRecoveryTick--;
            phaseAlpha = Math.min(1.0f, 0.20f + 0.80f * (1.0f - (float) blinkRecoveryTick / BLINK_RECOVERY_TICKS));
            faceEntity(target);
            return true;
        }
        return false;
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double attackRange = definition.getAttackRange();
            double whipRange = attackRange * 1.25;
            double soulBoltMaxRange = definition.getSightRange() * 0.95;

            if (blinkCooldown == 0 && cooldown[Cooldown.ATTACK.ordinal()] == 0
                    && dist > whipRange * 1.2 && dist < soulBoltMaxRange * 0.85
                    && rand.nextDouble() < 0.007 && requestMeleeToken()) {
                if (startPhaseBlink(levelData, target, true)) {
                    return;
                }
            }

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= attackRange * 0.72 && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_2);
                    return;
                }
                else if (dist <= whipRange && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist >= whipRange * 1.25 && dist <= soulBoltMaxRange && rand.nextDouble() < 0.025 && requestRangedToken()) {
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

    private void meleeAttackAction(Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
        }
        else if ((animIndex == 4 || animIndex == 5) && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void spellBoltAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
            faceEntity(target);
        }
        else if (animIndex == 4 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createWraithBolt(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            attackCheck = true;
        }
    }

    private void hitAction(int[][] levelData, Entity target) {
        pushBack(pushDirection, levelData, 1.6, enemySpeed);
        if (enemySpeed > 0 && animIndex == 3 && blinkCooldown == 0 && rand.nextDouble() < 0.20) {
            startPhaseBlink(levelData, target, false);
        }
    }

    /**
     * Begins a telegraphed Phase-Blink either behind the target at whip distance or away to reset spacing.
     */
    private boolean startPhaseBlink(int[][] levelData, Entity target, boolean behindTarget) {
        double targetCenterX = target.getHitBox().getCenterX();
        double dirFromTarget = (hitBox.getCenterX() < targetCenterX) ? -1.0 : 1.0;
        double desiredOffset = behindTarget ? (-dirFromTarget * 1.85 * TILES_SIZE) : (dirFromTarget * 3.5 * TILES_SIZE);
        double newX = targetCenterX + desiredOffset - (hitBox.width / 2.0);
        Direction moveDir = (newX > hitBox.x) ? Direction.RIGHT : Direction.LEFT;

        if (canMoveHere(newX, hitBox.y, hitBox.width, hitBox.height, levelData)
                && isFloor(new Rectangle2D.Double(newX, hitBox.y, hitBox.width, hitBox.height), 0, levelData, moveDir)) {
            this.pendingBlinkX = newX;
            this.blinkWindupTick = BLINK_FADEOUT_TICKS;
            this.blinkCooldown = BLINK_COOLDOWN_TICKS;
            setLocomotionState(Anim.IDLE);
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_HIDE);
            return true;
        }
        return false;
    }

    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        hoverPhase += 0.045;
        if (blinkCooldown > 0) blinkCooldown--;
        if (blinkWindupTick == 0 && blinkRecoveryTick == 0 && phaseAlpha < 1.0f) {
            phaseAlpha = Math.min(1.0f, phaseAlpha + 0.03f);
        }

        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    public void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    private void updateAttackBox() {
        int baseWidth = definition.getAttackBoxWidth();
        if (entityState == Anim.ATTACK_1) {
            updateForwardAttackBox(baseWidth * 0.90);
        }
        else {
            attackBox.width = (int) (baseWidth * 1.10);
            attackBox.x = hitBox.getCenterX() - (attackBox.width / 2.0);
        }
        attackBox.y = hitBox.y - (4 * SCALE);
    }

    public int getHoverOffsetY() {
        if (entityState == Anim.DEATH) return 0;
        return (int) Math.round(Math.sin(hoverPhase) * (3.0 * SCALE));
    }

    public float getPhaseAlpha() {
        return phaseAlpha;
    }

    public AfterimageTrail getAfterimageTrail() {
        return afterimageTrail;
    }

    @Override
    protected void reset() {
        super.reset();
        blinkCooldown = 0;
        cancelPendingBlink();
        afterimageTrail.clear();
    }
}