package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Wraith extends Enemy {

    private int attackOffset;

    // Physics
    private final double gravity = 0.03 * SCALE;
    private final double collisionFallSpeed = 0.2 * SCALE;

    public Wraith(int xPos, int yPos) {
        super(xPos, yPos, WRAITH_WIDTH, WRAITH_HEIGHT, EnemyType.WRAITH, 25);
        initHitBox(WRAITH_HB_WID, WRAITH_HB_HEI);
        initAttackBox();
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, WRAITH_AB_WID, WRAITH_AB_HEI);
        this.attackOffset =  (int)(40 * SCALE);
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
        Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_DEATH);
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
        if (animIndex == 0) attackCheck = false;
        else if (animIndex == 3 && !attackCheck) checkPlayerHit(attackBox, player);
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