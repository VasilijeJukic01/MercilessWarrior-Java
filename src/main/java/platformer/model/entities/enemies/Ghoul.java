package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.audio.Audio;
import platformer.audio.Sound;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.player.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.*;

public class Ghoul extends Enemy {

    private int attackOffset;

    // Physics
    private final double gravity = 0.025 * SCALE;
    private final double collisionFallSpeed = 0.5 * SCALE;

    public Ghoul(int xPos, int yPos) {
        super(xPos, yPos, GHOUL_WIDTH, GHOUL_HEIGHT, EnemyType.GHOUL, 25);
        initHitBox(GHOUL_HB_WID, GHOUL_HB_HEI);
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, GHOUL_AB_WID, GHOUL_AB_HEI);
        this.attackOffset = (int)(20 * SCALE);
    }

    // Attack
    @Override
    public void hit(double damage, boolean enableRevive, boolean hitSound) {
        if (enableRevive) {
            hide();
            if (entityState == Anim.HIDE) return;
        }
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            checkDeath();
        }
        else setEnemyAction(Anim.HIT);
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
    }

    private void hide() {
        Random rand = new Random();
        double x = rand.nextDouble();
        if (x < 0.3) {
            entityState = Anim.HIDE;
            Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_HIDE);
        }
    }

    private void checkDeath() {
        Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_DEATH);
        setEnemyAction(Anim.DEATH);
    }

    // Ghoul Core
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
                attackAction(levelData, player);
                break;
            case REVEAL:
                revealAction(player);
                break;
            default: break;
        }
    }

    // Behavior
    private void idleAction() {
        if (cooldown[Cooldown.ATTACK.ordinal()] == 0) setEnemyAction(Anim.WALK);
        animSpeed = 25;
    }

    private void moveAction(int[][] levelData, Player player) {
        if (canSeePlayer(levelData, player)) directToPlayer(player);
        if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
            setEnemyAction(Anim.ATTACK_1);
            animSpeed = 15;
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

    private void attackAction(int[][] levelData, Player player) {
        if (animIndex == 0) attackCheck = false;
        if (animIndex == 3 && !attackCheck) {
            checkPlayerHit(attackBox, player);
            fastAttack(levelData);
        }
    }

    private void fastAttack(int[][] levelData) {
        double xSpeed;

        if (direction == Direction.LEFT) xSpeed = -enemySpeed;
        else xSpeed = enemySpeed;

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * 12, hitBox.y, hitBox.width, hitBox.height, levelData))
            if (Utils.getInstance().isFloor(hitBox, xSpeed * 12, levelData, direction)) {
                hitBox.x += xSpeed * 12;
                return;
            }
        entityState = Anim.IDLE;
    }

    private void revealAction(Player player) {
        if (animIndex == 0) {
            hitBox.x = player.getHitBox().x;
            hitBox.y = player.getHitBox().y;
            inAir = true;
        }
        else if (animIndex == 5) Audio.getInstance().getAudioPlayer().playSound(Sound.GHOUL_REVEAL);
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
