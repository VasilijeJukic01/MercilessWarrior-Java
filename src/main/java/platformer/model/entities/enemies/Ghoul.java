package platformer.model.entities.enemies;

import platformer.animation.AnimType;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.model.Tiles;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Ghoul extends Enemy {

    private int attackOffset;

    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.025 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();

    public Ghoul(int xPos, int yPos) {
        super(xPos, yPos, EnemySize.GHOUL_WIDTH.getValue(), EnemySize.GHOUL_HEIGHT.getValue(), EnemyType.GHOUL, 25);
        int w = (int)(21 * Tiles.SCALE.getValue());
        int h =  (int)(42 * Tiles.SCALE.getValue());
        initHitBox(w, h);
        initAttackBox();
        super.cooldown = new double[1];
    }

    private void initAttackBox() {
        int w = (int)(60 * Tiles.SCALE.getValue());
        int h = (int)(45 * Tiles.SCALE.getValue());
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, w, h);
        this.attackOffset = (int)(20*Tiles.SCALE.getValue());
    }

    public void updateMove(int[][] levelData, Player player) {
        if (!Utils.getInstance().isEntityOnFloor(hitBox, levelData)) inAir = true;
        if (inAir) {
            if (Utils.getInstance().canMoveHere(hitBox.x, hitBox.y + airSpeed, hitBox.width, hitBox.height, levelData)) {
                hitBox.y += airSpeed;
                airSpeed += gravity;
            }
            else {
                hitBox.y = Utils.getInstance().getYPosOnTheCeil(hitBox, airSpeed);
                if (airSpeed > 0) {
                    airSpeed = 0;
                    inAir = false;
                }
                else {
                    airSpeed = collisionFallSpeed;
                }
            }
        }
        else updateBehavior(levelData, player);
    }

    // Attack
    public void hit(double damage, boolean enableRevive, boolean hitSound) {
        if (enableRevive) {
            Random rand = new Random();
            double x = rand.nextDouble();
            if (x < 0.3) {
                entityState = AnimType.HIDE;
                Audio.getInstance().getAudioPlayer().playSound(Sounds.GHOUL_HIDE.ordinal());
                return;
            }
        }
        currentHealth -= damage;
        if (hitSound) Audio.getInstance().getAudioPlayer().playHitSound();
        if (currentHealth <= 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sounds.GHOUL_DEATH.ordinal());
            setEnemyAction(AnimType.DEATH);
        }
        else setEnemyAction(AnimType.HIT);
    }

    public void spellHit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sounds.GHOUL_DEATH.ordinal());
            setEnemyAction(AnimType.DEATH);
        }
        else {
            if (entityState == AnimType.HIT) setEnemyActionNoReset(AnimType.HIT);
            else setEnemyAction(AnimType.HIT);
        }
    }

    // Ghoul Core
    private void updateBehavior(int[][] levelData, Player player) {
        switch (entityState) {
            case IDLE:
                if (cooldown[Cooldown.ATTACK.ordinal()] == 0) setEnemyAction(AnimType.WALK);
                animSpeed = 25;
                break;
            case RUN:
            case WALK:
                if (canSeePlayer(levelData, player)) directToPlayer(player);
                if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player) && cooldown[Cooldown.ATTACK.ordinal()] == 0) {
                    setEnemyAction(AnimType.ATTACK_1);
                    animSpeed = 15;
                }
                double enemyXSpeed = 0;

                if (direction == Direction.LEFT) enemyXSpeed = -enemySpeed;
                else if (direction == Direction.RIGHT) enemyXSpeed = enemySpeed;

                if (Utils.getInstance().canMoveHere(hitBox.x + enemyXSpeed, hitBox.y, hitBox.width, hitBox.height, levelData)) {
                    if (Utils.getInstance().isFloor(hitBox, enemyXSpeed, levelData, direction)) {
                        hitBox.x += enemyXSpeed;
                        return;
                    }
                }

                changeDirection();
                break;
            case ATTACK_1:
                if (animIndex == 0) attackCheck = false;
                if (animIndex == 3 && !attackCheck) {
                    checkPlayerHit(attackBox, player);
                    fastMove(levelData);
                }
                break;
            case REVEAL:
                if (animIndex == 0) {
                    hitBox.x = player.getHitBox().x;
                    hitBox.y = player.getHitBox().y;
                    inAir = true;
                }
                else if (animIndex == 5) Audio.getInstance().getAudioPlayer().playSound(Sounds.GHOUL_REVEAL.ordinal());
            default: break;
        }
    }

    protected void fastMove(int[][] levelData) {
        double xSpeed;

        if (direction == Direction.LEFT) xSpeed = -enemySpeed;
        else xSpeed = enemySpeed;

        if (Utils.getInstance().canMoveHere(hitBox.x + xSpeed * 12, hitBox.y, hitBox.width, hitBox.height, levelData))
            if (Utils.getInstance().isFloor(hitBox, xSpeed * 12, levelData, direction)) {
                hitBox.x += xSpeed * 12;
                return;
            }
        entityState = AnimType.IDLE;
    }

    private void updateAttackBox() {
        this.attackBox.x = hitBox.x - attackOffset;
        this.attackBox.y = hitBox.y;
    }

    public void update(BufferedImage[][] animations, int[][] levelData, Player player) {
        updateMove(levelData, player);
        updateAnimation(animations);
        updateAttackBox();
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