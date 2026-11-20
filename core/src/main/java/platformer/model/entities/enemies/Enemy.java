package platformer.model.entities.enemies;

import platformer.animation.Anim;
import platformer.animation.SpriteManager;
import platformer.audio.Audio;
import platformer.audio.types.Sound;
import platformer.debug.Debug;
import platformer.model.entities.Cooldown;
import platformer.model.entities.Direction;
import platformer.model.entities.Entity;
import platformer.model.entities.enemies.loader.EnemyDefinition;
import platformer.model.entities.enemies.loader.EnemyRegistry;
import platformer.model.entities.follower.Follower;
import platformer.model.entities.player.Player;
import platformer.model.entities.player.PlayerAction;
import platformer.physics.DamageSource;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Random;

import static platformer.constants.Constants.TILES_SIZE;
import static platformer.physics.CollisionDetector.isEntityOnFloor;
import static platformer.physics.CollisionDetector.isSightClear;

/**
 * Abstract base class for all enemies in the game, configured via {@link EnemyDefinition}.
 */
public abstract class Enemy extends Entity implements DamageSource, Debug<Graphics> {

    private final EnemyType enemyType;
    protected final EnemyDefinition definition;
    protected double enemySpeed;
    protected int originalAnimSpeed, animSpeed, animIndex, animTick = 0;
    protected Direction direction = Direction.RIGHT;
    protected boolean alive = true;
    private boolean criticalHit;
    protected int freezeTick = 0;
    protected int stunImmunityTick = 0;
    protected Random rand = new Random();

    public Enemy(int xPos, int yPos, EnemyType enemyType) {
        this(xPos, yPos, enemyType, EnemyRegistry.getInstance().get(enemyType));
    }

    private Enemy(int xPos, int yPos, EnemyType enemyType, EnemyDefinition definition) {
        super(xPos, yPos, definition.getDrawWidth(), definition.getDrawHeight());
        this.enemyType = enemyType;
        this.definition = definition;
        this.animSpeed = this.originalAnimSpeed = definition.getDefaultAnimSpeed();
        this.enemySpeed = definition.getScaledWalkSpeed();
        this.maxHealth = enemyType.getHealth();
        this.currentHealth = maxHealth;
        this.cooldown = new double[Cooldown.values().length];
        initHitBox(definition.getHitboxWidth(), definition.getHitboxHeight());
    }

    /**
     * Updates the enemy's state for a single engine tick.
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
        if (entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.SPELL_1 || entityState == Anim.HIT || entityState == Anim.BLOCK || entityState == Anim.REVEAL) {
            if (isAttacking()) {
                double minCd = definition.getAttackCooldownMin();
                double range = Math.max(0.0, definition.getAttackCooldownMax() - minCd);
                cooldown[Cooldown.ATTACK.ordinal()] = minCd + rand.nextDouble() * range;
            }
            if (entityState == Anim.HIT) {
                enemySpeed = definition.getScaledWalkSpeed();
            }
            entityState = Anim.IDLE;
        }
        else if (entityState == Anim.HIDE) entityState = Anim.REVEAL;
        else if (entityState == Anim.DEATH) alive = false;
    }

    // Targeting
    protected Entity getCloseTarget(Player player, Entity follower) {
        if (follower == null) return player;
        if (follower instanceof Follower f && f.isKnockedDown()) {
            return player;
        }

        double distToPlayer = Math.abs(player.getHitBox().x - hitBox.x);
        double distToFollower = Math.abs(follower.getHitBox().x - hitBox.x);
        return (distToFollower < distToPlayer) ? follower : player;
    }

    protected boolean canSeeEntity(int[][] levelData, Entity entity) {
        int yTilePlayer = (int) (entity.getHitBox().y / TILES_SIZE) + 1;
        int yTileEnemy = (int) (hitBox.y / TILES_SIZE) + 1;
        if (yTilePlayer != yTileEnemy) return false;
        if (!isEntityInSight(entity)) return false;
        return isSightClear(levelData, hitBox, entity.getHitBox(), yTileEnemy);
    }

    protected void directToEntity(Entity entity) {
        entityState = Anim.RUN;
        enemySpeed = definition.getScaledRunSpeed();
        faceEntity(entity);
    }

    protected void faceEntity(Entity entity) {
        if (entity.getHitBox().getCenterX() > hitBox.getCenterX()) setDirection(Direction.RIGHT);
        else setDirection(Direction.LEFT);
    }

    protected boolean isEntityCloseForAttack(Entity entity) {
        double distance = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());
        return distance <= definition.getAttackRange();
    }

    protected boolean isEntityInSight(Entity entity) {
        double distance = Math.abs(entity.getHitBox().getCenterX() - hitBox.getCenterX());
        return distance <= definition.getSightRange();
    }

    protected void changeDirection() {
        if (direction == Direction.LEFT) setDirection(Direction.RIGHT);
        else if (direction == Direction.RIGHT) setDirection(Direction.LEFT);
        entityState = Anim.WALK;
        enemySpeed = definition.getScaledWalkSpeed();
    }

    // Combat Helpers
    /**
     * Checks whether the target player successfully parried this enemy's windup attack.
     */
    protected boolean checkPlayerParry(Entity target) {
        if (target instanceof Player player) {
            boolean block = player.checkAction(PlayerAction.BLOCK);
            boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
            if (block && !canBlock) {
                boolean facingEnemy = (player.getHitBox().x < hitBox.x && player.getFlipSign() == 1) || (player.getHitBox().x > hitBox.x && player.getFlipSign() == -1);
                if (facingEnemy) {
                    player.addAction(PlayerAction.CAN_BLOCK);
                    Audio.getInstance().getAudioPlayer().playBlockSound("Player");
                    return true;
                }
            }
        }
        return false;
    }

