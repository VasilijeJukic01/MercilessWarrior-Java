package platformer.model.entities.enemies.types;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.event.EventBus;
import platformer.event.events.effects.ScreenShakeEvent;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.Enemy;
import platformer.model.entities.enemies.EnemyType;
import platformer.model.entities.player.Player;
import platformer.model.projectiles.ProjectileFactory;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Undead cavalry knight mounted on a skeletal warhorse.
 * Features a high-speed Charging Lance Thrust (ATTACK_1), a Sweeping Lance Cleave (ATTACK_2)
 * that chains into a Warhorse Rear-Up Hoof Stomp (ATTACK_3), and a ranged Runic Lance Bolt (SPELL_1).
 */
public class DeathRider extends Enemy {

    private static final double CHARGE_THRUST_SPEED = 1.85 * SCALE;

    private boolean hasHyperArmor = true;
    private boolean chainIntoHoofStomp = false;

    public DeathRider(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.DEATH_RIDER);
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
        chainIntoHoofStomp = false;
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
        chainIntoHoofStomp = false;
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> lanceThrustAction(levelData, target);
            case ATTACK_2 -> lanceCleaveAction(levelData, target);
            case ATTACK_3 -> hoofStompAction(target);
            case SPELL_1 -> runicWaveAction(target);
            case HIT -> pushBack(pushDirection, levelData, 0.85, enemySpeed);
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
                    if (rand.nextBoolean()) {
                        chainIntoHoofStomp = true;
                        setEnemyAction(Anim.ATTACK_2);
                    }
                    else {
                        chainIntoHoofStomp = false;
                        setEnemyAction(Anim.ATTACK_1);
                    }
                    return;
                }
                else if (dist > attackRange && dist <= attackRange * 1.9 && rand.nextDouble() < 0.055 && requestMeleeToken()) {
                    hasHyperArmor = true;
                    faceEntity(target);
                    chainIntoHoofStomp = false;
                    setEnemyAction(Anim.ATTACK_1);
                    return;
                }
                else if (dist > attackRange * 1.9 && dist <= definition.getSightRange() * 0.92 && rand.nextDouble() < 0.022 && requestRangedToken()) {
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

    private void lanceThrustAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex >= 3 && animIndex <= 5) {
            stepForwardClamped(levelData, target, CHARGE_THRUST_SPEED);
            updateForwardAttackBox(definition.getAttackBoxWidth());
            if (!attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
                attackCheck = true;
            }
        }
    }

    private void lanceCleaveAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            chainIntoHoofStomp = false;
            attackCheck = true;
            return;
        }
        if (animIndex == 3 || animIndex == 4) {
            if (animTick % 2 == 0) {
                stepForwardClamped(levelData, target, 1.1 * SCALE);
                updateForwardAttackBox(definition.getAttackBoxWidth());
            }
            if (!attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
                attackCheck = true;
            }
        }
        else if (animIndex == 7 && chainIntoHoofStomp) {
            chainIntoHoofStomp = false;
            hasHyperArmor = true;
            faceEntity(target);
            setEnemyAction(Anim.ATTACK_3);
        }
    }

    private void hoofStompAction(Entity target) {
        if (animIndex <= 3 && !attackCheck && checkPlayerParry(target)) {
            hasHyperArmor = false;
            attackCheck = true;
            return;
        }
        if (animIndex == 4 && !attackCheck) {
            EventBus.getInstance().publish(new ScreenShakeEvent(12, 6.5));
            checkEntityHit(attackBox, target);
        }
    }

    private void runicWaveAction(Entity target) {
        if (animIndex == 0) {
            faceEntity(target);
        }
        if (animIndex == 3 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createCyanRunicBolt(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.FIREBALL);
            attackCheck = true;
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
        hasHyperArmor = true;
        chainIntoHoofStomp = false;
    }
}