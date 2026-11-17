package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.debug.Debug;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.*;
import static platformer.physics.CollisionDetector.isSightClear;

/**
 * Abstract base class for all enemies in the game.
 * An enemy is a type of entity that can interact with the player and the game world.
 */
@SuppressWarnings("FieldCanBeLocal")
public abstract class Enemy extends Entity implements DamageSource, Debug<Graphics> {

    private final EnemyType enemyType;
    protected double enemySpeed = ENEMY_SPEED_SLOW;
    protected int originalAnimSpeed, animSpeed, animIndex, animTick = 0;
    protected Direction direction = Direction.RIGHT;
    protected boolean alive = true;
    private boolean criticalHit;
    protected int freezeTick = 0;
    protected int stunImmunityTick = 0;
    protected Random rand = new Random();

    public Enemy(int xPos, int yPos, int width, int height, EnemyType enemyType, int animSpeed) {
        super(xPos, yPos, width, height);
        this.enemyType = enemyType;
        this.animSpeed = this.originalAnimSpeed = animSpeed;
        this.maxHealth = enemyType.getHealth();
        this.currentHealth = maxHealth;
        this.cooldown = new double[Cooldown.values().length];
    }

    /**
     * Updates the enemy's state.
     *
     * @param levelData The level data.
     * @param player The player.
     * @param follower The follower.
     */
    public abstract void update(int[][] levelData, Player player, Entity follower);

    protected void updateAnimation() {
        animTick++;
        if (animTick >= animSpeed) {
            animTick = 0;
            animIndex++;
            if (animIndex >= SpriteManager.getInstance().getAnimFrames(enemyType, entityState)) {
                finishAnimation();
            }
        }
        if (cooldown != null) coolDownTickUpdate();
    }

    private void finishAnimation() {
        animIndex = 0;
        if (enemyType == EnemyType.GHOUL && entityState == Anim.ATTACK_1){
            cooldown[Cooldown.ATTACK.ordinal()] = 8 + rand.nextDouble() * 4;
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.SPELL_1 || entityState == Anim.HIT || entityState == Anim.BLOCK || entityState == Anim.REVEAL) {     if (isAttacking()) {
                cooldown[Cooldown.ATTACK.ordinal()] = 2.8 + rand.nextDouble() * 1.5;
            }
            if (entityState == Anim.HIT) {
                enemySpeed = ENEMY_SPEED_SLOW;
            }
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.HIDE) entityState = Anim.REVEAL;
        else if (entityState == Anim.DEATH) alive = false;
    }

    // Targeting
    protected Entity getCloseTarget(Player player, Entity follower) {
        if (follower == null) return player;
        if (follower instanceof Follower) {
            Follower f = (Follower) follower;
            if (f.isKnockedDown()) {
                return player;
            }
        }

        double distToPlayer = Math.abs(player.getHitBox().x - hitBox.x);
        double distToFollower = Math.abs(follower.getHitBox().x - hitBox.x);
        return (distToFollower < distToPlayer) ? follower : player;
    }

