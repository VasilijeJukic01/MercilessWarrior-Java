package platformer.model.entities.enemies;

import platformer.animation.AnimType;
import platformer.model.ModelUtils;
import platformer.model.Tiles;
import platformer.debug.Debug;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.Player;
import platformer.utils.Utils;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

@SuppressWarnings("FieldCanBeLocal")
public abstract class Enemy extends Entity implements Debug {

    private final EnemyType enemyType;
    protected double enemySpeed = 0.2*Tiles.SCALE.getValue();
    protected int animSpeed, animIndex, animTick = 0;
    protected Direction direction = Direction.RIGHT;
    protected double attackRange = 1.15*Tiles.TILES_SIZE.getValue();
    protected boolean alive = true;

    public Enemy(int xPos, int yPos, int width, int height, EnemyType enemyType, int animSpeed) {
        super(xPos, yPos, width, height);
        this.enemyType = enemyType;
        this.animSpeed = animSpeed;
        this.maxHealth = ModelUtils.getInstance().getHealth(enemyType);
        this.currentHealth = maxHealth;
    }

    protected void updateAnimation(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                if (entityState == AnimType.ATTACK_1 || entityState == AnimType.HIT || entityState == AnimType.BLOCK) entityState = AnimType.IDLE;
                else if (entityState == AnimType.DEATH) alive = false;
            }
        }
    }

    // Targeting Player
    protected boolean canSeePlayer(int[][] levelData, Player player) {
        int yTilePlayer = (int)(player.getHitBox().y / Tiles.TILES_SIZE.getValue())+1;
        int yTileEnemy = (int)(hitBox.y / Tiles.TILES_SIZE.getValue())+1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isPlayerInSight(player)) return false;
        return (Utils.getInstance().isSightClear(levelData, hitBox, player.getHitBox(), yTileEnemy));
    }

    protected void directToPlayer(Player player) {
        entityState = AnimType.RUN;
        enemySpeed = 0.35*Tiles.SCALE.getValue();
        if (player.getHitBox().x > hitBox.x) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    protected boolean isPlayerCloseForAttack(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x-hitBox.x);
        return distance <= attackRange;
    }

    protected boolean isPlayerInSight(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - hitBox.x);
        return distance <= attackRange * 5;
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = AnimType.WALK;
        enemySpeed = 0.2*Tiles.SCALE.getValue();
    }

    // Attack
    protected void checkPlayerHit(Rectangle2D.Double attackBox, Player player) {
        if (attackBox.intersects(player.getHitBox())) {
            if (!player.canBlock()) player.changeHealth(-ModelUtils.getInstance().getDamage(enemyType), this);
        }
        attackCheck = true;
    }

    // Reset
    protected void reset() {
        hitBox.x = xPos;
        hitBox.y = yPos;
        currentHealth = maxHealth;
        entityState = AnimType.IDLE;
        alive = true;
        animIndex = animTick = 0;
        animSpeed = 25;
        enemySpeed = 0.2*Tiles.SCALE.getValue();
        pushOffset = 0;
    }

    // Getters & Setters
    public int getAnimIndex() {
        return animIndex;
    }

    public AnimType getEnemyAction() {
        return entityState;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setEnemyAction(AnimType enemyAction) {
        this.entityState = enemyAction;
        this.animIndex = 0;
        this.animTick = 0;
    }

    public void setEnemyActionNoReset(AnimType enemyAction) {
        this.entityState = enemyAction;
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

    public double getPushOffset() {
        return pushOffset;
    }

    public Direction getDirection() {
        return direction;
    }
}
