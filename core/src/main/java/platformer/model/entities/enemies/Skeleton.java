package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;

import java.awt.geom.Rectangle2D;

import static platformer.constants.Constants.SCALE;
import static platformer.physics.CollisionDetector.*;

public class Skeleton extends Enemy {

    private int attackOffset;

    public Skeleton(int xPos, int yPos) {
        super(xPos, yPos, EnemyType.SKELETON);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos - 1, definition.getAttackBoxWidth(), definition.getAttackBoxHeight());
        this.attackOffset = (int) (20 * SCALE);
    }

    // Attack
    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        if (enableBlock) {
            blockAttack();
            if (entityState == Anim.BLOCK) return true;
        }
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else setEnemyAction(Anim.HIT);
        if (pushOffset == 0) pushOffsetDirection = Direction.UP;
        enemySpeed = definition.getScaledWalkSpeed();
        return true;
    }

    @Override
    public void spellHit(double damage) {
        if (entityState == Anim.DEATH || !alive) return;
        currentHealth -= damage;
        enemySpeed = 0;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
    }

    private void blockAttack() {
        if (rand.nextDouble() < 0.2) {
            entityState = Anim.BLOCK;
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
        }
    }

    private void checkDeath() {
        playDeathSound();
        setEnemyAction(Anim.DEATH);
    }

    // Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE -> idleAction();
            case RUN, WALK -> moveAction(levelData, target);
            case ATTACK_1 -> attackAction(target);
            case HIT -> hitAction(levelData);
            default -> {}
        }
    }

    // Behavior
    private void idleAction() {
        setEnemyAction(Anim.WALK);
    }

    private void moveAction(int[][] levelData, Entity entity) {
        if (canSeeEntity(levelData, entity)) directToEntity(entity);
        if (canSeeEntity(levelData, entity) && isEntityCloseForAttack(entity) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            setEnemyAction(Anim.ATTACK_1);
        }
        double enemyXSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            if (isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                hitBox.x += enemyXSpeed;
                return;
            }
        }
        changeDirection();
    }

    private void attackAction(Entity target) {
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 1 || animIndex == 2) && checkPlayerParry(target)) {
            attackCheck = true;
        }
        else if (animIndex == 3 && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void hitAction(int[][] levelData) {
        pushBack(pushDirection, levelData, 1.5, enemySpeed);
    }

    // Update
    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (handleStunTick(levelData)) return;
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
        if (pushOffset < 0 || pushOffsetDirection == Direction.UP) {
            updatePushOffset();
        }
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
        else updateBehavior(levelData, target);
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }
}