    protected boolean canSeeEntity(int[][] levelData, Entity entity) {
        int yTilePlayer = (int)(entity.getHitBox().y / TILES_SIZE) + 1;
        int yTileEnemy = (int)(hitBox.y / TILES_SIZE) + 1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isEntityInSight(entity)) return false;
        return (isSightClear(levelData, hitBox, entity.getHitBox(), yTileEnemy));
    }

    protected void directToEntity(Entity entity) {
        entityState = Anim.RUN;
        configureEnemySpeed();
        if (entity.getHitBox().x > hitBox.x) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    private void configureEnemySpeed() {
        if (enemyType == EnemyType.GHOUL) enemySpeed = GHOUL_SPEED_FAST;
        else if (enemyType == EnemyType.SKELETON) enemySpeed = SKELETON_SPEED_FAST;
        else if (enemyType == EnemyType.KNIGHT) enemySpeed = KNIGHT_SPEED_FAST;
        else if (enemyType == EnemyType.WRAITH) enemySpeed = WRAITH_SPEED_FAST;
    }

    protected boolean isEntityCloseForAttack(Entity entity) {
        int distance = (int)Math.abs(entity.getHitBox().x-hitBox.x);
        if (enemyType == EnemyType.SKELETON) return distance <= SKELETON_ATT_RANGE;
        else if (enemyType == EnemyType.GHOUL) return distance <= GHOUL_ATT_RANGE;
        else if (enemyType == EnemyType.KNIGHT) return distance <= KNIGHT_ATT_RANGE;
        else if (enemyType == EnemyType.WRAITH) return distance <= WRAITH_ATT_RANGE;
        else if (enemyType == EnemyType.LANCER) return distance <= LANCER_ATT_RANGE;
        else if (enemyType == EnemyType.RORIC) return distance <= RORIC_ATT_RANGE;
        return false;
    }

    protected boolean isEntityInSight(Entity entity) {
        int distance = (int)Math.abs(entity.getHitBox().x - hitBox.x);
        return distance <= SIGHT_RANGE;
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = Anim.WALK;
        enemySpeed = ENEMY_SPEED_SLOW;
    }

    // Attack
    public void checkEntityHit(Rectangle2D.Double attackBox, Entity target) {
        if (attackBox.intersects(target.getHitBox())) {
            if (target instanceof Player) {
                Player player = (Player) target;
                boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
                if (!canBlock) player.changeHealth(-enemyType.getDamage(), this);
            }
            else if (target instanceof Follower) {
                Follower follower = (Follower) target;
                if (follower.isKnockedDown()) return;
                follower.hit(enemyType.getDamage(), this);
            }
        }
        else if (enemyType == EnemyType.GHOUL || enemyType == EnemyType.LANCER) return;
        attackCheck = true;
    }

    public boolean isAttacking() {
        return entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3 || entityState == Anim.SPELL_1 || entityState == Anim.SPELL_3;
    }

    public void freeze(int ticks) {
        if (entityState == Anim.DEATH || !alive || stunImmunityTick > 0) return;
        this.freezeTick = Math.max(this.freezeTick, ticks);
        this.attackCheck = true;
        if (entityState != Anim.HIT && enemyType != EnemyType.LANCER && enemyType != EnemyType.RORIC) {
            this.entityState = Anim.HIT;
            this.animIndex = 0;
            this.animTick = 0;
        }
    }

    /**
     * Immediately breaks a stun when the enemy takes a wake-up hit and grants brief stun immunity.
     */
    public void wakeUp() {
        if (freezeTick > 0) {
            this.freezeTick = 0;
            this.stunImmunityTick = 260;
        }
    }

    protected boolean handleStunTick(int[][] levelData, double gravity, double collisionFallSpeed) {
        if (stunImmunityTick > 0) stunImmunityTick--;
        if (freezeTick > 0) {
            freezeTick--;
            if (!platformer.physics.CollisionDetector.isEntityOnFloor(hitBox, levelData)) {
                inAir = true;
            }
            if (inAir) {
                updateInAir(levelData, gravity, collisionFallSpeed);
            }
            if (freezeTick == 0 && entityState == Anim.HIT) {
                this.stunImmunityTick = 200;
                setEnemyAction(Anim.IDLE);
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true during the exact windup animation frames when the player can parry this enemy's attack.
     */
    public boolean isParryWindup() {
        if (!alive || freezeTick > 0 || attackCheck) return false;
        return switch (enemyType) {
            case SKELETON, GHOUL -> entityState == Anim.ATTACK_1 && (animIndex == 1 || animIndex == 2);
            case KNIGHT, WRAITH -> (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2) && (animIndex == 2 || animIndex == 3);
            default -> false;
        };
    }

    public abstract boolean hit(double damage, boolean special, boolean hitSound);

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
        freezeTick = 0;
        stunImmunityTick = 0;
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

    public boolean isVisible() {
        return true;
    }

    /**
     * Dynamically adjusts the animation speed based on the enemy's current state.
     * Lower = Faster !
     */
    protected void setAnimSpeedByState() {
        if (enemyType == EnemyType.LANCER || enemyType == EnemyType.RORIC) return;

        switch (entityState) {
            case RUN:
                animSpeed = 15;
                break;
            case WALK:
                animSpeed = 23;
                break;
            case ATTACK_1:
            case ATTACK_2:
                animSpeed = 18;
                break;
            case HIT:
                animSpeed = 12;
                break;
            default:
                animSpeed = originalAnimSpeed;
                break;
        }
    }

    public void setEnemyAction(Anim enemyAction) {
        this.entityState = enemyAction;
        this.animIndex = 0;
        this.animTick = 0;
        criticalHit = false;
        setAnimSpeedByState();
    }

    public void setEnemyActionNoReset(Anim enemyAction) {
        this.entityState = enemyAction;
        criticalHit = false;
        setAnimSpeedByState();
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

    public boolean isStunned() {
        return alive && entityState != Anim.DEATH && freezeTick > 0;
    }

    public int getFreezeTick() {
        return freezeTick;
    }

    public int getAnimTick() {
        return animTick;
    }

    public int getAnimSpeed() {
        return animSpeed;
    }
}
