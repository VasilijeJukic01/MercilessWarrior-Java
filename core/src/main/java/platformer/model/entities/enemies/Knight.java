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

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.*;

public class Knight extends Enemy {

    private int attackOffset;

    // Physics
    private final double gravity = 0.020 * SCALE;
    private final double collisionFallSpeed = 0.6 * SCALE;

    public Knight(int xPos, int yPos) {
        super(xPos, yPos, KNIGHT_WIDTH, KNIGHT_HEIGHT, EnemyType.KNIGHT, 25);
        initHitBox(KNIGHT_HB_WID, KNIGHT_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, KNIGHT_AB_WID, KNIGHT_AB_HEI);
        this.attackOffset =  (int)(20 * SCALE);
    }

    // Attack
    @Override
    public boolean hit(double damage, boolean enableBlock, boolean hitSound) {
        if (entityState == Anim.DEATH || !alive) return false;
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else setEnemyAction(Anim.HIT);
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
        enemySpeed = 0;
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
            animSpeed = 20;
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
        else if (entityState == Anim.ATTACK_1 && (animIndex == 1 || animIndex == 2) && block && !canBlock) {
            if (player != null && ((player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1))) {
                player.addAction(PlayerAction.CAN_BLOCK);
                Audio.getInstance().getAudioPlayer().playBlockSound("Player");
            }
        }
        else if (animIndex == 4 && !attackCheck) checkEntityHit(attackBox, target);
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

    public void updateMove(int[][] levelData, Entity entity) {
        if (!isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, entity);
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
