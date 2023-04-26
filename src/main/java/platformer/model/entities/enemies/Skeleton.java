package platformer.model.entities.enemies;

import platformer.animation.AnimType;
import platformer.audio.Audio;
import platformer.audio.Sounds;
import platformer.model.Tiles;
import platformer.model.entities.Direction;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class Skeleton extends Enemy {

    private int attackOffset;

    // Physics
    private double airSpeed = 0;
    private final double gravity = 0.035 * Tiles.SCALE.getValue();
    private final double collisionFallSpeed = 0.5 * Tiles.SCALE.getValue();

    public Skeleton(int xPos, int yPos) {
        super(xPos, yPos, EnemySize.SKELETON_WIDTH.getValue(), EnemySize.SKELETON_HEIGHT.getValue(), EnemyType.SKELETON, 25);
        int w = (int)(21 * Tiles.SCALE.getValue());
        int h =  (int)(42 * Tiles.SCALE.getValue());
        initHitBox(w, h);
        initAttackBox();
    }

    private void initAttackBox() {
        int w = (int)(80 * Tiles.SCALE.getValue());
        int h = (int)(45 * Tiles.SCALE.getValue());
        this.attackBox = new Rectangle2D.Double(xPos, yPos-1, w, h);
        this.attackOffset =  (int)(30*Tiles.SCALE.getValue());
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
    public void hit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sounds.SKELETON_DEATH_1.ordinal());
            setEnemyAction(AnimType.DEATH);
        }
        else setEnemyAction(AnimType.HIT);
        pushOffsetDirection = Direction.UP;
        pushOffset = 0;
        enemySpeed = 0.2*Tiles.SCALE.getValue();
    }

    public void spellHit(double damage) {
        currentHealth -= damage;
        if (currentHealth <= 0) {
            Audio.getInstance().getAudioPlayer().playSound(Sounds.SKELETON_DEATH_1.ordinal());
            setEnemyAction(AnimType.DEATH);
        }
        else {
            if (entityState == AnimType.HIT) setEnemyActionNoReset(AnimType.HIT);
            else setEnemyAction(AnimType.HIT);
        }
        pushOffsetDirection = Direction.DOWN;
        pushOffset = 0;
        enemySpeed = 0;
    }

    // Skeleton Core
    private void updateBehavior(int[][] levelData, Player player) {
        switch (entityState) {
            case IDLE:
                setEnemyAction(AnimType.WALK);
                animSpeed = 25;
                break;
            case RUN:
            case WALK:
                if (canSeePlayer(levelData, player)) directToPlayer(player);
                if (canSeePlayer(levelData, player) && isPlayerCloseForAttack(player)) {
                    setEnemyAction(AnimType.ATTACK_1);
                    animSpeed = 18;
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
                if (animIndex == 3 && !attackCheck) checkPlayerHit(attackBox, player);
                break;
            case HIT:
                pushBack(pushDirection, levelData, 1.5, enemySpeed);
                updatePushOffset();
            default: break;
        }
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
