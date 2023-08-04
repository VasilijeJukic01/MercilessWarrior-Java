package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.debug.Debug;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.player.Player;
import platformer.utils.Utils;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import static platformer.constants.Constants.SCALE;
import static platformer.constants.Constants.TILES_SIZE;

@SuppressWarnings("FieldCanBeLocal")
public abstract class Enemy extends Entity implements Debug {

    protected final Random rand;
    private final EnemyType enemyType;
    protected double enemySpeed = 0.2*SCALE;
    protected int originalAnimSpeed, animSpeed, animIndex, animTick = 0;
    protected Direction direction = Direction.RIGHT;
    protected double attackRange = 1.25*TILES_SIZE;
    protected boolean alive = true;
    protected int fadeCoefficient = 0;
    private boolean criticalHit;

    public Enemy(int xPos, int yPos, int width, int height, EnemyType enemyType, int animSpeed) {
        super(xPos, yPos, width, height);
        this.enemyType = enemyType;
        this.animSpeed = this.originalAnimSpeed = animSpeed;
        this.maxHealth = enemyType.getHealth();
        this.currentHealth = maxHealth;
        this.rand = new Random();
    }

    protected void updateAnimation(BufferedImage[][] animations) {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            // Ghoul Only
            if ((entityState == Anim.HIDE || entityState == Anim.REVEAL) && fadeCoefficient < 255) {
                fadeCoefficient += 12;
                fadeCoefficient = Math.min(fadeCoefficient, 255);
            }
            if (animIndex >= animations[entityState.ordinal()].length) {
                animIndex = 0;
                if (enemyType == EnemyType.GHOUL && entityState == Anim.ATTACK_1){
                    cooldown[Cooldown.ATTACK.ordinal()] = 10;
                    entityState = Anim.IDLE;
                }
                else if (entityState == Anim.ATTACK_1 || entityState == Anim.HIT || entityState == Anim.BLOCK || entityState == Anim.REVEAL) {
                    entityState = Anim.IDLE;
                    fadeCoefficient = 0;
                }

                else if (entityState == Anim.HIDE) entityState = Anim.REVEAL;
                else if (entityState == Anim.DEATH) alive = false;
                criticalHit = false;
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    // Targeting Player
    protected boolean canSeePlayer(int[][] levelData, Player player) {
        int yTilePlayer = (int)(player.getHitBox().y / TILES_SIZE)+1;
        int yTileEnemy = (int)(hitBox.y / TILES_SIZE)+1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isPlayerInSight(player)) return false;
        return (Utils.getInstance().isSightClear(levelData, hitBox, player.getHitBox(), yTileEnemy));
    }

    protected void directToPlayer(Player player) {
        entityState = Anim.RUN;
        if (enemyType == EnemyType.GHOUL) enemySpeed = 0.45*SCALE;
        else if (enemyType == EnemyType.SKELETON) enemySpeed = 0.35*SCALE;
        if (player.getHitBox().x > hitBox.x) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    protected boolean isPlayerCloseForAttack(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x-hitBox.x);
        if (enemyType == EnemyType.SKELETON) return distance <= attackRange/1.25;
        else if (enemyType == EnemyType.GHOUL) return distance <= attackRange * 2;
        else if (enemyType == EnemyType.SPEAR_WOMAN) return distance <= attackRange * 1.8;
        return false;
    }

    protected boolean isPlayerInSight(Player player) {
        int distance = (int)Math.abs(player.getHitBox().x - hitBox.x);
        return distance <= attackRange * 5;
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = Anim.WALK;
        enemySpeed = 0.2*SCALE;
    }

    // Attack
    protected void checkPlayerHit(Rectangle2D.Double attackBox, Player player) {
        if (attackBox.intersects(player.getHitBox())) {
            if (!player.canBlock()) player.changeHealth(-enemyType.getDamage(), this);
        }
        else if (enemyType == EnemyType.GHOUL || enemyType == EnemyType.SPEAR_WOMAN) return;
        attackCheck = true;
    }

    // Reset
    protected void reset() {
        hitBox.x = xPos;
        hitBox.y = yPos;
        currentHealth = maxHealth;
        entityState = Anim.IDLE;
        alive = true;
        animIndex = animTick = 0;
        animSpeed = originalAnimSpeed;
        enemySpeed = 0.2*SCALE;
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
    }

    public void setEnemyActionNoReset(Anim enemyAction) {
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

    public void setCriticalHit(boolean criticalHit) {
        this.criticalHit = criticalHit;
    }

    public double getPushOffset() {
        return pushOffset;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getFadeCoefficient() {
        return fadeCoefficient;
    }

    public boolean isCriticalHit() {
        return criticalHit;
    }
}
