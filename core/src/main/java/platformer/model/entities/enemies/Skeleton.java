package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

public class Skeleton extends Enemy {

    private int attackOffset;

    // Physics
    private final double gravity = 0.035 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    public Skeleton(int xPos, int yPos) {
        super(xPos, yPos, SKELETON_WIDTH, SKELETON_HEIGHT, EnemyType.SKELETON, 25);
        initHitBox(SKELETON_HB_WID, SKELETON_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, SKELETON_AB_WID, SKELETON_AB_HEI);
        this.attackOffset =  (int)(20 * SCALE);
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
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        enemySpeed = ENEMY_SPEED_SLOW;
        return true;
    }

    @Override
    public void spellHit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            checkDeath();
        }
        else {
            if (entityState == Anim.HIT) setEnemyActionNoReset(Anim.HIT);
            else setEnemyAction(Anim.HIT);
        }
        pushOffsetDirection = Direction.DOWN;
        pushOffset = 0;
        enemySpeed = 0;
    }

    private void blockAttack() {
        Random rand = new Random();
        double x = rand.nextDouble();
        if (x < 0.2) {
            entityState = Anim.BLOCK;
            Audio.getInstance().getAudioPlayer().playBlockSound("Enemy");
        }
    }

    private void checkDeath() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.SKELETON_DEATH_1);
        setEnemyAction(Anim.DEATH);
    }

    // Skeleton Core
    private void updateBehavior(int[][] levelData, Entity target) {
        switch (entityState) {
            case IDLE:
               idleAction();
               break;
            case RUN:
            case WALK:
                moveAction(levelData, target);
                break;
            case ATTACK_1:
                attackAction(target);
                break;
            case HIT:
                hitAction(levelData);
            default: break;
        }
    }

    // Behavior
    private void idleAction() {
        setEnemyAction(Anim.WALK);
        animSpeed = 25;
    }

    private void moveAction(int[][] levelData, Entity entity) {
        if (canSeeEntity(levelData, entity)) directToEntity(entity);
        if (canSeeEntity(levelData, entity) && isEntityCloseForAttack(entity)) {
            setEnemyAction(Anim.ATTACK_1);
            animSpeed = 23;
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
        boolean block = false;
        boolean canBlock = false;
        Player player = null;

        if (target instanceof Player) {
            player = (Player) target;
            block = player.checkAction(PlayerAction.BLOCK);
            canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
        }

        if (animIndex == 0) {
            attackCheck = false;
        }
        else if ((animIndex == 1 || animIndex == 2) && block && !canBlock) {
            if (player != null && ((player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1))) {
                player.addAction(PlayerAction.CAN_BLOCK);
                Audio.getInstance().getAudioPlayer().playBlockSound("Player");
            }
        }
        else if (animIndex == 3 && !attackCheck) {
            checkEntityHit(attackBox, target);
        }
    }

    private void hitAction(int[][] levelData) {
        pushBack(pushDirection, levelData, 1.5, enemySpeed);
        updatePushOffset();
    }

    // Update
    @Override
    public void update(int[][] levelData, Player player, Entity follower) {
        if (freezeTick > 0) {
            freezeTick--;
            return;
        }
        Entity target = getCloseTarget(player, follower);
        updateMove(levelData, target);
        updateAnimation();
        updateAttackBox();
    }

    private void updateMove(int[][] levelData, Entity target) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, target);
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }

    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

}
