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

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Elder bark-and-bone arachnid horror featuring a Cyan Foreleg Slash (ATTACK_1),
 * Piercing Mandible Thrust (ATTACK_2), Erupting Ground Spikes (SPELL_1), and a Cyan Arcane Web-Blast (SPELL_2).
 */
public class RootWeaver extends Enemy {

    private static final double GROUND_SPIKE_REACH = 2.5 * TILES_SIZE;

    public RootWeaver(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.ROOT_WEAVER);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
    }

    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        releaseCombatToken();
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
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
        releaseCombatToken();
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
        releaseCombatToken();
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE, RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1, ATTACK_2 -> meleeSlashAction(target);
            case SPELL_1 -> groundSpikesAction(target);
            case SPELL_2 -> webBoltAction(target);
            case HIT -> pushBack(pushDirection, levelData, 1.15, enemySpeed);
            default -> {}
        }
    }

    private void moveAction(int[][] levelData, Entity target) {
        if (canSeeEntity(levelData, target)) {
            double dist = Math.abs(target.getHitBox().getCenterX() - hitBox.getCenterX());
            double closeRange = definition.getAttackRange();

            if (cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                if (dist <= closeRange && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(rand.nextBoolean() ? Anim.ATTACK_1 : Anim.ATTACK_2);
                    return;
                }
                else if (dist > closeRange && dist <= GROUND_SPIKE_REACH && requestMeleeToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.SPELL_1);
                    return;
                }
                else if (dist > GROUND_SPIKE_REACH && dist <= definition.getSightRange() * 0.92 && rand.nextDouble() < 0.025 && requestRangedToken()) {
                    faceEntity(target);
                    setEnemyAction(Anim.SPELL_2);
                    return;
                }
            }

            moveWithCombatSpacing(levelData, target, definition.getScaledRunSpeed(), definition.getMinSpacing(), definition.getRetreatSpacing());
            return;
        }
        patrolStep(levelData);
    }

    private void meleeSlashAction(Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if ((animIndex == 3 || animIndex == 4) && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void groundSpikesAction(Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if (animIndex == 3 && !attackCheck) {
            EventBus.getInstance().publish(new ScreenShakeEvent(8, 4.5));
            checkEntityHit(attackBox, target);
        }
    }

    private void webBoltAction(Entity target) {
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
        double reach = (entityState == Anim.SPELL_1) ? GROUND_SPIKE_REACH : definition.getAttackBoxWidth();
        updateForwardAttackBox(reach);
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }
}