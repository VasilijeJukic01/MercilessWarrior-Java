package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.debug.Debug;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.utils.Utils;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static platformer.constants.Constants.*;

/**
 * Abstract base class for all enemies in the game.
 * An enemy is a type of entity that can interact with the player and the game world.
 */
@SuppressWarnings("FieldCanBeLocal")
public abstract class Enemy extends Entity implements Debug<Graphics> {

    private final EnemyType enemyType;
    protected double enemySpeed = ENEMY_SPEED_SLOW;
    protected int originalAnimSpeed, animSpeed, animIndex, animTick = 0;
    protected Direction direction = Direction.RIGHT;
    protected boolean alive = true;
    private boolean criticalHit;

    public Enemy(int xPos, int yPos, int width, int height, EnemyType enemyType, int animSpeed) {
        super(xPos, yPos, width, height);
        this.enemyType = enemyType;
        this.animSpeed = this.originalAnimSpeed = animSpeed;
        this.maxHealth = enemyType.getHealth();
        this.currentHealth = maxHealth;
    }

    /**
     * Updates the enemy's state.
     *
     * @param animations The animations for the enemy.
     * @param levelData The level data.
     * @param player The player.
     */
    public abstract void update(BufferedImage[][] animations, int[][] levelData, Player player);

    protected void updateAnimation(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                finishAnimation();
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    private void finishAnimation() {
        animIndex = 0;
        if (enemyType == EnemyType.GHOUL && entityState == Anim.ATTACK_1){
            cooldown[Cooldown.ATTACK.ordinal()] = GHOUL_ATT_CD;
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.HIT || entityState == Anim.BLOCK || entityState == Anim.REVEAL) {
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.HIDE) entityState = Anim.REVEAL;
        else if (entityState == Anim.DEATH) alive = false;
    }

    // Targeting Player
    protected boolean canSeePlayer(int[][] levelData, Player player) {
        int yTilePlayer = (int)(player.getHitBox().y / TILES_SIZE) + 1;
        int yTileEnemy = (int)(hitBox.y / TILES_SIZE) + 1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isPlayerInSight(player)) return false;
        return (Utils.getInstance().isSightClear(levelData, hitBox, player.getHitBox(), yTileEnemy));
    }

    protected void directToPlayer(Player player) {
        entityState = Anim.RUN;
        configureEnemySpeed();
        if (player.getHitBox().x > hitBox.x) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    private void configureEnemySpeed() {
        if (enemyType == EnemyType.GHOUL) enemySpeed = GHOUL_SPEED_FAST;
        else if (enemyType == EnemyType.SKELETON) enemySpeed = SKELETON_SPEED_FAST;
        else if (enemyType == EnemyType.KNIGHT) enemySpeed = KNIGHT_SPEED_FAST;
        else if (enemyType == EnemyType.WRAITH) enemySpeed = WRAITH_SPEED_FAST;
    }

    protected boolean isPlayerCloseForAttack(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x-hitBox.x);
        if (enemyType == EnemyType.SKELETON) return distance <= SKELETON_ATT_RANGE;
        else if (enemyType == EnemyType.GHOUL) return distance <= GHOUL_ATT_RANGE;
        else if (enemyType == EnemyType.KNIGHT) return distance <= KNIGHT_ATT_RANGE;
        else if (enemyType == EnemyType.WRAITH) return distance <= WRAITH_ATT_RANGE;
        else if (enemyType == EnemyType.SPEAR_WOMAN) return distance <= SW_ATT_RANGE;
        return false;
    }

    protected boolean isPlayerInSight(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - hitBox.x);
        return distance <= SIGHT_RANGE;
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = Anim.WALK;
        enemySpeed = ENEMY_SPEED_SLOW;
    }

    // Attack
    protected void checkPlayerHit(Rectangle2D.Double attackBox, Player player) {
        if (attackBox.intersects(player.getHitBox())) {
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (!canBlock) player.changeHealth(-enemyType.getDamage(), this);
        }
        else if (enemyType == EnemyType.GHOUL || enemyType == EnemyType.SPEAR_WOMAN) return;
        attackCheck = true;
    }

    public abstract void hit(double damage, boolean special, boolean hitSound);

    public abstract void spellHit(double damage);

    // Reset
    protected void reset() {
        hitBox.x = xPos;
        hitBox.y = yPos;
        currentHealth = maxHealth;
        entityState = Anim.IDLE;
        alive = true;
        animIndex = animTick = 0;
        animSpeed = originalAnimSpeed;
        enemySpeed = ENEMY_SPEED_SLOW;
        pushOffset = 0;
    }

    // Getters & Setters
    public int getAnimIndex() {
        return animIndex;
    }

    public Anim getEnemyAction() {
        return entityState;
    }

    public EnemyType getEnemyType() {
        return enemyType;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setEnemyAction(Anim enemyAction) {
        this.entityState = enemyAction;
        this.animIndex = 0;
        this.animTick = 0;
        criticalHit = false;
    }

    public void setEnemyActionNoReset(Anim enemyAction) {
        this.entityState = enemyAction;
        criticalHit = false;
    }

    public void setDirection(Direction direction) {
        if (direction == Direction.RIGHT) {
            this.flipCoefficient = 0;
            this.flipSign = 1;
            this.pushDirection = Direction.LEFT;
        }
        else if (direction == Direction.LEFT) {
            this.flipCoefficient = width;
            this.flipSign = -1;
            this.pushDirection = Direction.RIGHT;
        }
        this.direction = direction;
    }

    public void setCriticalHit(boolean criticalHit) {
        this.criticalHit = criticalHit;
    }

    public double getPushOffset() {
        return pushOffset;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isCriticalHit() {
        return criticalHit;
    }
}