    protected void playDeathSound() {
        Sound deathSound = definition.getDeathSoundEnum();
        if (deathSound != null) {
            Audio.getInstance().getAudioPlayer().playSound(deathSound);
        }
    }

    public void checkEntityHit(Rectangle2D.Double attackBox, Entity target) {
        if (attackBox.intersects(target.getHitBox())) {
            if (target instanceof Player player) {
                boolean canBlock = player.checkAction(PlayerAction.CAN_BLOCK);
                if (!canBlock) player.changeHealth(-enemyType.getDamage(), this);
            }
            else if (target instanceof Follower follower) {
                if (follower.isKnockedDown()) return;
                follower.hit(enemyType.getDamage(), this);
            }
        }
        else if (definition.isMultiFrameHitCheck()) return;
        attackCheck = true;
    }

    public boolean isAttacking() {
        return entityState == Anim.ATTACK_1 || entityState == Anim.ATTACK_2 || entityState == Anim.ATTACK_3 || entityState == Anim.SPELL_1 || entityState == Anim.SPELL_3;
    }

    public boolean isObjectBreakFrame() {
        int breakFrame = definition.getAttackObjectBreakFrame();
        return breakFrame >= 0 && animIndex == breakFrame;
    }

    public void freeze(int ticks) {
        if (entityState == Anim.DEATH || !alive || stunImmunityTick > 0) return;
        this.freezeTick = Math.max(this.freezeTick, ticks);
        this.attackCheck = true;
        if (entityState != Anim.HIT && !definition.isBoss()) {
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

    protected boolean handleStunTick(int[][] levelData) {
        return handleStunTick(levelData, definition.getScaledGravity(), definition.getScaledCollisionFallSpeed());
    }

    protected boolean handleStunTick(int[][] levelData, double gravity, double collisionFallSpeed) {
        if (stunImmunityTick > 0) stunImmunityTick--;
        if (freezeTick > 0) {
            freezeTick--;
            if (!isEntityOnFloor(hitBox, levelData)) {
                inAir = true;
            }
            if (inAir) {
                updateInAir(levelData, gravity, collisionFallSpeed);
            }
            updateAnimation();
            if (freezeTick == 0) {
                this.stunImmunityTick = 260;
            }
            return true;
        }
        return false;
    }

    /**
     * Returns true during the windup animation frames when the player can parry this enemy's attack.
     */
    public boolean isParryWindup() {
        if (!alive || freezeTick > 0 || attackCheck) return false;
        if (definition.getParryWindupStart() < 0) return false;
        if (entityState != Anim.ATTACK_1 && entityState != Anim.ATTACK_2) return false;
        return animIndex >= definition.getParryWindupStart() && animIndex <= definition.getParryWindupEnd();
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
        enemySpeed = definition.getScaledWalkSpeed();
        pushOffset = 0;
        freezeTick = 0;
        stunImmunityTick = 0;
    }

    // Debug Rendering
    @Override
    public void hitBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset, Color color) {
        renderHitBox(g, xLevelOffset, yLevelOffset, color);
    }

    @Override
    public void attackBoxRenderer(Graphics g, int xLevelOffset, int yLevelOffset) {
        renderAttackBox(g, xLevelOffset, yLevelOffset);
    }

    // Getters & Setters
    public EnemyDefinition getDefinition() {
        return definition;
    }

    public boolean isUndead() {
        return definition.isUndead();
    }

    public boolean isBoss() {
        return definition.isBoss();
    }

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
     * Dynamically adjusts animation speed from {@link EnemyDefinition} based on the current state.
     */
    protected void setAnimSpeedByState() {
        if (definition.isBoss()) return;

        switch (entityState) {
            case RUN -> animSpeed = definition.getRunAnimSpeed();
            case WALK -> animSpeed = definition.getWalkAnimSpeed();
            case ATTACK_1, ATTACK_2 -> animSpeed = definition.getAttackAnimSpeed();
            case HIT -> animSpeed = definition.getHitAnimSpeed();
            default -> animSpeed = originalAnimSpeed;
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
