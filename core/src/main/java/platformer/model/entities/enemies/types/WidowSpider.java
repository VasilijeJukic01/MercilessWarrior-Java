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
import static platformer.physics.CollisionDetector.isEntityOnFloor;

/**
 * Agile cavern Widow Spider featuring a fast venomous fang strike (ATTACK_1)
 * and a ranged acid-glob spit (SPELL_1).
 */
public class WidowSpider extends Enemy {

    public WidowSpider(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.WIDOW_SPIDER);
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
            case ATTACK_1 -> fangBiteAction(levelData, target);
            case SPELL_1 -> acidSpitAction(target);
            case HIT -> pushBack(pushDirection, levelData, 1.5, enemySpeed);
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
                else if (dist > attackRange * 1.4 && dist <= definition.getSightRange() * 0.90 && rand.nextDouble() < 0.028 && requestRangedToken()) {
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

    private void fangBiteAction(int[][] levelData, Entity target) {
        if (animIndex <= 2 && !attackCheck && checkPlayerParry(target)) {
            attackCheck = true;
            return;
        }
        if (animIndex == 3 || animIndex == 4) {
            if (animTick % 2 == 0) {
                stepForwardClamped(levelData, target, 1.15 * SCALE);
                updateForwardAttackBox(definition.getAttackBoxWidth());
            }
            if (!attackCheck && attackBox.intersects(target.getHitBox())) {
                checkEntityHit(attackBox, target);
                attackCheck = true;
            }
        }
    }

    private void acidSpitAction(Entity target) {
        if (animIndex == 0) {
            faceEntity(target);
        }
        if (animIndex == 4 && !attackCheck) {
            faceEntity(target);
            ProjectileFactory.createVenomSpit(this);
            Audio.getInstance().getAudioPlayer().playSound(Sound.ARROW);
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
}