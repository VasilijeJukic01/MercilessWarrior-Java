package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

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
    public void hit(double damage, boolean enableBlock, boolean hitSound) {
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else setEnemyAction(Anim.HIT);
        enemySpeed = ENEMY_SPEED_SLOW;
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
        // TODO: Find audio
        setEnemyAction(Anim.DEATH);
    }

    // Skeleton Core
    private void updateBehavior(int[][] levelData, Player player) {
        switch (entityState) {
            case IDLE:
                idleAction();
                break;
            case RUN:
            case WALK:
                moveAction(levelData, player);
                break;
            case ATTACK_1:
            case ATTACK_2:
                attackAction(player);
                break;
            default: break;
        }
    }

    // Behavior
    private void idleAction() {
        setEnemyAction(Anim.WALK);
        animSpeed = 25;
    }

    private void moveAction(int[][] levelData, Player player) {
        if (canSeePlayer(levelData, player)) directToPlayer(player);
        if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player)) {
            Random rand = new Random();
            boolean x = rand.nextBoolean();
            setEnemyAction(x ? Anim.ATTACK_1 : Anim.ATTACK_2);
            animSpeed = 20;
        }
        double enemyXSpeed = (direction == Direction.LEFT) ? -enemySpeed : enemySpeed;
        if (Utils.getInstance().canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
            if (Utils.getInstance().isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                hitBox.x += enemyXSpeed;
                return;
            }
        }
        changeDirection();
    }

    private void attackAction(Player player) {
        boolean block = player.checkAction(PlayerAction.BLOCK);
        if (animIndex == 0) {
            attackCheck = false;
        }
        else if (entityState == Anim.ATTACK_1 && (animIndex == 1 || animIndex == 2) && block) {
            if ((player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1)) {
                player.addAction(PlayerAction.CAN_BLOCK);
                Audio.getInstance().getAudioPlayer().playBlockSound("Player");
            }
        }
        else if (animIndex == 4 && !attackCheck) checkPlayerHit(attackBox, player);
    }

    // Update
    @Override
    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {
        updateMove(levelData, player);
        updateAnimation(animations);
        updateAttackBox();
    }

    public void updateMove(int[][] levelData, Player player) {
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) updateInAir(levelData, gravity, collisionFallSpeed);
        else updateBehavior(levelData, player);
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
